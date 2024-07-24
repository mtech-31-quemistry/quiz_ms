package com.quemistry.quiz_ms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"service.question.url=http://mock-service-url"}
)
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}
