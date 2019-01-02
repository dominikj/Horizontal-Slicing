package pl.mgr.hs.docker.util.exception;

/** Created by dominik on 01.01.19. */
public class SSHOpertationException extends RuntimeException {
  public SSHOpertationException() {}

  public SSHOpertationException(String message) {
    super(message);
  }

  public SSHOpertationException(String message, Throwable cause) {
    super(message, cause);
  }

  public SSHOpertationException(Throwable cause) {
    super(cause);
  }
}
