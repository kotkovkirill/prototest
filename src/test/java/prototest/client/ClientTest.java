package prototest.client;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import io.netty.channel.embedded.EmbeddedChannel;
import prototest.Prototest;
import prototest.server.PrototestServerHandler;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class ClientTest
{

	@Autowired
	private PrototestClientHandler prototestClientHandler;
	@Autowired
	private MessagePersister messagePersister;



	@Test
	public void testClient() {

		EmbeddedChannel channel = new EmbeddedChannel(prototestClientHandler);

		PrototestClientHandler handler = channel.pipeline().get(PrototestClientHandler.class);

		handler.invokeServer("Test client body");

		Object request = channel.readOutbound();

		Assert.assertTrue(request instanceof Prototest.ProtoRequest);
		Prototest.ProtoRequest req = (Prototest.ProtoRequest)request;
		Assert.assertNotNull( req.getId());
		Assert.assertNotNull( req.getBody());
		verify(messagePersister, times(1)).save(any(Prototest.ProtoRequest.class));
		verify(messagePersister, times(1)).getMessages();

		Prototest.ProtoResponse failedResponse = Prototest.ProtoResponse.newBuilder().setResponseStatus(Prototest.ResponseStatus.ERROR).setId(req.getId()).build();
		channel.writeInbound(failedResponse);

		verify(messagePersister, times(0)).delete(any());

		Prototest.ProtoResponse successResponse = Prototest.ProtoResponse.newBuilder().setResponseStatus(Prototest.ResponseStatus.SUCCESS).setId(req.getId()).build();
		channel.writeInbound(successResponse);

		verify(messagePersister, times(1)).delete(any());


	}
}
