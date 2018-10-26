package pl.mgr.hs.docker.util.exception;

/** Created by dominik on 26.10.18. */
public class DockerOperationException extends RuntimeException {
  public DockerOperationException() {}

  public DockerOperationException(String message) {
    super(message);
  }

  public DockerOperationException(String message, Throwable cause) {
    super(message, cause);
  }

  public DockerOperationException(Throwable cause) {
    super(cause);
  }
}
