package algo.trading.tgalerter.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Configuration properties for external service integrations. */
@Data
@Component
@NoArgsConstructor
@ConfigurationProperties(value = "integration")
public class IntegrationProperty {
  /** Orchestrator service connection details. */
  public Orchestrator orchestrator;

  /** Nested config for Orchestrator service. */
  @Data
  @NoArgsConstructor
  public static class Orchestrator {
    private String url;
  }
}
