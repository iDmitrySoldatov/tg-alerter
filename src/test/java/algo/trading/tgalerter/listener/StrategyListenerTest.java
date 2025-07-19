package algo.trading.tgalerter.listener;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import algo.trading.common.dto.ChatDto;
import algo.trading.common.dto.EventType;
import algo.trading.common.dto.StrategyEvent;
import algo.trading.tgalerter.BaseIntegrationTest;
import algo.trading.tgalerter.bot.TradingAlertBot;
import algo.trading.tgalerter.service.EventSubscriptionManager;
import algo.trading.tgalerter.service.MemoryEventSubscriptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.telegram.telegrambots.longpolling.starter.TelegramBotStarterConfiguration;

@SpringBootTest
@ActiveProfiles("test")
@ImportAutoConfiguration(exclude = TelegramBotStarterConfiguration.class)
public class StrategyListenerTest extends BaseIntegrationTest {
  @Autowired private ObjectMapper objectMapper;

  @Autowired StrategyListener strategyListener;

  @Autowired private TradingAlertBot tradingAlertBot;

  @Autowired private EventSubscriptionManager eventSubscriptionManager;

  @TestConfiguration
  static class TestConfig {
    @Bean
    @Primary // Важно - переопределяем основной бин
    public TradingAlertBot tradingAlertBot() {
      TradingAlertBot mock = mock(TradingAlertBot.class);
      when(mock.getBotToken()).thenReturn("test-token");
      return mock;
    }

    @Bean
    @Primary
    public EventSubscriptionManager eventSubscriptionManager() {
      return new MemoryEventSubscriptions();
    }
  }

  @BeforeEach
  @SneakyThrows
  void setUp() {
    Mockito.reset(tradingAlertBot);
    // Очищаем подписки через reflection
    if (eventSubscriptionManager instanceof MemoryEventSubscriptions) {
      Field storageField = MemoryEventSubscriptions.class.getDeclaredField("storage");
      storageField.setAccessible(true);
      ConcurrentHashMap<?, ?> storage =
          (ConcurrentHashMap<?, ?>) storageField.get(eventSubscriptionManager);
      storage.clear();
    }
  }

  @Test
  @SneakyThrows
  public void processStrategyEventSuccessTest() {
    // given
    StrategyEvent strategyEvent =
        StrategyEvent.builder().type(EventType.ACTION).strategyId(777L).build();
    ChatDto chatDto = ChatDto.builder().chatId("666").build();
    stubFor(
        WireMock.get(urlEqualTo("/inner/strategy/777/info"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(chatDto))));

    // when
    strategyListener.processStrategyEvent(strategyEvent);

    // then
    ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
    verify(tradingAlertBot, times(1)).alert(messageCaptor.capture(), eq(chatDto.getChatId()));

    String sentMessage = messageCaptor.getValue();
    assertThat(sentMessage).isNotEmpty();
    assertThat(sentMessage).contains("777");
  }

  @Test
  @SneakyThrows
  public void processStrategyEventWhenUnsubscribedShouldNotSendAlert() {
    // given
    StrategyEvent strategyEvent =
        StrategyEvent.builder().type(EventType.ACTION).strategyId(777L).build();

    ChatDto chatDto = ChatDto.builder().chatId("666").build();

    stubFor(
        WireMock.get(urlEqualTo("/inner/strategy/777/info"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(chatDto))));

    eventSubscriptionManager.unsubscribe(chatDto.getChatId(), EventType.ACTION);

    // when
    strategyListener.processStrategyEvent(strategyEvent);

    // then
    verify(tradingAlertBot, never()).alert(anyString(), eq(chatDto.getChatId()));
  }
}
