package pl.mgr.hs.client.cli;

import com.spotify.docker.client.messages.Container;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyInvocation.Builder;
import org.glassfish.jersey.client.JerseyWebTarget;
import pl.mgr.hs.client.cli.rest.data.command.AttachCommandResponse;
import pl.mgr.hs.client.cli.rest.data.slice.SliceData;
import pl.mgr.hs.client.cli.rest.data.slice.SliceListResponse;
import pl.mgr.hs.client.cli.rest.data.token.JoinTokenData;
import pl.mgr.hs.client.cli.rest.data.token.JoinTokenResponse;
import pl.mgr.hs.docker.util.exception.DockerOperationException;
import pl.mgr.hs.docker.util.service.dockercli.DefaultDockerCliService;
import pl.mgr.hs.docker.util.service.dockercli.DockerCliService;
import pl.mgr.hs.docker.util.service.remote.DefaultDockerIntegrationService;
import pl.mgr.hs.docker.util.service.remote.DockerIntegrationService;

import javax.ws.rs.core.MediaType;
import java.util.Optional;

/** Created by dominik on 17.11.18. */
public class SliceService {
  private final DockerIntegrationService integrationService = new DefaultDockerIntegrationService();
  private final DockerCliService cliService = new DefaultDockerCliService();

  private static final String AVAILABLE_SLICES_URL = "http://%s/rest/slice/available?hostId=%s";
  private static final String JOIN_TOKEN_URL =
      "http://%s/rest/slice/join-token?hostId=%s&sliceId=%s";
  private static final String ATTACH_COMMAND_URL =
      "http://%s/rest/slice/attach-command?hostId=%s&sliceId=%s";

  public SliceListResponse getAvailableSlicesForHost(String hostId, String hostAddress) {
    return request(
        SliceListResponse.class, String.format(AVAILABLE_SLICES_URL, hostAddress, hostId));
  }

  public void joinToSlice(String sliceName, String hostName, String hostAddress) {

    if (!joinToSliceInternal(sliceName, hostName, hostAddress)) {
      if (!joinToSliceInternal(sliceName, hostName, hostAddress)) {
        System.out.println("Cannot connect to slice: " + sliceName);
        return;
      }
    }
    System.out.println("Connected to slice: " + sliceName);
  }

  public void disconnectFromSlice() {
    integrationService.leaveSwarm();
    System.out.println("Disconnected from slice");
  }

  public void attachToSliceApp(String sliceName, String hostId, String hostAddress) {
    Optional<Integer> sliceId = getSliceId(hostId, hostAddress, sliceName);

    if (sliceId.isPresent()) {
      AttachCommandResponse response =
          request(
              AttachCommandResponse.class,
              String.format(ATTACH_COMMAND_URL, hostAddress, hostId, sliceId.get()));

      Container container =
          integrationService
              .getClientAppContainer()
              .orElseThrow(() -> new RuntimeException("Cannot get client app container"));
      cliService.startContainerInteractive(container.id(), response.getAttachCommand());
      return;
    }
    System.out.println("Cannot attach to application");
  }

  private boolean joinToSliceInternal(String sliceName, String hostName, String hostAddress) {
    Optional<JoinTokenResponse> tokenResponse =
        getJoinTokenForSlice(sliceName, hostName, hostAddress);

    if (!tokenResponse.isPresent()) {
      return false;
    }

    JoinTokenData tokenData = tokenResponse.get().getTokenDto();

    try {
      integrationService.joinSwarm(
          tokenData.getToken(), tokenData.getIpAddress() + ":" + tokenData.getPort());
    } catch (DockerOperationException e) {
      return false;
    }

    return true;
  }

  private Optional<JoinTokenResponse> getJoinTokenForSlice(
      String sliceName, String hostName, String hostAddress) {

    return getSliceId(hostName, hostAddress, sliceName)
        .map(
            integer ->
                request(
                    JoinTokenResponse.class,
                    String.format(JOIN_TOKEN_URL, hostAddress, hostName, integer)));
  }

  private Optional<Integer> getSliceId(String hostName, String hostAddress, String sliceName) {
    SliceListResponse list =
        request(
            SliceListResponse.class, String.format(AVAILABLE_SLICES_URL, hostAddress, hostName));

    return list.getSlices()
        .stream()
        .filter(sliceData -> sliceName.equals(sliceData.getName()))
        .findFirst()
        .map(SliceData::getId);
  }

  private <T> T request(Class<T> responseMappingClass, String url) {
    JerseyClient client = JerseyClientBuilder.createClient();
    JerseyWebTarget target = client.target(url);
    Builder request = target.request();
    request.accept(MediaType.APPLICATION_JSON);
    return request.get(responseMappingClass);
  }
}
