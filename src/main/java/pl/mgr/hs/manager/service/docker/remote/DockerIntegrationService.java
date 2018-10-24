package pl.mgr.hs.manager.service.docker.remote;

import com.spotify.docker.client.messages.swarm.Node;
import pl.mgr.hs.manager.service.docker.DockerMachineEnv;

import java.util.List;

/**
 * Created by dominik on 24.10.18.
 */
public interface DockerIntegrationService {
    List<Node> getConnectedHosts(DockerMachineEnv machineEnv);
}
