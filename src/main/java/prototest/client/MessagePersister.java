package prototest.client;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.springframework.stereotype.Component;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import prototest.Prototest;


@Component
public class MessagePersister
{
	private InternalLogger logger = InternalLoggerFactory.getInstance(MessagePersister.class);

	private static final String DB_NAME = "messagestore";
	private DB database;
	private ConcurrentMap map;

	@PostConstruct
	public void setup() {
		database = DBMaker.fileDB(DB_NAME).transactionEnable().make();
		map = database.hashMap("map").createOrOpen();
	}

	@PreDestroy
	public void shutdown() {
		database.close();
	}

	public void save(final Prototest.ProtoRequest build)
	{
		map.put(build.getId(), build);
		logger.info("Message saved,   storage size : " + map.size());
	}

	public Collection<Prototest.ProtoRequest> getMessages()
	{

		return (Collection<Prototest.ProtoRequest>) map.values();
	}

	public void delete(final Prototest.ProtoResponse resp)
	{
		map.remove(resp.getId());
		logger.info("Message removed, storage size : " + map.size());
	}
}
