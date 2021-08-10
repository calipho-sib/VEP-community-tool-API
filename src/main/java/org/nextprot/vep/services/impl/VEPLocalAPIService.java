package org.nextprot.vep.services.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmd;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.apache.commons.io.IOUtils;
import org.nextprot.vep.domain.ProteinVariant;
import org.nextprot.vep.domain.SequenceMappingProfile;
import org.nextprot.vep.services.SequenceMappingService;
import org.nextprot.vep.services.VEPAPIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;
import java.util.List;

@Service
@Qualifier("LocalService")
public class VEPLocalAPIService implements VEPAPIService {

    @Autowired
    SequenceMappingService sequenceMappingService;

    @Value("${DOCKER_HOST}")
    private String dockerHost;

    @Value("${DOCKER_CERT_PATH}")
    private String dockerCertPath;

    @Value("${registry.username}")
    private String registryUserName;

    @Value("${registry.password}")
    private String registryPassword;

    @Value("${registry.url}")
    private String registryURL;

    private DockerClientConfig config;
    private DockerHttpClient httpClient;

    @PostConstruct
    public void init() {
        config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .withDockerTlsVerify(true)
                .withDockerCertPath(dockerCertPath)
                .withRegistryUsername(registryUserName)
                .withRegistryPassword(registryPassword)
                .withRegistryUrl(registryURL)
                .build();

        httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

    }

    @Override
    public List<ProteinVariant> getVEPResults(String isoform, List<ProteinVariant> variants) {
        getVEPResultsFromDocker();
        return null;
    }

    private List<ProteinVariant> getVEPResultsFromDocker() {
        DockerHttpClient.Request request = DockerHttpClient.Request.builder()
                .method(DockerHttpClient.Request.Method.GET)
                .path("/_ping")
                .build();

        try  {
            DockerHttpClient.Response response = httpClient.execute(request);
            System.out.println(IOUtils.toString(response.getBody()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        DockerClient client = DockerClientImpl.getInstance(config, httpClient);
        List<Container> containers = client.listContainersCmd().exec();
        Optional<Container> ensemblContainer = containers.stream().filter(container -> "ensemblvep".equals(container.getImage())).findFirst();
        String containerId = null;
        if(ensemblContainer.isPresent()) {
            containerId = ensemblContainer.get().getId();
        }
        client.execCreateCmd("./vep").exec();
        return null;
    }
}
