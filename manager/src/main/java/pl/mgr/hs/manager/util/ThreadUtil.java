package pl.mgr.hs.manager.util;

/** Created by dominik on 02.01.19. */
public class ThreadUtil {
  public static void sleep(long millis) {
    try {
      Thread.sleep(millis);

    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted thread", e);
    }
  }
}
