package algo.trading.tgalerter.service;

import algo.trading.common.dto.StrategyEvent;
import algo.trading.tgalerter.bot.TradingAlertBot;
import algo.trading.tgalerter.integration.TradeOrchestratorIntegration;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Service for event processing. */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private final TradeOrchestratorIntegration tradeOrchestratorIntegration;
  private final TradingAlertBot tradingAlertBot;
  private final ObjectMapper objectMapper;

  /** Method for strategy event processing. */
  @SneakyThrows
  public void processEvent(StrategyEvent event) {
    log.debug("processEvent() - start: {}", event);
    String chatId = tradeOrchestratorIntegration.getChat(event.getStrategyId()).getChatId();
    String formattedEventMessage = formatEventMessage(event);
    tradingAlertBot.alert(formattedEventMessage, chatId);
    log.debug("processEvent() - end: {}", event);
  }

  @SneakyThrows
  private String formatEventMessage(StrategyEvent event) {
    if (event == null) {
      return "Null event received";
    }

    StringBuilder sb = new StringBuilder();
    sb.append("=== Strategy Event ===\n");

    // Основные поля с проверкой на null
    appendIfNotNull(sb, "Type", event.getType());
    appendIfNotNull(sb, "Strategy ID", event.getStrategyId());
    appendIfNotNull(sb, "Time", formatTime(event.getTime()));
    appendIfNotNull(sb, "State", event.getState());

    // Сообщение (может быть null)
    if (StringUtils.isNotBlank(event.getMessage())) {
      sb.append("\nMessage:\n").append(event.getMessage()).append("\n");
    }

    // Детали ордера
    if (event.getOrder() != null) {
      sb.append("\nOrder Details:\n")
          .append(
              objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(event.getOrder()));
    }

    return sb.toString();
  }

  private String formatTime(Instant time) {
    if (time == null) {
      return "null";
    }
    DateTimeFormatter formatter = DATE_TIME_FORMATTER.withZone(ZoneId.systemDefault());
    return formatter.format(time);
  }

  // Вспомогательный метод для безопасного добавления полей
  private void appendIfNotNull(StringBuilder sb, String label, Object value) {
    if (value != null) {
      sb.append(label).append(": ").append(value).append("\n");
    }
  }
}
