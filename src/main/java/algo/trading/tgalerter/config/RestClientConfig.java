package algo.trading.tgalerter.config;

import algo.trading.tgalerter.exceptions.IntegrationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/** Rest Client configuration class. */
@Slf4j
@Configuration
public class RestClientConfig {

  /**
   * Configure Rest Client bean.
   *
   * @return Rest Client bean
   */
  @Bean
  RestClient workerRestClient() {
    return RestClient.builder()
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultStatusHandler(
            response -> {
              if (response.getStatusCode().isError()) {
                String errorDetails =
                    "Integration failed - Status: "
                        + response.getStatusCode()
                        + ", Body: "
                        + new String(response.getBody().readAllBytes());

                log.error("Integration error occurred: {}", errorDetails);
                throw new IntegrationException("Service integration error: " + errorDetails);
              }
              return false;
            })
        .build();
  }
}
