package org.springframework.quotes.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientSsl;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
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
			Mono<QuoteResource> result = webClient.get()
					.uri(baseUrl + "/api/random")
					.exchangeToMono((response) -> {
						if (response.statusCode().equals(HttpStatus.OK)) {
							return response.bodyToMono(QuoteResource.class);
						} else {
							return response.createError();
						}
					});
			String quote = result.block().value().quote();
			log.info(quote);
			System.out.println();
			System.out.println("Successfully connected to server and received quote:");
			System.out.println("\"" + quote + "\"");
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

		@Bean
		public WebClientCustomizer webClientCustomizer(WebClientSsl ssl,
													   @Value("${client.ssl-bundle}") String sslBundle) {
			return (webClientBuilder) -> webClientBuilder.apply(ssl.fromBundle(sslBundle)).build();
		}

	}
}
