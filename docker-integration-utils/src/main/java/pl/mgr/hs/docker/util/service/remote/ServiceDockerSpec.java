package pl.mgr.hs.docker.util.service.remote;

import com.spotify.docker.client.messages.swarm.EndpointSpec;
import com.spotify.docker.client.messages.swarm.NetworkAttachmentConfig;
import com.spotify.docker.client.messages.swarm.PortConfig;
import com.spotify.docker.client.messages.swarm.RestartPolicy;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.mgr.hs.docker.util.util.CommandUtil;

import java.util.List;
import java.util.Map;

/** Created by dominik on 20.12.18. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ServiceDockerSpec {

  private String imageId;
  private String serviceName;
  private boolean createVirtualTerminal;
  private String restartPolicy = RestartPolicy.RESTART_POLICY_NONE;
  private String placementConstraint;
  private EndpointSpec endpointSpec;
  private NetworkAttachmentConfig networkAttachmentConfig;
  private List<String> command;

  public static Builder builder() {
    return new ServiceDockerSpec().new Builder();
  }

  public class Builder {
    private static final String EXCLUDE_MANAGER_PLACEMENT_CONSTRAINT = "node.role!=manager";
    private static final String EXCLUDE_WORKER_PLACEMENT_CONSTRAINT = "node.role!=worker";

    public Builder image(String imageId) {

      if (ServiceDockerSpec.this.imageId != null && ServiceDockerSpec.this.imageId.contains(":")) {
        ServiceDockerSpec.this.imageId = imageId + ServiceDockerSpec.this.imageId;
      } else {
        ServiceDockerSpec.this.imageId = imageId;
      }

      return this;
    }

    public Builder tag(String imageTag) {
      ServiceDockerSpec.this.imageId = ServiceDockerSpec.this.imageId + ":" + imageTag;
      return this;
    }

    public Builder name(String serviceName) {
      ServiceDockerSpec.this.serviceName = serviceName;
      return this;
    }

    public Builder command(String command, Map<String, String> replacements) {
      ServiceDockerSpec.this.command = CommandUtil.parseCommand(command, replacements);
      return this;
    }

    public Builder createVirtualTerminal() {
      ServiceDockerSpec.this.createVirtualTerminal = true;
      return this;
    }

    public Builder restartOnFailure() {
      ServiceDockerSpec.this.restartPolicy = RestartPolicy.RESTART_POLICY_ON_FAILURE;
      return this;
    }

    public Builder excludeManagersFromPlacement() {
      ServiceDockerSpec.this.placementConstraint = EXCLUDE_MANAGER_PLACEMENT_CONSTRAINT;
      return this;
    }

    public Builder excludeWorkersFromPlacement() {
      ServiceDockerSpec.this.placementConstraint = EXCLUDE_WORKER_PLACEMENT_CONSTRAINT;
      return this;
    }

    public Builder publishPort(Integer port) {
      if (port == null) {
        return this;
      }
      EndpointSpec.Builder builder = EndpointSpec.builder();

      builder.addPort(
          PortConfig.builder()
              .publishedPort(port)
              .targetPort(port)
              .protocol(PortConfig.PROTOCOL_TCP)
              .build());

      ServiceDockerSpec.this.endpointSpec = builder.build();
      return this;
    }

    public Builder attachNetwork(String networkAlias) {
      ServiceDockerSpec.this.networkAttachmentConfig =
          NetworkAttachmentConfig.builder().target(networkAlias).build();
      return this;
    }

    public ServiceDockerSpec build() {
      return ServiceDockerSpec.this;
    }
  }
}
