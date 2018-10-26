package pl.mgr.hs.manager.dto.details;

import lombok.Data;

import java.util.List;

/** Created by dominik on 25.10.18. */
@Data
public class ApplicationDto {
  private String image;
  private List<Integer> publishedPorts;
  private String status;
}
