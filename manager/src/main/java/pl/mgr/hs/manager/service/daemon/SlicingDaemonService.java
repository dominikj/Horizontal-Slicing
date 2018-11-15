package pl.mgr.hs.manager.service.daemon;

/** Created by dominik on 07.11.18. */
public interface SlicingDaemonService {

  void registerSlice(Integer id);

  void removeSlice(Integer id);
}
