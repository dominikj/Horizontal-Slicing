package pl.mgr.hs.docker.util.service.remote;

import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.swarm.Node;
import com.spotify.docker.client.messages.swarm.Service;
import com.spotify.docker.client.messages.swarm.Swarm;
import com.spotify.docker.client.messages.swarm.Task;
import pl.mgr.hs.docker.util.service.DockerMachineEnv;

import java.util.List;
import java.util.Optional;

/** Created by dominik on 24.10.18. */
public interface DockerIntegrationService {
  List<Node> getNodes(DockerMachineEnv machineEnv);

  List<Task> getTasksForNode(DockerMachineEnv machineEnv, String nodeId);

  Optional<Swarm> getSwarmConfiguration(DockerMachineEnv machineEnv);

  List<Service> getServices(DockerMachineEnv machineEnv);

  List<Container> getContainers(DockerMachineEnv machineEnv, boolean onlyRunningContainers);

  void removeNodesFromSwarm(DockerMachineEnv machineEnv, List<String> nodeIds);

  void leaveSwarm(DockerMachineEnv machineEnv);

  void initSwarm(DockerMachineEnv machineEnv);

  void initSwarm(DockerMachineEnv machineEnv, String advertiseAddress);

  void createSliceService(DockerMachineEnv machineEnv, String imageId, Integer port);

  void removeServerContainer(DockerMachineEnv machineEnv);

  void restartServerContainer(DockerMachineEnv machineEnv);

  void createServerContainer(DockerMachineEnv machineEnv, String imageId, Integer publishedPort);

  void rotateWorkerJoinToken(DockerMachineEnv machineEnv);
}
