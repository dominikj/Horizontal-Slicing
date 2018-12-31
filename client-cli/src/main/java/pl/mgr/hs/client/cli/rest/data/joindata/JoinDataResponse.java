package pl.mgr.hs.client.cli.rest.data.joindata;

import lombok.Data;
import lombok.NoArgsConstructor;

/** Created by dominik on 14.11.18. */
@NoArgsConstructor
@Data
public class JoinDataResponse {
  private JoinTokenData tokenDto;
  private String attachCommand;
}
