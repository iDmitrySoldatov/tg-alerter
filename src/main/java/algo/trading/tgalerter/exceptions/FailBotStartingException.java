package algo.trading.tgalerter.exceptions;

/** Exception for bot initialization failures. */
public class FailBotStartingException extends RuntimeException {
  /**
   * Constructor.
   *
   * @param message error description.
   */
  public FailBotStartingException(String message) {
    super(message);
  }
}
