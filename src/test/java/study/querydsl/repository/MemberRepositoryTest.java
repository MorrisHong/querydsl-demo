package study.querydsl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    EntityManager em;
    @Autowired
    MemberRepository memberRepository;

    @Test
    void basicTest() {
        Member member = new Member("member1", 10);
        memberRepository.save(member);

        em.flush();
        em.flush();

        Member findMember = memberRepository.findById(member.getId()).get();
        assertEquals("member1", findMember.getUsername());

        List<Member> all = memberRepository.findAll();
        assertEquals("member1", all.get(0).getUsername());

        List<Member> member1 = memberRepository.findByUsername("member1");
        assertEquals(10, member1.get(0).getAge());
    }
}