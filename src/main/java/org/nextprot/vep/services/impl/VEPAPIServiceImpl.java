package org.nextprot.vep.services.impl;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.nextprot.vep.domain.ProteinVariant;
import org.nextprot.vep.domain.ProteinVariantRequest;
import org.nextprot.vep.domain.SequenceMappingProfile;
import org.nextprot.vep.domain.TranscriptConsequence;
import org.nextprot.vep.services.AminoAcidService;
import org.nextprot.vep.services.SequenceMappingService;
import org.nextprot.vep.services.VEPAPIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.json.JsonParser;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Qualifier("APIService")
public class VEPAPIServiceImpl implements VEPAPIService {

    Logger logger = LoggerFactory.getLogger(VEPAPIServiceImpl.class);

    private CloseableHttpClient httpClient;

    private HttpPost httpPost;

    private final Environment environment;

    private final HashMap<String,String> VEPEndpoints = new HashMap<>();

    private String AMINO_ACID_DELETION = "del";

    private String DEFAULT_VEP_VERSION = "107";

    @Autowired
    SequenceMappingService sequenceMappingService;

    @Autowired
    AminoAcidService aminoAcidService;

    public VEPAPIServiceImpl(Environment environment) {
        this.environment = environment;
        VEPEndpoints.put("107", this.environment.getProperty("VEP_REST_ENDPOINT_107"));
        VEPEndpoints.put("109", this.environment.getProperty("VEP_REST_ENDPOINT_109"));
    }

    @PostConstruct
    void initialize() {
        httpClient = HttpClients.createDefault();
        createPOSTRequest(DEFAULT_VEP_VERSION);
    }

