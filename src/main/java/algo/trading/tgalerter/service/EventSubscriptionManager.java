package algo.trading.tgalerter.service;

import algo.trading.common.dto.EventType;

/** Manages chat subscriptions to event types. */
public interface EventSubscriptionManager {
  /** Unsubscribe chat from specific event type. */
  void unsubscribe(String chatId, EventType eventType);

  /** Subscribe chat to specific event type. */
  void subscribe(String chatId, EventType eventType);

  /** Check if chat is subscribed to event type. */
  boolean isSubscribed(String chatId, EventType eventType);
}
