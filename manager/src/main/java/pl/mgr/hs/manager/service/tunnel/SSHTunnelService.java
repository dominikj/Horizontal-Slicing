package pl.mgr.hs.manager.service.tunnel;

import pl.mgr.hs.manager.entity.Slice;

/** Created by dominik on 02.01.19. */
public interface SSHTunnelService {
  void createTunnelForSlice(Slice slice);

  void removeTunnelForSlice(int sliceId);
}
