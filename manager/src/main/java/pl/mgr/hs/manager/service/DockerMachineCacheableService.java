package pl.mgr.hs.manager.service;

import pl.mgr.hs.docker.util.enums.DockerMachineStatus;
import pl.mgr.hs.docker.util.service.DockerMachineEnv;

/** Created by dominik on 12.01.19. */
public interface DockerMachineCacheableService {

  DockerMachineStatus getMachineStatus(String name);

  DockerMachineEnv getMachineEnv(String name);

  void removeMachine(String name);

  void restartMachine(String name);

  void regenerateCertsForMachine(String name);

  void createNewMachine(String name);

  void createNewMachine(String name, String mirrorAddress, String insecureRegistryAddress);

  void stopMachine(String name);

  String getExternalIpAddress(String name);
}
