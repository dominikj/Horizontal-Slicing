package pl.mgr.hs.manager.dto.web.details;

import lombok.Data;

/** Created by dominik on 24.10.18. */
@Data
public class HostDto {
  private String address;
  private String name;
  private String replicationStatus;
  private String replicationInfo;
  private String state;
}
