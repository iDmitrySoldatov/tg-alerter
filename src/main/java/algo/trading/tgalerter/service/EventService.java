package algo.trading.tgalerter.service;

import static algo.trading.tgalerter.util.FormatUtil.appendIfNotNull;
import static algo.trading.tgalerter.util.FormatUtil.formatTime;

import algo.trading.common.dto.StrategyEvent;
import algo.trading.common.dto.StrategyInfo;
import algo.trading.tgalerter.bot.TradingAlertBot;
import algo.trading.tgalerter.integration.TradeOrchestratorIntegration;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Service for event processing. */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
  private final TradeOrchestratorIntegration tradeOrchestratorIntegration;
  private final TradingAlertBot tradingAlertBot;
  private final ObjectMapper objectMapper;
  private final EventSubscriptionManager eventSubscriptionManager;

  /** Method for strategy event processing. */
  @SneakyThrows
  public void processEvent(StrategyEvent event) {
    log.debug("processEvent() - start: {}", event);
    StrategyInfo strategyInfo = tradeOrchestratorIntegration.getStrategyInfo(event.getStrategyId());
    if (!eventSubscriptionManager.isSubscribed(strategyInfo.getChatId(), event.getType())) {
      log.debug(
          "processEvent() - no subscribed on eventType: {} for chatId: {}",
          event.getType(),
          strategyInfo.getChatId());
      return;
    }
    String formattedEventMessage = formatEventMessage(event, strategyInfo);
    tradingAlertBot.alert(formattedEventMessage, strategyInfo.getChatId());
    log.debug("processEvent() - end: {}", event);
  }

  @SneakyThrows
  private String formatEventMessage(StrategyEvent event, StrategyInfo strategyInfo) {
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
    appendIfNotNull(sb, "Symbol", strategyInfo.getSymbol());
    appendIfNotNull(sb, "Timeframe", strategyInfo.getTimeframe());
    appendIfNotNull(sb, "Exchange", strategyInfo.getExchange());

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
}
