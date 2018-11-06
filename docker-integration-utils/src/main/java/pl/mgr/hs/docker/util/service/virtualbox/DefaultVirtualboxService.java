package pl.mgr.hs.docker.util.service.virtualbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.mgr.hs.docker.util.exception.VirtualboxOperationException;
import pl.mgr.hs.docker.util.service.CliExecutorService;

import java.util.List;

/** Created by dominik on 27.10.18. */
public class DefaultVirtualboxService extends CliExecutorService implements VirtualboxService {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultVirtualboxService.class);

  private static final String SET_BRIDGED_ADAPTER_TO_WLAN_COMMAND =
      "VBoxManage modifyvm %s --nic3 bridged --nictype3 82545EM --bridgeadapter3 wlan0";

  @Override
  public void createBridgedAdapterForMachine(String machineName) {
    Result result =
        executeCommand(
            String.format(SET_BRIDGED_ADAPTER_TO_WLAN_COMMAND, machineName),
            this::createResultForSetBridgedAdapter);

    if (result.isFailure()) {
      throw new VirtualboxOperationException(
          String.format("Cannot set bridged adapter for machine: %s", machineName));
    }
  }

  private Result createResultForSetBridgedAdapter(List<String> commandOutput) {
    if (commandOutput.isEmpty()) {
      LOGGER.info(String.join("\n", commandOutput));
      return new Result<>(null, false);
    }
    return new Result<>(null, true);
  }
}
