package de.jaku.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "user.name=TestUser")
class E2EClientApplicationTests {

    @Test
    void contextLoads() {
    }

}
