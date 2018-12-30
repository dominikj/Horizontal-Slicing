package pl.mgr.hs.docker.util.service.remote;

import com.spotify.docker.client.messages.PortBinding;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.mgr.hs.docker.util.util.CommandUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Created by dominik on 20.12.18. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ContainerDockerSpec {

  private String imageId;
  private String name;
  private List<String> command;
  private Map<String, List<PortBinding>> portBindings;

  public String getPublishedPort() {
    if (portBindings != null && portBindings.size() != 0) {
      return portBindings.values().iterator().next().get(0).hostPort();
    }
    return null;
  }

  public static Builder builder() {
    return new ContainerDockerSpec().new Builder();
  }

  public class Builder {

    private static final String LATEST_VERSION = "latest";
    private static final String DEFAULT_IP = "0.0.0.0";

    public Builder image(String imageId) {

      if (ContainerDockerSpec.this.imageId != null
          && ContainerDockerSpec.this.imageId.contains(":")) {
        ContainerDockerSpec.this.imageId = imageId + ContainerDockerSpec.this.imageId;
      } else {
        ContainerDockerSpec.this.imageId = imageId + ":" + LATEST_VERSION;
      }

      return this;
    }

    public Builder tag(String imageTag) {
      ContainerDockerSpec.this.imageId = ContainerDockerSpec.this.imageId + ":" + imageTag;
      return this;
    }

    public Builder name(String containerName) {
      ContainerDockerSpec.this.name = containerName;
      return this;
    }

    public Builder command(String command, Map<String, String> replacements) {
      ContainerDockerSpec.this.command = CommandUtil.parseCommand(command, replacements);
      return this;
    }

    public Builder publishPort(int port) {
      List<PortBinding> hostPorts =
          Collections.singletonList(PortBinding.of(DEFAULT_IP, String.valueOf(port)));
      portBindings = Collections.singletonMap(String.valueOf(port), hostPorts);
      return this;
    }

    public ContainerDockerSpec build() {
      return ContainerDockerSpec.this;
    }
  }
}
