package pl.mgr.hs.manager.service.docker.remote;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.swarm.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.mgr.hs.manager.service.docker.DockerMachineEnv;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by dominik on 24.10.18.
 */
@Service
public class DefaultDockerIntegrationService implements DockerIntegrationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDockerIntegrationService.class);
    private static final String WORKER_ROLE = "worker";
    private static final String RUNNING_STATUS = "running";

    @Override
    public List<Node> getConnectedHosts(DockerMachineEnv machineEnv) {

        DockerClient docker = createDockerConnection(machineEnv);
        List<Node> nodes;
        try {
            nodes = docker.listNodes(Node.Criteria.builder().nodeRole(WORKER_ROLE).build());
        } catch (DockerException | InterruptedException e) {
            LOGGER.error("Cannot get nodes list from machine {}", machineEnv.getAddress().getHost());
            nodes = Collections.emptyList();
        }
        docker.close();
        return nodes.stream().filter(node -> RUNNING_STATUS.equals(node.status().state())).collect(Collectors.toList());
    }

    private DockerClient createDockerConnection(DockerMachineEnv machineEnv) {
        try {
            return DefaultDockerClient.builder()
                    .uri(machineEnv.getAddress())
                    .dockerCertificates(new DockerCertificates(machineEnv.getCertPath()))
                    .build();
        } catch (DockerCertificateException e) {
            throw new IllegalStateException("Cannot get certs for docker machine", e);
        }
    }
}
