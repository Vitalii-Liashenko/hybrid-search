package com.liashenko.v.hybrid.search.testconfig;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for integration tests using Testcontainers.
 * Provides common configuration for database integration tests.
 */
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "spring.profiles.active=test",
    })
@Import(IntegrationTestConfig.class)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {
    // Common test utilities and setup can be added here
}
