package pl.mgr.hs.manager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import pl.mgr.hs.docker.util.enums.DockerMachineStatus;
import pl.mgr.hs.docker.util.service.DockerMachineEnv;
import pl.mgr.hs.docker.util.service.machine.DockerMachineService;

/** Created by dominik on 12.01.19. */
@Service
public class DefaultDockerMachineCacheableService implements DockerMachineCacheableService {

  private static final String MACHINE_ENV_CACHE = "machineEnvs";
  private static final String EXTERNAL_IP_ADDRESS_CACHE = "externalIpAddresses";

  private final DockerMachineService dockerMachineService;
  private final CacheManager cacheManager;

  @Autowired
  public DefaultDockerMachineCacheableService(
      DockerMachineService dockerMachineService, CacheManager cacheManager) {
    this.dockerMachineService = dockerMachineService;
    this.cacheManager = cacheManager;
  }

  @Override
  public DockerMachineStatus getMachineStatus(String name) {
    return dockerMachineService.getMachineStatus(name);
  }

  @Override
  @Cacheable(value = MACHINE_ENV_CACHE)
  public DockerMachineEnv getMachineEnv(String name) {
    return dockerMachineService.getMachineEnv(name);
  }

  @Override
  @Cacheable(value = EXTERNAL_IP_ADDRESS_CACHE)
  public String getExternalIpAddress(String name) {
    return dockerMachineService.getExternalIpAddress(name);
  }

  @Override
  public void stopMachine(String name) {
    dockerMachineService.stopMachine(name);
    evictCaches(name);
  }

  @Override
  public void removeMachine(String name) {
    dockerMachineService.removeMachine(name);
    evictCaches(name);
  }

  @Override
  public void restartMachine(String name) {
    dockerMachineService.restartMachine(name);
    evictCaches(name);
  }

  @Override
  public void regenerateCertsForMachine(String name) {
    dockerMachineService.regenerateCertsForMachine(name);
    cacheManager.getCache(MACHINE_ENV_CACHE).evict(name);
  }

  @Override
  public void createNewMachine(String name) {
    dockerMachineService.createNewMachine(name);
  }

  @Override
  public void createNewMachine(String name, String mirrorAddress, String insecureRegistryAddress) {
    dockerMachineService.createNewMachine(name, mirrorAddress, insecureRegistryAddress);
  }

  private void evictCaches(String name) {
    cacheManager.getCache(MACHINE_ENV_CACHE).evict(name);
    cacheManager.getCache(EXTERNAL_IP_ADDRESS_CACHE).evict(name);
  }
}
