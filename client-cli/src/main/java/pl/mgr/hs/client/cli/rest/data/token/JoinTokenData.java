package pl.mgr.hs.client.cli.rest.data.token;

import lombok.Data;

/** Created by dominik on 14.11.18. */
@Data
public class JoinTokenData {
  private String token;
  private String ipAddress;
  private Integer port;
}
