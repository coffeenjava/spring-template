package com.my.api.repository;

import com.my.api.repository.dto.TestDetailDto;

public interface TestEntityQueryRepository {
    TestDetailDto findDetail(Long id);
}
