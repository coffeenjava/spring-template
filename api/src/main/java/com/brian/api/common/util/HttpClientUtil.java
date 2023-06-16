package com.brian.api.common.util;

import com.brian.api.repository.dto.TestMemberAndDetailDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpClientUtil {

    public static void main(String[] args) throws Exception {
        String host = "http://localhost:8090/hello/body";
        URI uri = new URI(host);

        TestMemberAndDetailDto requestDto = new TestMemberAndDetailDto();
        requestDto.setName("brian");
        requestDto.setPhone("000-111-1111");

        CustomObjectMapper mapper = new CustomObjectMapper(new ObjectMapper());
        String requestStr = mapper.writeValueAsString(requestDto);

        HttpRequest request = HttpRequest.newBuilder(uri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                .method(HttpMethod.POST.name(), HttpRequest.BodyPublishers.ofString(requestStr))
                .POST(HttpRequest.BodyPublishers.ofString(requestStr))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        TestMemberAndDetailDto responseDto = mapper.readValue(response.body(), TestMemberAndDetailDto.class);
        System.out.println(responseDto.getName());

        /**
         * webclient
         */
//        TestMemberAndDetailDto wcResponse = WebClient.create(host)
//                .post()
//                .bodyValue(requestDto)
//                .retrieve()
//                .bodyToMono(TestMemberAndDetailDto.class)
//                .block();
//
//        System.out.println(wcResponse.getName());
    }
}
