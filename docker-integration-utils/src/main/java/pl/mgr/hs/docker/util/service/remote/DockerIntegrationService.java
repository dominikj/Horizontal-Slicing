package pl.mgr.hs.docker.util.service.remote;

import com.spotify.docker.client.messages.swarm.Node;
import pl.mgr.hs.docker.util.service.DockerMachineEnv;

import java.util.List;

/**
 * Created by dominik on 24.10.18.
 */
public interface DockerIntegrationService {
    List<Node> getConnectedHosts(DockerMachineEnv machineEnv);
}
