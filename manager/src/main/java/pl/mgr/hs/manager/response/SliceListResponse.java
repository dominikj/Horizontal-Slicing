package pl.mgr.hs.manager.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import pl.mgr.hs.manager.dto.rest.SliceDto;

import java.util.List;

/** Created by dominik on 09.11.18. */
@Data
@AllArgsConstructor
public class SliceListResponse {
  private List<SliceDto> slices;
}
