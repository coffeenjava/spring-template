package com.my.api.repository.entity;

import com.my.api.common.consts.YesNo;
import com.my.api.common.convert.jpa.CustomEnumType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class TestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Type(type = CustomEnumType.NAME) // Enum 의 code 필드 기반으로 DB 에 값을 저장/조회하기 위한 설정
    private YesNo adultYn;
}
