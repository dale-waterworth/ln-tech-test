package dale.lexisnexisapidemo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Configuration
public class AppConfig {
  public static final String X_API_KEY = "x-api-key";

  @Bean
  public RestTemplate thirdPartyRestTemplate(
    RestTemplateBuilder builder,
    @Value("${api.url}") String apiUrl,
    @Value("${api.key}") String apiKey
    ) {
    var restTemplate = builder
      .rootUri(apiUrl)
      .build();

    List<ClientHttpRequestInterceptor> interceptors = Optional.of(restTemplate.getInterceptors())
      .orElse(List.of());

    interceptors.add((request, body, execution) -> {
      request.getHeaders().set(X_API_KEY, apiKey);
      return execution.execute(request, body);
    });

    return restTemplate;
  }
}
