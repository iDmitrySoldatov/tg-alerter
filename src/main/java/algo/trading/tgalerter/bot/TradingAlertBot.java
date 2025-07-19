package algo.trading.tgalerter.bot;

import algo.trading.common.dto.EventType;
import algo.trading.tgalerter.config.TelegramBotProperties;
import algo.trading.tgalerter.exceptions.FailBotStartingException;
import algo.trading.tgalerter.service.EventSubscriptionManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/** Telegram bot implementation for sending trading alerts. */
@Slf4j
@Component
public class TradingAlertBot implements AlertBot, SpringLongPollingBot {
  private static final String GET_CHAT_ID_BUTTON = "Get chatId";
  private static final String UNSUBSCRIBE_FROM_ACTION_EVENTS_BUTTON =
      "Unsubscribe from ACTION events";
  private static final String SUBSCRIBE_TO_ACTION_EVENTS_BUTTON = "Subscribe to ACTION events";

  private final TelegramClient client;
  private final TelegramBotProperties properties;
  private final Map<String, Consumer<Message>> commandHandlers;
  private final EventSubscriptionManager eventSubscriptionManager;

  /**
   * Initializes bot with configuration properties.
   *
   * @param properties bot configuration (token, chatId)
   */
  @Autowired
  public TradingAlertBot(
      TelegramBotProperties properties, EventSubscriptionManager eventSubscriptionManager) {
    this.properties = properties;
    client = new OkHttpTelegramClient(properties.getToken());
    this.commandHandlers = new HashMap<>();
    initializeCommandHandlers();
    this.eventSubscriptionManager = eventSubscriptionManager;
  }

  private void initializeCommandHandlers() {
    commandHandlers.put("/getchatid", this::handleGetChatIdCommand);
    commandHandlers.put("/unsubscribe_action_events", this::handleUnsubscribeActionEventsCommand);
    commandHandlers.put("/subscribe_action_events", this::handleSubscribeActionEventsCommand);
    commandHandlers.put("/menu", this::showMenu);

    // Button text handlers (exact matches)
    commandHandlers.put(GET_CHAT_ID_BUTTON, this::handleGetChatIdCommand);
    commandHandlers.put(
        SUBSCRIBE_TO_ACTION_EVENTS_BUTTON, this::handleSubscribeActionEventsCommand);
    commandHandlers.put(
        UNSUBSCRIBE_FROM_ACTION_EVENTS_BUTTON, this::handleUnsubscribeActionEventsCommand);
  }

  /**
   * Getter for token.
   *
   * @return validated bot token
   * @throws FailBotStartingException if token is empty
   */
  @Override
  public String getBotToken() {
    String token = properties.getToken();
    if (token == null || token.isEmpty()) {
      log.error("Bot token is empty");
      throw new FailBotStartingException("Bot token is empty");
    }
    return token;
  }

  /** GetUpdatesConsumer realized. */
  @Override
  public LongPollingUpdateConsumer getUpdatesConsumer() {
    return updates -> updates.forEach(this::processUpdate);
  }

  private void processUpdate(Update update) {
    if (!update.hasMessage() || !update.getMessage().hasText()) {
      return;
    }
    Message message = update.getMessage();
    String text = message.getText();
    Consumer<Message> handler = commandHandlers.get(text);
    if (handler != null) {
      handler.accept(message);
      return;
    }
    commandHandlers.entrySet().stream()
        .filter(entry -> text.startsWith(entry.getKey()))
        .findFirst()
        .ifPresent(entry -> entry.getValue().accept(message));
  }

  private void handleGetChatIdCommand(Message message) {
    Long chatId = message.getChatId();
    try {
      sendMessage(message.getChatId(), "Your Chat ID: " + message.getChatId());
      log.debug("Sent chat ID to chat: {}", chatId);
    } catch (Exception e) {
      log.debug("Failed to send chat ID: {}", e.getMessage());
    }
  }

  private void handleUnsubscribeActionEventsCommand(Message message) {
    Long chatId = message.getChatId();
    try {
      log.debug("Unsubscribe action events for chatId: {}", chatId);
      eventSubscriptionManager.unsubscribe(chatId.toString(), EventType.ACTION);
      sendMessage(message.getChatId(), "Unsubscribed from ACTION events");
      log.debug("Action events unsubscribed for chatId: {}", chatId);
    } catch (Exception e) {
      log.error("Action unsubscribe failed for chatId: {}", chatId, e);
    }
  }

  private void handleSubscribeActionEventsCommand(Message message) {
    Long chatId = message.getChatId();
    try {
      log.debug("Subscribe action events for chatId: {}", chatId);
      eventSubscriptionManager.subscribe(chatId.toString(), EventType.ACTION);
      sendMessage(message.getChatId(), "Subscribed to ACTION events");
      log.debug("Action events subscribed for chatId: {}", chatId);
    } catch (Exception e) {
      log.error("Action subscribe failed for chatId: {}", chatId, e);
    }
  }

  private void showMenu(Message message) {
    try {
      // Create keyboard rows
      List<KeyboardRow> keyboard = new ArrayList<>();

      // First row with two buttons
      KeyboardRow row1 = new KeyboardRow();
      row1.add(new KeyboardButton(GET_CHAT_ID_BUTTON));
      row1.add(new KeyboardButton(SUBSCRIBE_TO_ACTION_EVENTS_BUTTON));
      keyboard.add(row1);

      // Second row with one button
      KeyboardRow row2 = new KeyboardRow();
      row2.add(new KeyboardButton(UNSUBSCRIBE_FROM_ACTION_EVENTS_BUTTON));
      keyboard.add(row2);

      // Create and configure keyboard markup
      ReplyKeyboardMarkup menu =
          ReplyKeyboardMarkup.builder()
              .keyboard(keyboard)
              .resizeKeyboard(true) // Make buttons smaller to fit
              .oneTimeKeyboard(false) // Keep keyboard visible after use
              .build();

      // Create and send menu message
      SendMessage menuMessage =
          SendMessage.builder()
              .chatId(message.getChatId().toString())
              .text("Choose action:")
              .replyMarkup(menu)
              .build();

      client.execute(menuMessage);
    } catch (TelegramApiException e) {
      log.error("Failed to show menu", e);
    }
  }

  /**
   * Sends alert message to configured chat.
   *
   * @param message text to send
   */
  @Override
  public void alert(String message, String chatId) throws TelegramApiException {
    try {
      log.debug("Sending alert message: {}, to chatId: {}", message, chatId);
      client.execute(createSendMessage(message, chatId));
    } catch (TelegramApiException exception) {
      log.error(exception.getMessage());
      throw exception;
    }
  }

  private SendMessage createSendMessage(String message, String chatId) {
    return SendMessage.builder().chatId(chatId).text(message).build();
  }

  private void sendMessage(Long chatId, String text) {
    sendMessage(chatId, text, null);
  }

  private void sendMessage(Long chatId, String text, ReplyKeyboardMarkup keyboard) {
    try {
      SendMessage.SendMessageBuilder builder =
          SendMessage.builder().chatId(chatId.toString()).text(text);

      if (keyboard != null) {
        builder.replyMarkup(keyboard);
      }

      client.execute(builder.build());
    } catch (TelegramApiException e) {
      log.error("Message send failed", e);
    }
  }
}
