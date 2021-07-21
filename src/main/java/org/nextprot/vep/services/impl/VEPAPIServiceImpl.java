package org.nextprot.vep.services.impl;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.nextprot.vep.domain.ProteinVariant;
import org.nextprot.vep.services.VEPAPIService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.json.JsonParser;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Service
public class VEPAPIServiceImpl implements VEPAPIService {

    private CloseableHttpClient httpClient;

    private HttpPost httpPost;

    @Value("${VEP_REST_ENDPOINT}")
    private String VEPRESTEndpoint;

    @PostConstruct
    void initialize() {
        httpClient = HttpClients.createDefault();
        httpPost = new HttpPost(VEPRESTEndpoint);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        String request = "{ \"hgvs_notations\" : [\"ENST00000366667:c.803C>T\", \"9:g.22125504G>C\" ] }";
        try {
            httpPost.setEntity(new StringEntity(request));
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String jsonResponse = EntityUtils.toString(response.getEntity());
            System.out.println(jsonResponse);
            JsonParser jsonParser = new JacksonJsonParser();
            List<Object> ensemblResponse = jsonParser.parseList(jsonResponse);
            System.out.println(ensemblResponse);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<ProteinVariant> getVEPResults() {
        return null;
    }
}
