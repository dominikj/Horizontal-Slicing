package pl.mgr.hs.docker.util.service.dockercli;

import pl.mgr.hs.docker.util.service.CliExecutorService;

/** Created by dominik on 20.11.18. */
public class DefaultDockerCliService extends CliExecutorService implements DockerCliService {
  //TODO
  private static final String DOCKER_EXEC_COMMAND = "docker exec -it %s";

  @Override
  public void attachToContainer(String containerId) {
    executeCommandInteractive(String.format(DOCKER_EXEC_COMMAND, containerId));
  }
}
