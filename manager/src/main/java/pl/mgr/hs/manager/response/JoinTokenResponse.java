package pl.mgr.hs.manager.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import pl.mgr.hs.manager.dto.rest.JoinTokenDto;

/** Created by dominik on 14.11.18. */
@AllArgsConstructor
@Data
public class JoinTokenResponse {
  private JoinTokenDto tokenDto;
}
