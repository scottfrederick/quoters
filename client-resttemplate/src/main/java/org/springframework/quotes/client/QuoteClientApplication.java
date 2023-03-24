package org.springframework.quotes.client;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslDetails;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class QuoteClientApplication {

	@Value("${client.server-base-url}")
	private String baseUrl;

	private static final Logger log = LoggerFactory.getLogger(QuoteClientApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(QuoteClientApplication.class, args);
	}

	@Bean
	public CommandLineRunner run(RestTemplate restTemplate) {
		return args -> {
			QuoteResource quote = restTemplate.getForObject(baseUrl +
					"/api/random", QuoteResource.class);
			log.info(quote.value().quote());
		};
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	@Configuration
	@ConditionalOnProperty(value = "client.ssl-bundle")
	public static class RestTemplateSslConfiguration {

		@Value("${client.ssl-bundle}")
		private String sslBundle;

		@Bean
		public RestTemplateCustomizer restTemplateCustomizer(SslBundles sslBundles) {
			return (restTemplate) -> {
				SslBundle sslBundle = sslBundles.getBundle(this.sslBundle);
				SSLContext sslContext = sslBundle.getSslContext();
				SslDetails sslDetails = sslBundle.getDetails();

				SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext,
						sslDetails.getEnabledProtocols(),
						sslDetails.getCiphers(),
						new DefaultHostnameVerifier());
				PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder
						.create().setSSLSocketFactory(socketFactory).build();
				CloseableHttpClient client = HttpClientBuilder.create().setConnectionManager(connectionManager).build();

				restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(client));
			};
		}

	}
}
