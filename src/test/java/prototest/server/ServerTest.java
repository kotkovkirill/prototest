package prototest.server;

import java.nio.charset.Charset;
import java.util.UUID;

import static org.junit.Assert.*;

import javax.annotation.PostConstruct;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.DigestUtils;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import prototest.Prototest;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class ServerTest
{
	@Value("${secret_phrase}")
	private String secret;
	private String md5;

	@PostConstruct
	public void setup() {
		md5 = DigestUtils.md5DigestAsHex(secret.getBytes(Charset.forName("UTF-8")));
	}


	@Test
	public void testBasicServer() {

		EmbeddedChannel channel = new EmbeddedChannel(new PrototestServerHandler());

		Prototest.ProtoRequest.Builder builder = Prototest.ProtoRequest.newBuilder();

		final String uuid = UUID.randomUUID().toString();
		builder.setId(uuid);
		builder.setBody("Test body");
		builder.setAuthHeader(md5);

		channel.writeInbound(builder.build());

		Object response = channel.readOutbound();

		assertTrue(response instanceof Prototest.ProtoResponse);

		Prototest.ProtoResponse protoResponse = (Prototest.ProtoResponse)response;

		assertEquals(uuid, protoResponse.getId() );
		assertEquals(Prototest.ResponseStatus.SUCCESS, protoResponse.getResponseStatus());

	}
}
