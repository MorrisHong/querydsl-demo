package study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

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

    @Test
    void searchTest() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        List<MemberTeamDto> memberTeamDtos = memberJpaRepository.searchByBuilder(condition);
        assertEquals(1, memberTeamDtos.size());
        assertEquals("member4", memberTeamDtos.get(0).getUsername());
    }

}