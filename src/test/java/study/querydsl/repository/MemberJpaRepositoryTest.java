package study.querydsl.repository;

import org.junit.jupiter.api.Assertions;
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
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;
    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    void basicTest() {
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        em.flush();
        em.flush();

        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertEquals("member1", findMember.getUsername());

        List<Member> all = memberJpaRepository.findAll();
        assertEquals("member1", all.get(0).getUsername());

        List<Member> member1 = memberJpaRepository.findByUsername("member1");
        assertEquals(10, member1.get(0).getAge());
    }

    @Test
    void basicQueryDslTest() {
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        em.flush();
        em.flush();

        List<Member> all = memberJpaRepository.findAll_Querydsl();
        assertEquals("member1", all.get(0).getUsername());

        List<Member> member1 = memberJpaRepository.findByUsername_Querydsl("member1");
        assertEquals(10, member1.get(0).getAge());
    }

}