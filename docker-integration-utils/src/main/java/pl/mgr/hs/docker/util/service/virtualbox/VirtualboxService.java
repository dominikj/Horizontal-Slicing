package pl.mgr.hs.docker.util.service.virtualbox;

/** Created by dominik on 27.10.18. */
public interface VirtualboxService {

  void createBridgedAdapterToInterfaceForMachine(String machineName, String physicalInterface);
}
