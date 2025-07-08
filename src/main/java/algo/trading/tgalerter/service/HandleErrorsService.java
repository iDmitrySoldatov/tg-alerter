package algo.trading.tgalerter.service;

import static algo.trading.tgalerter.util.FormatUtil.appendIfNotNull;
import static algo.trading.tgalerter.util.FormatUtil.formatTime;
import static algo.trading.tgalerter.util.FormatUtil.getFirstLinesOfStackTrace;

import algo.trading.common.dto.StrategyEvent;
import algo.trading.tgalerter.bot.TradingAlertBot;
import algo.trading.tgalerter.config.TelegramBotProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for handling and reporting errors that occur during strategy event processing. Formats
 * error messages and sends them to a configured Telegram chat.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HandleErrorsService {
  private final TradingAlertBot tradingAlertBot;
  private final TelegramBotProperties telegramBotProperties;
  private final ObjectMapper objectMapper;

  /**
   * Handles an exception that occurred during strategy event processing. Builds an error message
   * and sends it to the configured Telegram error chat.
   *
   * @param exception the exception that occurred
   * @param event the strategy event being processed when the error occurred
   * @throws RuntimeException if an error occurs during message sending (logged but not propagated)
   */
  public void handleError(Exception exception, StrategyEvent event) {
    try {
      log.debug("handleError() - start, with exception: {}, event :{}", exception, event);
      String message = buildMessage(exception, event);
      String errorChatId = telegramBotProperties.getErrorChatId();
      log.debug("handleError() - send to telegram, message : {}, chatId: {}", message, errorChatId);
      tradingAlertBot.alert(message, errorChatId);
      log.debug("handleError() - end");
    } catch (Exception e) {
      log.error("handleError() - error: {}", e.getMessage(), e);
    }
  }

  @SneakyThrows
  private String buildMessage(Exception ex, StrategyEvent event) {
    StringBuilder sb = new StringBuilder();
    sb.append("=== Error processing event ===\n");

    // Основные поля события
    appendIfNotNull(sb, "Type", event.getType());
    appendIfNotNull(sb, "Strategy ID", event.getStrategyId());
    appendIfNotNull(sb, "Time", formatTime(event.getTime()));
    appendIfNotNull(sb, "State", event.getState());

    // Сообщение об ошибке
    if (StringUtils.isNotBlank(event.getMessage())) {
      sb.append("\nEvent Message:\n").append(event.getMessage()).append("\n");
    }

    // Детали ордера (если есть)
    if (event.getOrder() != null) {
      sb.append("\nOrder Details:\n")
          .append(
              objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(event.getOrder()))
          .append("\n");
    }

    // Информация об исключении
    sb.append("\nError:\n");
    sb.append("Type: ").append(ex.getClass().getSimpleName()).append("\n");
    sb.append("Message: ").append(ex.getMessage()).append("\n");

    // StackTrace (первые 5 строк)
    sb.append("\nStacktrace (first 5 lines):\n").append(getFirstLinesOfStackTrace(ex, 5));

    return sb.toString();
  }
}
