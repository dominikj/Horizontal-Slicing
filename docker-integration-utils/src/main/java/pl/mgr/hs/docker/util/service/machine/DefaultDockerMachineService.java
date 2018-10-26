package pl.mgr.hs.docker.util.service.machine;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.mgr.hs.docker.util.enums.DockerMachineStatus;
import pl.mgr.hs.docker.util.exception.DockerOperationException;
import pl.mgr.hs.docker.util.service.DockerMachineEnv;
import pl.mgr.hs.docker.util.util.CollectingLogOutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;

/** Created by dominik on 20.10.18. */
public class DefaultDockerMachineService implements DockerMachineService {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDockerMachineService.class);

  private static final String SUDO_COMMAND = " sudo --validate --stdin";
  private static final String CHECK_STATE_COMMAND = "docker-machine ls --filter name=%s";
  private static final String REMOVE_MACHINE_COMMAND = "docker-machine rm -y %s";
  private static final String RESTART_MACHINE_COMMAND = "docker-machine restart -y %s";
  private static final String GET_ENV_COMMAND = "docker-machine env %s";
  private static final int STATUS_INDEX = 3;
  private static final String WHITESPACES_GROUP = "\\s+";
  private static final int LINE_WITH_RECORD = 1;
  private static final int NUMBER_OF_LINES_IN_PRORER_ENV_COMMAND_OUTPUT = 6;
  private static final int DOCKER_PROPERTY_VALUE = 1;
  private static final int HOST_ADDRESS = 1;
  private static final int CERT_PATH = 2;
  private static final String SUCCESSFULLY_REMOVED_MSG = "Successfully removed \\S+";
  private static final String SUCCESSFULLY_RESTARTED_MSG = "Restarted machines \\S+";
  private static final int SUCCESS_REMOVE_MACHINE_OUTPUT_SIZE = 2;
  private static final int SUCCESS_RESTART_MACHINE_OUTPUT_SIZE = 6;
  private final String sudoPassword;

  public DefaultDockerMachineService(String sudoPassword) {
    this.sudoPassword = sudoPassword;
  }

  @FunctionalInterface
  private interface ResultCreator {
    Result create(List<String> commandOutput);
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

  private Result executeCommand(String command, ResultCreator resultCreator) {
    LOGGER.info("Executing command: {}....", command);
    CollectingLogOutputStream outputStream = new CollectingLogOutputStream();
    CommandLine commandline = CommandLine.parse(command);
    DefaultExecutor exec = new DefaultExecutor();
    PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
    exec.setStreamHandler(streamHandler);
    execute(exec, commandline);
    Result result = resultCreator.create(outputStream.getLines());
    LOGGER.info("Result of execution: {}", result.getResultData());
    return result;
  }

  @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "Temporary unused")
  private void setupSudoCredentials(String password) {
    LOGGER.info("Setup sudo credentials....");
    CollectingLogOutputStream outputStream = new CollectingLogOutputStream();
    CommandLine commandline = CommandLine.parse(SUDO_COMMAND);
    DefaultExecutor exec = new DefaultExecutor();
    ByteArrayInputStream inputStream = createInputStream(password);
    PumpStreamHandler streamHandler =
        new PumpStreamHandler(outputStream, outputStream, inputStream);
    exec.setStreamHandler(streamHandler);
    execute(exec, commandline);
  }

  private ByteArrayInputStream createInputStream(String inputData) {
    try {
      return new ByteArrayInputStream((inputData + "\n").getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      LOGGER.error("Unsupported encoding is used");
      return null;
    }
  }

  // https://issues.apache.org/jira/browse/EXEC-101
  private void execute(DefaultExecutor executor, CommandLine commandLine) {
    try {
      executor.execute(commandLine);
    } catch (ExecuteException ex) {
      LOGGER.info(ex.getMessage());
    } catch (IOException ex) {
      LOGGER.debug("Known bug in Apache Commons Exec. I will try again", ex);
      execute(executor, commandLine);
    }
  }

  private Result<String> createResultForMachineListSearch(List<String> commandOutput) {
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
    if (commandOutput.size() == SUCCESS_REMOVE_MACHINE_OUTPUT_SIZE
        && commandOutput.get(1).matches(SUCCESSFULLY_REMOVED_MSG)) {
      return new Result(null, false);
    }
    return new Result(null, true);
  }

  private Result createResultForRestartMachine(List<String> commandOutput) {
    if (commandOutput.size() == SUCCESS_RESTART_MACHINE_OUTPUT_SIZE
        && commandOutput.get(5).matches(SUCCESSFULLY_RESTARTED_MSG)) {
      return new Result(null, false);
    }
    return new Result(null, true);
  }

  @Data
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  private static class Result<T> {
    private T resultData;
    private boolean failure;
  }
}
