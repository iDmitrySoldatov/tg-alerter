package algo.trading.tgalerter.integration;

import algo.trading.common.dto.StrategyInfo;
import algo.trading.tgalerter.config.IntegrationProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/** Service for integration with Trade Orchestrator service. */
@Slf4j
@Service
@RequiredArgsConstructor
public class TradeOrchestratorIntegration {
  private static final String GET_STRATEGY_INFO_URL = "/inner/strategy/{strategyId}/info";

  private final RestClient restClient;
  private final IntegrationProperty integrationProperty;

  /**
   * Fetches chat details for given strategy ID from Orchestrator service.
   *
   * @param strategyId ID of trading strategy
   * @return Chat DTO with communication details
   */
  public StrategyInfo getStrategyInfo(Long strategyId) {
    log.debug("Get strategyInfo for strategy {}", strategyId);
    StrategyInfo response =
        restClient
            .get()
            .uri(integrationProperty.getOrchestrator().getUrl() + GET_STRATEGY_INFO_URL, strategyId)
            .retrieve()
            .body(StrategyInfo.class);
    log.debug("Get strategyInfo for strategy {}, response: {}", strategyId, response);
    return response;
  }
}
