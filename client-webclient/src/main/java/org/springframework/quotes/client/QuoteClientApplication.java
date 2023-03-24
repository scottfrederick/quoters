package org.springframework.quotes.client;

import java.util.Arrays;

import javax.net.ssl.SSLContext;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.JdkSslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslDetails;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class QuoteClientApplication {

	@Value("${client.server-base-url}")
	private String baseUrl;

	private static final Logger log = LoggerFactory.getLogger(QuoteClientApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(QuoteClientApplication.class, args);
	}

	@Bean
	public CommandLineRunner run(WebClient webClient) {
		return args -> {
			Mono<QuoteResource> quote = webClient.get()
					.uri(baseUrl + "/api/random")
					.exchangeToMono((response) -> {
						if (response.statusCode().equals(HttpStatus.OK)) {
							return response.bodyToMono(QuoteResource.class);
						} else {
							return response.createError();
						}
					});
			log.info(quote.block().value().quote());
		};
	}

	@Bean
	public WebClient webClient(WebClient.Builder builder) {
		return builder.baseUrl(getBaseUrl()).build();
	}

	private String getBaseUrl() {
		return this.baseUrl;
	}

	@Configuration
	@ConditionalOnProperty(value = "client.ssl-bundle")
	public static class WebClientSslConfiguration {

		@Value("${client.ssl-bundle}")
		private String sslBundle;

		@Bean
		public WebClientCustomizer webClientCustomizer(SslBundles sslBundles) {
			return (webClientBuilder) -> {
				SslBundle sslBundle = sslBundles.getBundle(this.sslBundle);
				SSLContext sslContext = sslBundle.getSslContext();
				SslDetails sslDetails = sslBundle.getDetails();

				JdkSslContext nettySslContext = new JdkSslContext(sslContext, true,
						(sslDetails.getCiphers() != null) ? Arrays.asList(sslDetails.getCiphers()) : null,
						IdentityCipherSuiteFilter.INSTANCE, null,
						ClientAuth.NONE, sslDetails.getEnabledProtocols(),
						false);
				HttpClient httpClient = HttpClient.create()
						.secure((sslContextSpec) -> sslContextSpec.sslContext(nettySslContext));

				webClientBuilder.clientConnector(new ReactorClientHttpConnector(httpClient)).build();
			};
		}

	}
}
