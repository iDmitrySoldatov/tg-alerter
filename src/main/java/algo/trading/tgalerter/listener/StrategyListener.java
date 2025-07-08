package algo.trading.tgalerter.listener;

import algo.trading.common.dto.EventType;
import algo.trading.common.dto.StrategyEvent;
import algo.trading.tgalerter.service.EventService;
import algo.trading.tgalerter.service.HandleErrorsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Rabbit listeners for strategy events. */
@Slf4j
@Component
@RequiredArgsConstructor
public class StrategyListener {
  private final EventService eventService;
  private final HandleErrorsService handleErrorsService;

  /** Listener for strategy event queue. */
  @RabbitListener(queues = "${stage}_alert_event_q", concurrency = "1")
  void processStrategyEvent(StrategyEvent event) {
    try {
      log.trace("Got event {}", event);
      if (isProcessedEvent(event)) {
        eventService.processEvent(event);
      }
    } catch (Exception e) {
      log.error("Error processing strategy event = {}", event, e);
      handleErrorsService.handleError(e, event);
    }
  }

  private boolean isProcessedEvent(StrategyEvent event) {
    return event.getType() != EventType.TICK;
  }
}
