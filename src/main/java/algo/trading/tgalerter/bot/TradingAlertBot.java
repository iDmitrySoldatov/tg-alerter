package algo.trading.tgalerter.bot;

import algo.trading.tgalerter.config.TelegramBotProperties;
import algo.trading.tgalerter.exceptions.FailBotStartingException;
import java.util.HashMap;
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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/** Telegram bot implementation for sending trading alerts. */
@Slf4j
@Component
public class TradingAlertBot implements AlertBot, SpringLongPollingBot {
  private final TelegramClient client;
  private final TelegramBotProperties properties;
  private final Map<String, Consumer<Message>> commandHandlers;

  /**
   * Initializes bot with configuration properties.
   *
   * @param properties bot configuration (token, chatId)
   */
  @Autowired
  public TradingAlertBot(TelegramBotProperties properties) {
    this.properties = properties;
    client = new OkHttpTelegramClient(properties.getToken());
    this.commandHandlers = new HashMap<>();
    initializeCommandHandlers();
  }

  private void initializeCommandHandlers() {
    commandHandlers.put("/getchatid", this::handleGetChatIdCommand);
    // Добавляем другие команды здесь
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
    String text = message.getText().trim();
    commandHandlers.entrySet().stream()
        .filter(entry -> text.startsWith(entry.getKey()))
        .findFirst()
        .ifPresent(entry -> entry.getValue().accept(message));
  }

  private void handleGetChatIdCommand(Message message) {
    Long chatId = message.getChatId();
    try {
      String response = "ID этого чата/группы: " + chatId;
      client.execute(SendMessage.builder().chatId(chatId.toString()).text(response).build());
      log.debug("Sent chat ID to chat: {}", chatId);
    } catch (TelegramApiException e) {
      log.debug("Failed to send chat ID: {}", e.getMessage());
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

  /**
   * Creates SendMessage object with validation.
   *
   * @param message text content
   * @param chatId chat id
   * @return prepared SendMessage
   * @throws FailBotStartingException if chatId is empty
   */
  private SendMessage createSendMessage(String message, String chatId) {
    return SendMessage.builder().chatId(chatId).text(message).build();
  }
}
