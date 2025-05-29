package at.aau.serg.scotlandyard;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class ApplicationTests {

    @Test
    void contextLoads() {
        // This test will pass if the Spring application context loads successfully
    }

    @Test
    void mainMethodRunsWithoutException() {
        assertDoesNotThrow(() -> Application.main(new String[] {}));
    }
}
