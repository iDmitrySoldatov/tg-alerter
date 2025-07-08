package algo.trading.tgalerter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Telegram bot configuration properties. */
@Data
@ConfigurationProperties(prefix = "telegram.bot")
public class TelegramBotProperties {
  private String token;
  private String errorChatId;
}
