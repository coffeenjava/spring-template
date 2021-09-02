package com.brian.api.common.util;

import org.apache.commons.io.IOUtils;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ResourceUtil {

    /**
     * 리소스 path 의 file 내용 읽기
     */
    public static String readJson(final String path) {
        try {
            return IOUtils.toString(ResourceUtils.getURL(path), StandardCharsets.UTF_8);
        } catch (IOException e) {}

        return null;
    }
}
