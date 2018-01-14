package prototest;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import prototest.client.MessagePersister;


@Profile("test")
@Configuration
public class TestConfiguration
{
	@Bean
	@Primary
	public Launcher launcher() {
		return Mockito.mock(Launcher.class);
	}

	@Bean
	@Primary
	public MessagePersister messagePersister() {
		return spy(MessagePersister.class);
	}
}
