package algo.trading.tgalerter.bot;

/** Contract for alert notification service. */
public interface AlertBot {
  /**
   * Sends alert message.
   *
   * @param message text content to send
   */
  void alert(String message);
}
