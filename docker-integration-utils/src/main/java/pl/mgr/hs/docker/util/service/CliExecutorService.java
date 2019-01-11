package pl.mgr.hs.docker.util.service;

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
import pl.mgr.hs.docker.util.util.CollectingLogOutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/** Created by dominik on 27.10.18. */
public abstract class CliExecutorService {
  protected static final String SUDO_COMMAND = " sudo --validate --stdin";
  private static final Logger LOGGER = LoggerFactory.getLogger(CliExecutorService.class);

  @FunctionalInterface
  protected interface ResultCreator {
    CliExecutorService.Result create(List<String> commandOutput);
  }

  @Data
  @AllArgsConstructor(access = AccessLevel.PUBLIC)
  protected static class Result<T> {
    private T resultData;
    private boolean failure;
  }

  protected Result executeCommand(String command, ResultCreator resultCreator) {
    LOGGER.info("Executing command: {}....", command);
    CollectingLogOutputStream outputStream = new CollectingLogOutputStream();
    CommandLine commandline = CommandLine.parse(command);
    DefaultExecutor exec = new DefaultExecutor();
    PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
    exec.setStreamHandler(streamHandler);

    execute(exec, commandline);
    return resultCreator.create(outputStream.getLines());
  }

  protected void executeCommandInteractive(String command) {
    LOGGER.info("Executing command: {}....", command);
    CommandLine commandline = CommandLine.parse(command);
    DefaultExecutor exec = new DefaultExecutor();
    PumpStreamHandler streamHandler = new PumpStreamHandler(System.out, System.err, System.in);
    exec.setStreamHandler(streamHandler);

    execute(exec, commandline);
  }

  @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "Temporary unused")
  protected void setupSudoCredentials(String password) {
    LOGGER.info("Setup sudo credentials....");
    CollectingLogOutputStream outputStream = new CollectingLogOutputStream();
    CommandLine commandline = CommandLine.parse(SUDO_COMMAND);
    DefaultExecutor exec = new DefaultExecutor();
    ByteArrayInputStream inputStream = createInputStream(password);
    PumpStreamHandler streamHandler =
        new PumpStreamHandler(outputStream, outputStream, inputStream);
    streamHandler.setStopTimeout(1L);
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
}
