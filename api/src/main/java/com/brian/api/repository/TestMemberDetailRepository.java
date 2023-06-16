package com.brian.api.repository;

import com.brian.api.repository.entity.TestMemberDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestMemberDetailRepository extends JpaRepository<TestMemberDetail, Long> {
}
