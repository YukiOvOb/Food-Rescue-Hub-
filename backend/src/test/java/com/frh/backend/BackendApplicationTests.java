package com.frh.backend;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void main_invokesSpringApplicationRun() {
		String[] args = {"--spring.profiles.active=test"};
		ConfigurableApplicationContext mockContext = Mockito.mock(ConfigurableApplicationContext.class);

		try (MockedStatic<SpringApplication> springApplicationMock = Mockito.mockStatic(SpringApplication.class)) {
			springApplicationMock.when(() -> SpringApplication.run(BackendApplication.class, args))
				.thenReturn(mockContext);

			BackendApplication.main(args);

			springApplicationMock.verify(() -> SpringApplication.run(BackendApplication.class, args));
		}
	}

}
