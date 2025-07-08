package algo.trading.tgalerter;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {}
