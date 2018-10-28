package pl.mgr.hs.docker.util.exception;

/** Created by dominik on 27.10.18. */
public class VirtualboxOperationException extends RuntimeException {
  public VirtualboxOperationException() {}

  public VirtualboxOperationException(String message) {
    super(message);
  }

  public VirtualboxOperationException(String message, Throwable cause) {
    super(message, cause);
  }

  public VirtualboxOperationException(Throwable cause) {
    super(cause);
  }
}
