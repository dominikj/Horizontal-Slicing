package pl.mgr.hs.docker.util.exception;

/** Created by dominik on 02.01.19. */
public class CliAsyncExecutionException extends RuntimeException {
  public CliAsyncExecutionException() {}

  public CliAsyncExecutionException(String message) {
    super(message);
  }

  public CliAsyncExecutionException(String message, Throwable cause) {
    super(message, cause);
  }

  public CliAsyncExecutionException(Throwable cause) {
    super(cause);
  }
}