    private void createPOSTRequest(String vepVersion) {
        try {
            httpPost = new HttpPost(VEPEndpoints.get(vepVersion));
            URI uri = new URIBuilder(httpPost.getURI())
                    .addParameter("ambiguous_hgvs", "1")
                    .build();
            httpPost.setURI(uri);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<ProteinVariant> getVEPResults(ProteinVariantRequest vepRequest) {

        String vepVersion = vepRequest.getVersion();
        if(vepVersion == null) vepVersion = DEFAULT_VEP_VERSION;
        createPOSTRequest(vepVersion);
        String isoform = vepRequest.getIsoform();
        List<ProteinVariant> variants = vepRequest.getVariants();


        String logString = String.join(",", variants.stream()
                .map(variant -> variant.getOriginalAminoAcid() + "->" + variant.getVariantAminoAcid())
                .collect(Collectors.toList()));

        // Maps the nextprot isoform positions into ENSP HGVS identifiers
        // Call the mapping service to get the ENSP mappings
        List<TranscriptConsequence> transcriptConsequences = new ArrayList<>();
        List<SequenceMappingProfile> mappingProfiles = sequenceMappingService.getMappingProfilesForIsoform(isoform);
        for(SequenceMappingProfile mappingProfile: mappingProfiles) {
            String ensp = mappingProfile.getEnsp();
            String enst = mappingProfile.getEnst();

            List<String> ensps = new ArrayList<>();
            for (ProteinVariant variant : variants) {
                String originalAminoAcid = variant.getOriginalAminoAcid();
                String variantAminoAcid = variant.getVariantAminoAcid();
                int nextprotPosition = variant.getNextprotPosition();

                // Compute the ensembl position
                int ensemblPosition = nextprotPosition + mappingProfile.getOffset();
                try {
                    String enspString = ensp + ".1:p." + aminoAcidService.getThreeLetterCode(originalAminoAcid) + ensemblPosition;
                    // Have to handle substitution and deletion (currently only missense)
                    if (variantAminoAcid.equals(AMINO_ACID_DELETION)) {
                        enspString += AMINO_ACID_DELETION;
                    } else {
                        enspString += aminoAcidService.getThreeLetterCode(variantAminoAcid);
                    }

                    String enspHGVS = "\"" + enspString + "\"";
                    ensps.add(enspHGVS);

                    variant.addEnspMapping(ensp);
                    variant.addEnstMapping(enst);
                    variant.addHGVSVariant(enspString);

                } catch (Exception e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                }
            }
            // Calls the VEP API to get the SIFT and Polyphen results
            try {
                logger.info("Calling the VEP API for " + ensps.size() + " variants for " + ensp);
                String payload = getPayload(ensps);
                httpPost.setEntity(new StringEntity(payload));
                CloseableHttpResponse response = httpClient.execute(httpPost);
                String jsonResponse = EntityUtils.toString(response.getEntity());
                transcriptConsequences.addAll(parseVEPResponse(jsonResponse));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        // Adds the SIFT and polyphen values to the variants
        // VEP API returns a list of
        // - transcript_consequences
        // - regulatory_featur_consequences
        // For the moment, we only consider transcript_consequences
        // A given resulting transcript consequence is matched to a given ENSP input
        // if consequence_terms contain missense_variant
        // and biotype is protein_coding
        // and with a matching ENST to ENSP
        for (ProteinVariant proteinVariant: variants ) {

            // Get the consequnces corresponding to the hgvs and ensts and retrive the sift/polyphen scores
            List<TranscriptConsequence> transcriptConsequencesForVariant =
                    transcriptConsequences
                            .stream()
                            .filter(consequence -> proteinVariant.getRequestVariants().contains(consequence.getId()))
                            .collect(Collectors.toList());


            for(TranscriptConsequence transcriptConsequence : transcriptConsequencesForVariant) {

                if(!transcriptConsequence.getConsequenceTerm().contains("missense_variant")) continue;
                if(!"protein_coding".equals(transcriptConsequence.getBioType())) continue;
                if(!proteinVariant.getEnstMappings().contains(transcriptConsequence.getEnst())) continue;

                proteinVariant.setResultEnst(transcriptConsequence.getEnst());
                double siftScore;
                if (transcriptConsequence.getSiftScore() != -1) {
                    siftScore = transcriptConsequence.getSiftScore();
                    String siftPrediction = transcriptConsequence.getSiftPrediction();
                    proteinVariant.setSift(String.valueOf(siftScore));
                    proteinVariant.setSiftPrediction(siftPrediction);
                }

                double polyphenScore;
                if (transcriptConsequence.getPolypheScore() != -1) {
                    polyphenScore = transcriptConsequence.getPolypheScore();
                    String polyphenPrediction = transcriptConsequence.getPolyphePrediction();
                    proteinVariant.setPolyphen(String.valueOf(polyphenScore));
                    proteinVariant.setPolyphenPrediction(polyphenPrediction);
                }

                proteinVariant.setStatus(ProteinVariant.RESULTS_FOUND);
                break;
            }
        }

        // Append errors for the variants without consequence results from VEP
        variants
                .stream()
                .filter(variant -> variant.getSift() == null && variant.getPolyphen() == null)
                .forEach(variant -> variant.setStatus(ProteinVariant.ERROR));

        logger.info("Computed VEP results for variants: " + logString);
        return new ArrayList<>(variants);
    }

    /**
     * Parse the json response from VEP and returns a list of transcript consequences
     * @param jsonResponse
     * @return List of TranscriptConsequences with consequence term missense_variant
     */
    private List<TranscriptConsequence> parseVEPResponse(String jsonResponse) {
        JsonParser jsonParser = new JacksonJsonParser();
        // It can return error instead of a list of predictions
        if (jsonResponse.contains("error")) {
            logger.error(jsonParser.parseMap(jsonResponse).get("error").toString());
            return new ArrayList<>();
        } else {
            List<TranscriptConsequence> transcriptConsequences = new ArrayList<>();
            List<Object> VEPResponse = jsonParser.parseList(jsonResponse);
            for (Object vepResponse : VEPResponse) {
                Map<?, ?> vepResponseMap = (Map<?, ?>) vepResponse;
                List<Map<?, ?>> transcriptConsequenceList = (List<Map<?, ?>>) vepResponseMap.get("transcript_consequences");

                for (Map<?, ?> transcriptConsequenceResponse : transcriptConsequenceList) {
                    String id = (String) vepResponseMap.get("id");
                    List<String> consequenceTerms = (List<String>) transcriptConsequenceResponse.get("consequence_terms");
                    String bioType = (String) transcriptConsequenceResponse.get("biotype");
                    String enst = (String) transcriptConsequenceResponse.get("transcript_id");

                    double siftScore = -1;
                    Object siftScoreObj = transcriptConsequenceResponse.get("sift_score");
                    if (siftScoreObj instanceof Number) {
                        siftScore = ((Number) siftScoreObj).doubleValue();
                    }

                    String siftPrediction = (String) transcriptConsequenceResponse.get("sift_prediction");

                    double polyphenScore = -1;
                    Object polyphenScoreObj = transcriptConsequenceResponse.get("polyphen_score");
                    if (polyphenScoreObj instanceof Number) {
                        polyphenScore = ((Number) polyphenScoreObj).doubleValue();
                    }

                    String polyphenPrediction = (String) transcriptConsequenceResponse.get("polyphen_prediction");

                    TranscriptConsequence transcriptConsequence = new TranscriptConsequence.Builder()
                            .withId(id)
                            .withEnst(enst)
                            .withConsequenceTerm(consequenceTerms)
                            .withBioType(bioType)
                            .withSiftScore(siftScore)
                            .withSiftPrediction(siftPrediction)
                            .withPolypheScore(polyphenScore)
                            .withPolyphePrediction(polyphenPrediction)
                            .build();

                    transcriptConsequences.add(transcriptConsequence);
                }
            }
            return transcriptConsequences;
        }
    }

    private String getPayload(List<String> ensps) {
        String request = "{ \"hgvs_notations\" :";
        String hgvs = String.join(",", ensps);
        return request + "[" + hgvs + "]" + "}";
    }
}
