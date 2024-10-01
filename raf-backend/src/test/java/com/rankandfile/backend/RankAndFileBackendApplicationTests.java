package com.rankandfile.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class RankAndFileBackendApplicationTests {

	@Test
	void contextLoads() {
		assertEquals(1, 1);
	}

}
