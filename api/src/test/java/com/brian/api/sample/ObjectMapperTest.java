package com.brian.api.sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ObjectMapperTest {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Setter 없이 값이 들어가나 확인")
    void Setter_없이_값이_들어가나_확인() throws Exception {
        // Getter 필수
        // Setter 비필수
        final SampleDto dto = new SampleDto();
        final String str = mapper.writeValueAsString(dto); // 객체를 읽을 때 getter 가 있어야 한다.
        final SampleDto2 dto2 = mapper.readValue(str, SampleDto2.class);
        Assertions.assertThat(dto2.getName()).isEqualTo("hello");
    }

    @Test
    @DisplayName("Setter 가 있으면 동작하는지 확인")
    void Setter_가_있으면_동작하는지_확인() throws Exception {
        final SampleDto dto = new SampleDto();
        final String str = mapper.writeValueAsString(dto);
        final SampleDto3 dto3 = mapper.readValue(str, SampleDto3.class);
        Assertions.assertThat(dto3.getName()).isEqualTo("ok");

    }

    @Getter
    public static class SampleDto {
        private String name = "hello";
    }

    @Getter
    public static class SampleDto2 {
        private String name;
    }

    @Getter
    public static class SampleDto3 {
        private String name;

        public void setName(String name) {
            this.name = "ok";
        }
    }
}
