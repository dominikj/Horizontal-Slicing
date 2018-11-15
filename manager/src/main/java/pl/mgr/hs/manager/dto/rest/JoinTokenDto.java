package pl.mgr.hs.manager.dto.rest;

import lombok.Data;

/** Created by dominik on 14.11.18. */
@Data
public class JoinTokenDto {
  private String token;
  private String ipAddress;
  private Integer port;
}
