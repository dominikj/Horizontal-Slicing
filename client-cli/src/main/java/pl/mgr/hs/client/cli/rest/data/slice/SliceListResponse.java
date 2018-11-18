package pl.mgr.hs.client.cli.rest.data.slice;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Created by dominik on 09.11.18. */
@Data
@NoArgsConstructor
public class SliceListResponse {
  private List<SliceData> slices;
}
