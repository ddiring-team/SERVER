package com.ddiring.ddiring_server.global.client.fastapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class FastApiRestClientConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration WEEKLY_REPORT_READ_TIMEOUT = Duration.ofSeconds(60);

    @Value("${fastapi.base-url}")
    private String fastApiBaseUrl;

    @Bean
    public RestClient fastApiRestClient() {
        return buildClient(READ_TIMEOUT);
    }

    /**
     * 주간 리포트는 일주일치 응답을 한 번에 LLM으로 집계해 응답이 느리므로,
     * 별도의 긴 read timeout을 가진 전용 클라이언트를 사용한다.
     */
    @Bean
    public RestClient fastApiWeeklyReportRestClient() {
        return buildClient(WEEKLY_REPORT_READ_TIMEOUT);
    }

    private RestClient buildClient(Duration readTimeout) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .baseUrl(fastApiBaseUrl)
                .requestFactory(factory)
                .build();
    }
}
