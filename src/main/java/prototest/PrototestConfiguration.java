package prototest;

import javax.net.ssl.SSLException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;

@org.springframework.context.annotation.Configuration
public class PrototestConfiguration
{
	@Value("${ssl_enabled}")
	private Boolean sslEnabled;


	@Bean
	public SslContext serverSsl() throws Exception {
		if(sslEnabled) {
			SelfSignedCertificate ssc = new SelfSignedCertificate();
			SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
			return sslCtx;
		} else {
			return null;
		}
	}

	@Bean
	public SslContext clientSsl() throws SSLException
	{
		if(sslEnabled) {
			return SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
		} else {
			return null;
		}

	}

}
