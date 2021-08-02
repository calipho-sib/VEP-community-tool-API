package org.nextprot.vep.services.impl;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.nextprot.vep.domain.ProteinVariant;
import org.nextprot.vep.domain.SequenceMappingProfile;
import org.nextprot.vep.services.SequenceMappingService;
import org.nextprot.vep.services.VEPAPIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.json.JsonParser;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VEPAPIServiceImpl implements VEPAPIService {

    private CloseableHttpClient httpClient;

    private HttpPost httpPost;

    @Value("${VEP_REST_ENDPOINT}")
    private String VEPRESTEndpoint;

    @Autowired
    SequenceMappingService sequenceMappingService;

    @PostConstruct
    void initialize() {
        httpClient = HttpClients.createDefault();
        httpPost = new HttpPost(VEPRESTEndpoint);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
    }

    @Override
    public List<ProteinVariant> getVEPResults(String isoform, List<ProteinVariant> variants) {
        // Call the mapping service to get the ENSP mappings
        SequenceMappingProfile mappingProfile = sequenceMappingService.getMappingProfile(isoform);
        String ensp = mappingProfile.getEnsp();

        // Maps the nextprot isoform positions into ENSP HGVS identifiers
        Map<String, ProteinVariant> proteinVariantMap = new HashMap<>();
        List<String> ensps = new ArrayList<>();
        for(ProteinVariant variant : variants) {
            String originalAminoAcid = variant.getOriginalAminoAcid();
            String variantAminoAcid = variant.getVariantAminoAcid();
            int nextprotPosition = variant.getNextprotPosition();

            // Compute the ensembl position
            int ensemblPosition = nextprotPosition + mappingProfile.getOffset();
            String enspHGVS = "\"" + ensp + ".1:p." + "Met" + ensemblPosition + "Ser" + "\"";
            enspHGVS = "\"ENSP00000374828.2:p.Asn2Met\"";
            ensps.add(enspHGVS);
            proteinVariantMap.put(enspHGVS, variant);
            break;
        }

        // Calls the VEP API to get the SIFT and Polyphen results
        List<Object> VEPResponse;
        try {
            String payload = getPayload(ensps);
            httpPost.setEntity(new StringEntity(payload));
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String jsonResponse = EntityUtils.toString(response.getEntity());
            JsonParser jsonParser = new JacksonJsonParser();
            VEPResponse = jsonParser.parseList(jsonResponse);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Adds the SIFT and polyphen values to the variants
        /*for(Object vepResponse: VEPResponse) {
            vepResponse
            ProteinVariant variant = proteinVariantMap.get(ensgHGVS);

        }*/

        return null;
    }

    private String getPayload(List<String> ensps) {
        String request = "{ \"hgvs_notations\" :";
        String hgvs = String.join(",", ensps);
        return request + "[" + hgvs + "]" + "}";
    }
}
