package prototest;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ThreadUtils;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import prototest.Prototest;
import prototest.client.PrototestClient;
import prototest.proxy.PrototestProxy;
import prototest.server.PrototestServer;


@Component
public class Launcher implements ApplicationListener<ApplicationReadyEvent>
{
	@Autowired
	private PrototestClient prototestClient;
	@Autowired
	private PrototestServer prototestServer;
	@Autowired
	private PrototestProxy prototestProxy;


	@PostConstruct
	public void startup() {
		prototestServer.start();
		prototestProxy.start();
	}

	public void onApplicationEvent(final ApplicationReadyEvent event) {

		RandomStringGenerator generator = new RandomStringGenerator.Builder()
				.withinRange('0', 'z')
				.filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS)
				.build();

		while(true) {

			prototestClient.sendRequest( generator.generate(10000) );

			try
			{
				TimeUnit.SECONDS.sleep(3);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}


