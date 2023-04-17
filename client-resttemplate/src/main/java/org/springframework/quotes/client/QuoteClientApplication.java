package org.springframework.quotes.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestTemplateBuilderConfigurer;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
			QuoteResource result = restTemplate.getForObject(baseUrl +
					"/api/random", QuoteResource.class);
			String quote = result.value().quote();
			log.info(quote);
			System.out.println();
			System.out.println("Successfully connected to server and received quote:");
			System.out.println("\"" + quote + "\"");
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
		public RestTemplateBuilder restTemplateBuilder(RestTemplateBuilderConfigurer configurer, SslBundles sslBundles) {
			SslBundle sslBundle = sslBundles.getBundle(this.sslBundle);
			return configurer.configure(new RestTemplateBuilder()).setSslBundle(sslBundle);
		}

	}
}
