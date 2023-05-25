package org.nextprot.vep.services.impl;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.nextprot.vep.domain.ProteinVariant;
import org.nextprot.vep.domain.SequenceMappingProfile;
import org.nextprot.vep.services.AminoAcidService;
import org.nextprot.vep.services.SequenceMappingService;
import org.nextprot.vep.services.VEPAPIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${VEP_REST_ENDPOINT}")
    private String VEPRESTEndpoint;

    private String AMINO_ACID_DELETION = "del";

    @Autowired
    SequenceMappingService sequenceMappingService;

    @Autowired
    AminoAcidService aminoAcidService;

    @PostConstruct
    void initialize() {
        httpClient = HttpClients.createDefault();
        httpPost = new HttpPost(VEPRESTEndpoint);
        try {
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
    public List<ProteinVariant> getVEPResults(String isoform, List<ProteinVariant> variants) {

        String logString = String.join(",", variants.stream()
                .map(variant -> variant.getOriginalAminoAcid() + "->" + variant.getVariantAminoAcid())
                .collect(Collectors.toList()));

        // Call the mapping service to get the ENSP mappings
        SequenceMappingProfile mappingProfile = sequenceMappingService.getMappingProfile(isoform);
        String ensp = mappingProfile.getEnsp();
        String enst = mappingProfile.getEnst();

        // Maps the nextprot isoform positions into ENSP HGVS identifiers
        Map<String, ProteinVariant> proteinVariantMap = new HashMap<>();
        List<String> ensps = new ArrayList<>();
        for (ProteinVariant variant : variants) {
            String originalAminoAcid = variant.getOriginalAminoAcid();
            String variantAminoAcid = variant.getVariantAminoAcid();
            int nextprotPosition = variant.getNextprotPosition();

            // Compute the ensembl position
            int ensemblPosition = nextprotPosition + mappingProfile.getOffset();
            try {
                String enspString = ensp + ".1:p." + aminoAcidService.getThreeLetterCode(originalAminoAcid) + ensemblPosition;
                // Have to handle substitution and deletion
                if (variantAminoAcid.equals(AMINO_ACID_DELETION)) {
                    enspString += AMINO_ACID_DELETION;
                } else {
                    enspString += aminoAcidService.getThreeLetterCode(variantAminoAcid);
                }

                String enspHGVS = "\"" + enspString + "\"";
                ensps.add(enspHGVS);
                proteinVariantMap.put(enspString, variant);
            } catch (Exception e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
        }

        // Calls the VEP API to get the SIFT and Polyphen results
        List<Object> VEPResponse = null;
        try {
            String payload = getPayload(ensps);
            httpPost.setEntity(new StringEntity(payload));
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String jsonResponse = EntityUtils.toString(response.getEntity());

            JsonParser jsonParser = new JacksonJsonParser();
            // It can return error instead of a list of predictions
            if (jsonResponse.contains("error")) {
                logger.error(jsonParser.parseMap(jsonResponse).get("error").toString());
                return new ArrayList<>();
            }
            VEPResponse = jsonParser.parseList(jsonResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (VEPResponse == null) {
            logger.error("Could not get the response from ensembl");
            return new ArrayList<>();
        }

        // Adds the SIFT and polyphen values to the variants
        for (Object vepResponse : VEPResponse) {
            System.out.println(vepResponse.toString());
            Optional<Object> consequence = ((List) ((Map) vepResponse).get("transcript_consequences"))
                    .stream()
                    .filter(item -> "protein_coding".equals(((Map) item).get("biotype")) &&
                            ((List) ((Map) item).get("consequence_terms")).contains("missense_variant") &&
                            enst.equals(((Map) item).get("transcript_id")))
                    .findFirst();
            if (consequence.isPresent()) {
                Map consequenceMap = (Map) consequence.get();
                double siftScore = -1;
                if (consequenceMap.get("sift_score") != null) {
                    if (consequenceMap.get("sift_score").equals(0) || consequenceMap.get("sift_score").equals(1)) {
                        siftScore = (Integer) consequenceMap.get("sift_score");
                    } else {
                        siftScore = (double) consequenceMap.get("sift_score");
                    }
                    String siftPrediction = (String) consequenceMap.get("sift_prediction");
                    proteinVariantMap.get(((Map) vepResponse).get("id")).setSift(siftScore);
                    proteinVariantMap.get(((Map) vepResponse).get("id")).setSiftPrediction(siftPrediction);
                }

                double polyphenScore = -1;
                if (consequenceMap.get("polyphen_score") != null) {
                    if (consequenceMap.get("polyphen_score").equals(0) || consequenceMap.get("polyphen_score").equals(1)) {
                        polyphenScore = (Integer) consequenceMap.get("polyphen_score");
                    } else {
                        polyphenScore = (double) consequenceMap.get("polyphen_score");
                    }
                    String polyphenPrediction = (String) consequenceMap.get("polyphen_prediction");
                    proteinVariantMap.get(((Map) vepResponse).get("id")).setPolyphen(polyphenScore);
                    proteinVariantMap.get(((Map) vepResponse).get("id")).setPolyphenPrediction(polyphenPrediction);
                }

                proteinVariantMap.get(((Map) vepResponse).get("id")).setStatus(ProteinVariant.RESULTS_FOUND);
            }
        }

        // Append errors for the variants without consequence results from VEP
        proteinVariantMap.values()
                .stream()
                .filter(variant -> variant.getSift() == -1 && variant.getPolyphen() == -1)
                .forEach(variant -> variant.setStatus(ProteinVariant.ERROR));

        logger.info("Succesfully computed VEP results for variants: " + logString);
        return new ArrayList<>(proteinVariantMap.values());
    }

    private String getPayload(List<String> ensps) {
        String request = "{ \"hgvs_notations\" :";
        String hgvs = String.join(",", ensps);
        return request + "[" + hgvs + "]" + "}";
    }
}
