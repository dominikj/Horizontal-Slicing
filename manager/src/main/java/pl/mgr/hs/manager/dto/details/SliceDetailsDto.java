package pl.mgr.hs.manager.dto.details;

import lombok.Data;

import java.util.List;

/** Created by dominik on 24.10.18. */
@Data
public class SliceDetailsDto {
  private Integer id;
  private String name;
  private boolean working;
  private String managerHostName;
  private String managerHostAddressInternal;
  private String managerHostAddressExternal;
  private String serviceName;

  private ApplicationDto clientApplication;
  private ApplicationDto serverApplication;
  private String joinToken;
  private List<HostDto> hosts;
}
