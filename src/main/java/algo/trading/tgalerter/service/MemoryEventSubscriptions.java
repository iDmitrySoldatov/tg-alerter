package algo.trading.tgalerter.service;

import algo.trading.common.dto.EventType;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/** In-memory storage for event subscriptions. */
@Service
public class MemoryEventSubscriptions implements EventSubscriptionManager {
  private final ConcurrentHashMap<String, Set<EventType>> unsubscriptions =
      new ConcurrentHashMap<>();

  @Override
  public void subscribe(String chatId, EventType eventType) {
    unsubscriptions.computeIfPresent(
        chatId,
        (k, v) -> {
          v.remove(eventType);
          return v.isEmpty() ? null : v;
        });
  }

  @Override
  public void unsubscribe(String chatId, EventType eventType) {
    unsubscriptions.computeIfAbsent(chatId, k -> ConcurrentHashMap.newKeySet()).add(eventType);
  }

  @Override
  public boolean isSubscribed(String chatId, EventType eventType) {
    return !unsubscriptions.getOrDefault(chatId, Set.of()).contains(eventType);
  }
}
