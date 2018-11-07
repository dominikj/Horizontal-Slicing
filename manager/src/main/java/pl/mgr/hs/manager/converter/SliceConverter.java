package pl.mgr.hs.manager.converter;

import pl.mgr.hs.docker.util.service.DockerMachineEnv;
import pl.mgr.hs.docker.util.service.remote.DockerIntegrationService;

/** Created by dominik on 07.11.18. */
public abstract class SliceConverter<D, E> implements GenericConverter<D, E> {

  protected String getJoinToken(DockerMachineEnv env) {
    return getDockerIntegrationService()
        .getSwarmConfiguration(env)
        .map(swarm -> swarm.joinTokens().worker())
        .orElse("");
  }

  protected abstract DockerIntegrationService getDockerIntegrationService();
}
