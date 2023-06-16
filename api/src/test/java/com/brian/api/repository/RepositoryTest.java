package com.brian.api.repository;

import com.brian.api.repository.dto.TestMemberAndDetailDto;
import com.brian.api.repository.entity.TestMember;
import com.brian.api.repository.entity.TestMemberDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("local")
// embedded h2 설정 강제 막는 설정
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
class RepositoryTest {

    @Autowired
    private TestMemberRepository memberRepository;

    @Autowired
    private TestMemberDetailRepository detailRepository;

    @Autowired
    private TestQueryDslRepository queryDslRepository;

    @BeforeEach
    void createMemberAndDetail() {
        // given
        TestMember member1 = TestMember.builder()
                .name("brian")
                .build();

        memberRepository.save(member1);

        TestMemberDetail memberDetail1 = TestMemberDetail.builder()
                .memberId(member1.getId())
                .phone("010-1111-1111")
                .createDate(LocalDateTime.parse("2021-01-06T00:00:00"))
                .build();

        detailRepository.save(memberDetail1);

        TestMember member2 = TestMember.builder()
                .name("wyatt")
                .build();

        memberRepository.save(member2);

        TestMemberDetail memberDetail2 = TestMemberDetail.builder()
                .memberId(member1.getId())
                .phone("010-2222-2222")
                .createDate(LocalDateTime.parse("2021-01-04T00:00:00"))
                .build();

        detailRepository.save(memberDetail2);
    }

    @Test
    void save() {
        // given
        String name = "Brian";
        TestMember entity = TestMember.builder().name(name).build();

        // when
        memberRepository.save(entity);

        // then
        assertNotNull(entity.getId());
        assertEquals(name, entity.getName());
        System.out.println(entity);
    }

    @Test
    void queryDslTest() {
        LocalDateTime searchStartDate = LocalDateTime.parse("2021-01-05T00:00:00");
        LocalDateTime searchEndDate = LocalDateTime.parse("2021-01-08T00:00:00");
        List<TestMemberAndDetailDto> result = queryDslRepository.getMemberAndDetail(searchStartDate, searchEndDate);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getName(), "brian");
    }
}
