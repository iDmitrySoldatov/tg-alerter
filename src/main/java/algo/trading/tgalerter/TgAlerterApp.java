package algo.trading.tgalerter;

import algo.trading.tgalerter.config.TelegramBotProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/** Start spring application. */
@SpringBootApplication
@EnableConfigurationProperties(value = TelegramBotProperties.class)
public class TgAlerterApp {
  /** Main method for start application. */
  public static void main(String[] args) {
    SpringApplication.run(TgAlerterApp.class, args);
  }
}
