package com.rankandfile.dataloader;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class RankAndFileDataLoaderApplicationTests {

	@Test
	void contextLoads() {
		assertEquals(1, 1);
	}

}
