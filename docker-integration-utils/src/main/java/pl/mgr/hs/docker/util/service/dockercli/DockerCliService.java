package pl.mgr.hs.docker.util.service.dockercli;

/** Created by dominik on 20.11.18. */
public interface DockerCliService {
  void attachToContainer(String containerId);
}
