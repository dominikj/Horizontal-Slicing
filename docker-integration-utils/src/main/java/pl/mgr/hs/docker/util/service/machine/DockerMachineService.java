package pl.mgr.hs.docker.util.service.machine;

import pl.mgr.hs.docker.util.enums.DockerMachineStatus;
import pl.mgr.hs.docker.util.service.DockerMachineEnv;

/** Created by dominik on 20.10.18. */
public interface DockerMachineService {

  DockerMachineStatus getMachineStatus(String name);

  DockerMachineEnv getMachineEnv(String name);

  void removeMachine(String name);

  void restartMachine(String name);

  void regenerateCertsForMachine(String name);

  void createNewMachine(String name);

  void createNewMachine(String name, String mirrorAddress);

  void createNewMachine(String name, String mirrorAddress, String insecureRegistryAddress);

  void stopMachine(String name);

  String getExternalIpAddress(String name);
}
