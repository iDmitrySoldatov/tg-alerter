package algo.trading.tgalerter.bot;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/** Contract for alert notification service. */
public interface AlertBot {
  /**
   * Sends alert message.
   *
   * @param message text content to send
   */
  void alert(String message, String chatId) throws TelegramApiException;
}
