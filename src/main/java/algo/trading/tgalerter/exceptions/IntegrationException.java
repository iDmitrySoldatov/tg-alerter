package algo.trading.tgalerter.exceptions;

/** Exception for integration errors. */
public class IntegrationException extends RuntimeException {
  /**
   * Constructor.
   *
   * @param message error description.
   */
  public IntegrationException(String message) {
    super(message);
  }
}
