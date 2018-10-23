package pl.mgr.hs.manager.service.docker;

import lombok.Data;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.mgr.hs.manager.enums.DockerMachineStatus;
import pl.mgr.hs.manager.util.CollectingLogOutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by dominik on 20.10.18.
 */
@Service
public class DefaultDockerMachineService implements DockerMachineService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDockerMachineService.class);

    private static final String SUDO_COMMAND = " sudo --validate --stdin";
    private static final String CHECK_STATE_COMMAND = "sudo docker-machine ls --filter name=%s";
    private static final int STATUS_INDEX = 3;
    private static final String WHITESPACES_GROUP = "\\s+";
    private static final int LINE_WITH_RECORD = 1;

    private final String sudoPassword;

    public DefaultDockerMachineService(@Value("${local.sudo.password}") String sudoPassword) {
        this.sudoPassword = sudoPassword;
    }

    @Override
    public DockerMachineStatus getMachineStatus(String name) {
        try {
            setupSudoCredentials(sudoPassword);
            Result result = executeCommand(String.format(CHECK_STATE_COMMAND, name));

            if (result.isEmpty()) {
                return DockerMachineStatus.Unknown;
            }

            return DockerMachineStatus.valueOf(getMachineStatusFromResult(result.getResultData()));
        } catch (Exception e) {
            LOGGER.error("Exception occured during command execution: {}", e);
            return DockerMachineStatus.Unknown;
        }
    }

    @Override
    public String executeSSHCommandOnMachine(String machineName, String command, String grepRegex) {
        return null;
    }

    private Result executeCommand(String command) {
        LOGGER.info("Executing command: {}....", command);
        CollectingLogOutputStream outputStream = new CollectingLogOutputStream();
        CommandLine commandline = CommandLine.parse(command);
        DefaultExecutor exec = new DefaultExecutor();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        exec.setStreamHandler(streamHandler);
        execute(exec, commandline);
        Result result = createResultForMachineListSearch(outputStream.getLines());
        LOGGER.info("Result of execution: {}", result.getResultData());
        return result;
    }

    private void setupSudoCredentials(String password) {
        LOGGER.info("Setup sudo credentials....");
        CollectingLogOutputStream outputStream = new CollectingLogOutputStream();
        CommandLine commandline = CommandLine.parse(SUDO_COMMAND);
        DefaultExecutor exec = new DefaultExecutor();
        ByteArrayInputStream inputStream = createInputStream(password);
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, outputStream, inputStream);
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

    //https://issues.apache.org/jira/browse/EXEC-101
    private void execute(DefaultExecutor executor, CommandLine commandLine) {
        try {
            executor.execute(commandLine);
        } catch (IOException ex) {
            LOGGER.debug("Known bug in Apache Commons Exec. I will try again");
            execute(executor, commandLine);
        }
    }

    private Result createResultForMachineListSearch(List<String> lines) {
        if (isOnlyHeaderLine(lines.size())) {
            return new Result(null, true);
        }
        return new Result(lines.get(LINE_WITH_RECORD), false);
    }

    private boolean isOnlyHeaderLine(int size) {
        return size <= 1;
    }

    private String getMachineStatusFromResult(String result) {
        return result.trim().split(WHITESPACES_GROUP)[STATUS_INDEX];
    }

    @Data
    private static class Result {
        private Result(String resultData, boolean empty) {
            this.resultData = resultData;
            this.empty = empty;
        }

        private String resultData;
        private boolean empty;
    }
}
