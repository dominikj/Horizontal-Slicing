package pl.mgr.hs.manager.service.docker;

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

/**
 * Created by dominik on 20.10.18.
 */
@Service
public class DefaultDockerMachineService implements DockerMachineService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDockerMachineService.class);

    private static final String SUDO_COMMAND = " sudo --validate --stdin";
    private static final String CHECK_STATE_COMMAND = "sudo docker-machine ls --filter name=%s";

    private final String sudoPassword;

    public DefaultDockerMachineService(@Value("${local.sudo.password}") String sudoPassword) {
        this.sudoPassword = sudoPassword;
    }

    @Override
    public DockerMachineStatus getMachineStatus(String name) {
        try {
            setupSudoCredentials(sudoPassword);
            return DockerMachineStatus.valueOf(executeCommand(String.format(CHECK_STATE_COMMAND, name)));
        } catch (Exception e) {
            LOGGER.error("Exception occured during command execution: {}", e);
            return DockerMachineStatus.Unknown;
        }
    }

    @Override
    public String executeSSHCommandOnMachine(String machineName, String command, String grepRegex) {
        return null;
    }

    private String executeCommand(String command) throws Exception {
        LOGGER.info("Executing command: {}....", command);
        CollectingLogOutputStream outputStream = new CollectingLogOutputStream();
        CommandLine commandline = CommandLine.parse(command);
        DefaultExecutor exec = new DefaultExecutor();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        exec.setStreamHandler(streamHandler);
        exec.execute(commandline);
        String result = outputStream.getLines().get(0);
        LOGGER.info("Result of execution: {}", result);
        return result;
    }

    private String setupSudoCredentials(String password) throws IOException {
        LOGGER.info("Setup sudo credentials....");
        CollectingLogOutputStream outputStream = new CollectingLogOutputStream();
        CommandLine commandline = CommandLine.parse(SUDO_COMMAND);
        DefaultExecutor exec = new DefaultExecutor();
        ByteArrayInputStream inputStream = new ByteArrayInputStream((password + "\n").getBytes("UTF-8"));
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, outputStream, inputStream);
        exec.setStreamHandler(streamHandler);
        exec.execute(commandline);
        inputStream.close();
        return "OK";
    }
}
