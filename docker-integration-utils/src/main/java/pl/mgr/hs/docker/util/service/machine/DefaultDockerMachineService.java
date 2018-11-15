package pl.mgr.hs.docker.util.service.machine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.mgr.hs.docker.util.enums.DockerMachineStatus;
import pl.mgr.hs.docker.util.exception.DockerOperationException;
import pl.mgr.hs.docker.util.service.CliExecutorService;
import pl.mgr.hs.docker.util.service.DockerMachineEnv;

import java.net.URI;
import java.nio.file.Paths;
import java.util.List;

/** Created by dominik on 20.10.18. */
public class DefaultDockerMachineService extends CliExecutorService
    implements DockerMachineService {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDockerMachineService.class);

  private static final String CHECK_STATE_COMMAND = "docker-machine ls --filter name=%s";
  private static final String REMOVE_MACHINE_COMMAND = "docker-machine rm -y %s";
  private static final String RESTART_MACHINE_COMMAND = "docker-machine restart %s";
  private static final String STOP_MACHINE_COMMAND = "docker-machine stop %s";
  private static final String REGENERATE_CERTS_COMMAND = "docker-machine regenerate-certs -f %s";
  private static final String GET_EXTERNAL_IP_COMMAND = "docker-machine ssh %s ifconfig eth2";
  // FIXME
  private static final String BOOT2DOCKER_IMAGE_URL = "/opt/docker-images/boot2docker.iso";
  private static final String CREATE_MACHINE_COMMAND =
      "docker-machine create -d virtualbox --virtualbox-boot2docker-url %s %s";
  private static final String GET_ENV_COMMAND = "docker-machine env %s";
  private static final int STATUS_INDEX = 3;
  private static final String WHITESPACES_GROUP = "\\s+";
  private static final int LINE_WITH_RECORD = 1;
  private static final int NUMBER_OF_LINES_IN_PRORER_ENV_COMMAND_OUTPUT = 6;
  private static final int DOCKER_PROPERTY_VALUE = 1;
  private static final int HOST_ADDRESS = 1;
  private static final int CERT_PATH = 2;
  private static final String SUCCESSFULLY_REMOVED_MSG = "Successfully removed \\S+";
  private static final String SUCCESSFULLY_RESTARTED_MSG = "^Restarted machines .+";
  private static final String SUCCESSFULLY_STOPPED_MSG = ".+ was stopped.$";
  private static final int SUCCESS_REMOVE_MACHINE_OUTPUT_SIZE = 3;
  private static final int SUCCESS_REGENERATE_CERTS_OUTPUT_SIZE = 6;
  private static final String DOCKER_IS_UP_AND_RUNNING_MSG = "Docker is up and running!";
  private static final int SUCCESS_STOP_MACHINE_OUTPUT_SIZE = 2;
  private static final int SUCCESS_GET_EXTERNAL_IP_ADDRESS_OUTPUT_SIZE = 9;
  private static final String SUCCESS_GET_EXTERNAL_IP = "^.+:\\d+.\\d+.\\d+.\\d+.+";
  private static final int IP_ADDRESS = 2;
  private final String sudoPassword;

  public DefaultDockerMachineService(String sudoPassword) {
    this.sudoPassword = sudoPassword;
  }

  @Override
  public DockerMachineStatus getMachineStatus(String name) {
    try {
      //            Temporary disabled
      //            setupSudoCredentials(sudoPassword);
      Result result =
          executeCommand(
              String.format(CHECK_STATE_COMMAND, name), this::createResultForMachineListSearch);

      if (result.isFailure()) {
        return DockerMachineStatus.Unknown;
      }

      return DockerMachineStatus.valueOf(
          getMachineStatusFromResult(String.valueOf(result.getResultData())));
    } catch (Exception e) {
      LOGGER.error("Exception occured during command execution: {}", e);
      return DockerMachineStatus.Unknown;
    }
  }

  @Override
  public DockerMachineEnv getMachineEnv(String name) {
    Result<DockerMachineEnv> result =
        executeCommand(String.format(GET_ENV_COMMAND, name), this::createResultForGetMachineEnv);
    if (result.isFailure()) {
      throw new DockerOperationException(
          String.format("No env configuration for machine: %s", name));
    }
    return result.getResultData();
  }

  @Override
  public void removeMachine(String name) {
    Result result =
        executeCommand(
            String.format(REMOVE_MACHINE_COMMAND, name), this::createResultForRemoveMachine);

    if (result.isFailure()) {
      throw new DockerOperationException(String.format("Cannot remove machine: %s", name));
    }
  }

  @Override
  public void restartMachine(String name) {
    Result result =
        executeCommand(
            String.format(RESTART_MACHINE_COMMAND, name), this::createResultForRestartMachine);

    if (result.isFailure()) {
      throw new DockerOperationException(String.format("Cannot restart machine: %s", name));
    }
  }

  @Override
  public void regenerateCertsForMachine(String name) {
    Result result =
        executeCommand(
            String.format(REGENERATE_CERTS_COMMAND, name), this::createResultForRegenerateCerts);

    if (result.isFailure()) {
      throw new DockerOperationException(
          String.format("Cannot regenerate certs for machine: %s", name));
    }
  }

  @Override
  public void createNewMachine(String name) {
    Result result =
        executeCommand(
            String.format(CREATE_MACHINE_COMMAND, BOOT2DOCKER_IMAGE_URL, name),
            this::createResultForCreateMachine);

    if (result.isFailure()) {
      throw new DockerOperationException(String.format("Cannot create machine: %s", name));
    }
  }

  @Override
  public void stopMachine(String name) {
    Result result =
        executeCommand(String.format(STOP_MACHINE_COMMAND, name), this::createResultForStopMachine);

    if (result.isFailure()) {
      throw new DockerOperationException(String.format("Cannot create machine: %s", name));
    }
  }

  @Override
  public String getExternalIpAddress(String name) {
    Result<String> result =
        executeCommand(
            String.format(GET_EXTERNAL_IP_COMMAND, name),
            this::createResultForGetExternalIpAddress);

    if (result.isFailure()) {
      throw new DockerOperationException(
          String.format("Cannot obtain external ip address for machine: %s", name));
    }
    return result.getResultData();
  }

  private Result<String> createResultForMachineListSearch(List<String> commandOutput) {
    LOGGER.debug(String.join("\n", commandOutput));

    if (isOnlyHeaderLine(commandOutput.size())) {
      return new Result<>(null, true);
    }
    return new Result<>(commandOutput.get(LINE_WITH_RECORD), false);
  }

  private boolean isOnlyHeaderLine(int size) {
    return size <= 1;
  }

  private String getMachineStatusFromResult(String result) {
    return result.trim().split(WHITESPACES_GROUP)[STATUS_INDEX];
  }

  private Result<DockerMachineEnv> createResultForGetMachineEnv(List<String> commandOutput) {

    if (commandOutput.size() != NUMBER_OF_LINES_IN_PRORER_ENV_COMMAND_OUTPUT) {
      LOGGER.error(String.join("\n", commandOutput));
      return new Result<>(null, true);
    }

    String dockerHost =
        commandOutput.get(HOST_ADDRESS).split("=")[DOCKER_PROPERTY_VALUE].replace("\"", "");
    String certPath =
        commandOutput.get(CERT_PATH).split("=")[DOCKER_PROPERTY_VALUE].replace("\"", "");

    DockerMachineEnv machineEnv =
        new DockerMachineEnv(URI.create(dockerHost.replace("tcp", "https")), Paths.get(certPath));

    return new Result<>(machineEnv, false);
  }

  private Result createResultForRemoveMachine(List<String> commandOutput) {
    LOGGER.debug(String.join("\n", commandOutput));

    if (commandOutput.size() == SUCCESS_REMOVE_MACHINE_OUTPUT_SIZE
        && commandOutput.get(2).matches(SUCCESSFULLY_REMOVED_MSG)) {
      return new Result<>(null, false);
    }
    return new Result<>(null, true);
  }

  private Result createResultForRestartMachine(List<String> commandOutput) {
    LOGGER.debug(String.join("\n", commandOutput));

    if (commandOutput.stream().anyMatch(line -> line.matches(SUCCESSFULLY_RESTARTED_MSG))) {
      return new Result<>(null, false);
    }
    return new Result<>(null, true);
  }

  private Result createResultForRegenerateCerts(List<String> commandOutput) {
    LOGGER.debug(String.join("\n", commandOutput));

    if (commandOutput.size() == SUCCESS_REGENERATE_CERTS_OUTPUT_SIZE) {
      return new Result<>(null, false);
    }
    return new Result<>(null, true);
  }

  private Result createResultForCreateMachine(List<String> commandOutput) {
    LOGGER.debug(String.join("\n", commandOutput));

    if (commandOutput.stream().anyMatch(DOCKER_IS_UP_AND_RUNNING_MSG::equals)) {
      return new Result<>(null, false);
    }
    return new Result<>(null, true);
  }

  private Result createResultForStopMachine(List<String> commandOutput) {
    LOGGER.debug(String.join("\n", commandOutput));

    if (commandOutput.size() == SUCCESS_STOP_MACHINE_OUTPUT_SIZE
        && commandOutput.get(1).matches(SUCCESSFULLY_STOPPED_MSG)) {
      return new Result<>(null, false);
    }
    return new Result<>(null, true);
  }

  private Result createResultForGetExternalIpAddress(List<String> commandOutput) {
    LOGGER.debug(String.join("\n", commandOutput));

    if (commandOutput.size() == SUCCESS_GET_EXTERNAL_IP_ADDRESS_OUTPUT_SIZE
        && commandOutput.get(1).matches(SUCCESS_GET_EXTERNAL_IP)) {

      String ip = commandOutput.get(1).split(WHITESPACES_GROUP)[IP_ADDRESS].split(":")[1];
      return new Result<>(ip, false);
    }
    return new Result<>(null, true);
  }
}
