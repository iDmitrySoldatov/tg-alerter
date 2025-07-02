package algo.trading.tgalerter.config;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Spring Rabbit Configuration. */
@Configuration
@EnableRabbit
public class RabbitConfig {

  /** Create bean Jackson2JsonMessageConverter. */
  @Bean
  public Jackson2JsonMessageConverter converter() {
    return new Jackson2JsonMessageConverter();
  }
}
