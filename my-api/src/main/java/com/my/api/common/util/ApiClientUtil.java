package com.my.api.common.util;

import com.querydsl.codegen.ParameterizedTypeImpl;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ApiClientUtil {

    private static final String VALUE_DELIMITER = ",";

    private static <T> Mono<T> get(String host, Function<UriBuilder, URI> uriFunc, ParameterizedTypeReference<T> typeReference) {
        return WebClient.builder()
                .baseUrl(host)
                .build()
                .get()
                .uri(uriFunc)
//                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(httpStatus -> httpStatus.is4xxClientError() || httpStatus.is5xxServerError()
                        , clientResponse -> clientResponse.bodyToMono(String.class)
                                .map(RuntimeException::new))
                .bodyToMono(typeReference)
//                .timeout(Duration.ofSeconds(10))
                ;
    }

    /**
     * 목록 조회
     *
     * @param host
     * @param path
     * @param params       요청 파라미터 MultiValueMap
     * @param responseType 응답 타입
     * @return
     */
    public static <T> List<T> syncGetList(String host, String path, MultiValueMap<String,String> params, Class<T> responseType) {
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder.path(path)
                        .queryParams(params)
                        .build();

        ParameterizedTypeReference<List<T>> typeReference = generateListTypeReference(responseType);

        return get(host, uriFunc, typeReference)
                .block();
    }

    /**
     * 목록 조회
     *
     * @param host
     * @param path
     * @param params       요청 파라미터 Map
     * @param responseType 응답 타입
     * @return
     */
    public static <T> List<T> syncGetList(String host, String path, Map<String,Object> params, Class<T> responseType) {
        MultiValueMap<String,String> strParams = convertToStringParam(params);

        return syncGetList(host, path, strParams, responseType);
    }

    /**
     * 단건 조회
     * response Root 값이 표준(data)인 경우에 사용
     *
     * @param host
     * @param path
     * @param params       요청 파라미터 MultiValueMap
     * @param responseType 응답 타입
     * @return
     */
    public static <T> T standardSyncGet(String host, String path, MultiValueMap<String,String> params, Class<T> responseType) {
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder.path(path)
                        .queryParams(params)
                        .build();

        ParameterizedTypeReference<Map<String,T>> typeReference = generateStandardTypeReference(responseType);

        return get(host, uriFunc, typeReference)
                .map(v -> v.get("data"))
                .block();
    }

    public static void main(String[] args) {
        LinkedMultiValueMap params = new LinkedMultiValueMap();
        params.add("cupnUseResCd", "HP2300043119");
        Map result = ApiClientUtil.syncGet("https://qaapi.hanatour.com", "/ext/couponApiCategory/appliedCoupon", params, Map.class);
        Object data = result.get("data");
        System.out.println(data);
    }

    /**
     * 단건 조회
     *
     * @param host
     * @param path
     * @param params       요청 파라미터 MultiValueMap
     * @param responseType 응답 타입
     * @return
     */
    public static <T> T syncGet(String host, String path, MultiValueMap<String,String> params, Class<T> responseType) {
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder.path(path)
                        .queryParams(params)
                        .build();

        ParameterizedTypeReference<T> typeReference = generateTypeReference(responseType);

        return get(host, uriFunc, typeReference)
                .block();
    }

    /**
     * 단건 조회
     *
     * @param host
     * @param path
     * @param params       요청 파라미터 Map
     * @param responseType 응답 타입
     * @return
     */
//    public static <T> T syncGet(String host, String path, Map<String,Object> params, Class<T> responseType) {
//        MultiValueMap<String,String> strParams = convertToStringParam(params);
//
//        return syncGet(host, path, strParams, responseType);
//    }

    /**
     * 다건 응답 변환용 ParameterizedTypeReference 생성
     * 응답 형태가 아래와 같은 경우 사용
     *  {
     *      key : [value1, value2, value3]
     *  }
     */
    private static <T> ParameterizedTypeReference<Map<String,List<T>>> generateStandardListTypeReference(Class<T> type) {
        ParameterizedType paramType = new ParameterizedTypeImpl(List.class, new Type[]{type});
        ParameterizedType rootParamType = new ParameterizedTypeImpl(Map.class, new Type[]{String.class, paramType});
        return ParameterizedTypeReference.forType(rootParamType);
    }

    /**
     * 다건 응답 변환용 ParameterizedTypeReference 생성
     * 응답 형태가 아래와 같은 경우 사용
     *  [value1, value2, value3]
     */
    private static <T> ParameterizedTypeReference<List<T>> generateListTypeReference(Class<T> type) {
        ParameterizedType paramType = new ParameterizedTypeImpl(List.class, new Type[]{type});
        return ParameterizedTypeReference.forType(paramType);
    }

    /**
     * 단건 응답 변환용 ParameterizedTypeReference 생성
     * 응답 형태가 아래와 같은 경우 사용
     *  { key : value }
     */
    private static <T> ParameterizedTypeReference<Map<String, T>> generateStandardTypeReference(Class<T> type) {
        ParameterizedType rootParamType = new ParameterizedTypeImpl(Map.class, new Type[]{String.class, type});
        return ParameterizedTypeReference.forType(rootParamType);
    }

    /**
     * 단건 응답 변환용 ParameterizedTypeReference 생성
     * 응답 형태가 아래와 같은 경우 사용
     *  value
     */
    private static <T> ParameterizedTypeReference<T> generateTypeReference(Class<T> type) {
        return ParameterizedTypeReference.forType(type);
    }

    /**
     * 파라미터 값을 String 으로 변환
     * @param paramMap 값이 단순 타입 혹은 Collection 타입 가능
     */
    public static MultiValueMap<String,String> convertToStringParam(Map<String,Object> paramMap) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        paramMap.forEach((k,v) -> {
            if (v instanceof Collection) {
                // Collection 인 경우 String 변환 후 VALUE_DELIMITER(,)로 연결
                String vStr = (String) ((Collection) v).stream()
                        .map(o -> o.toString())
                        .collect(Collectors.joining(VALUE_DELIMITER));
                params.add(k, vStr);
            } else {
                params.add(k, v.toString());
            }
        });

        return params;
    }
}
