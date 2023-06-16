package com.brian.api.repository;

import com.brian.api.repository.entity.TestMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestMemberRepository extends JpaRepository<TestMember, Long> {
}
