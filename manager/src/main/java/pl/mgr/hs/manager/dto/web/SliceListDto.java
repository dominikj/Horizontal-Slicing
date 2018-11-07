package pl.mgr.hs.manager.dto.web;

import lombok.Data;

/** Created by dominik on 20.10.18. */
@Data
public class SliceListDto {
  private Integer id;
  private String name;
  private boolean working;
  private long activeHosts;
  private String ipAddress;
}
