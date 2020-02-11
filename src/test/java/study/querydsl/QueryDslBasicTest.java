package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class QueryDslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    void each() {
        this.queryFactory = new JPAQueryFactory(em);

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
    }

    @Test
    void startJPQL() throws Exception {
        //member1 찾기.
        String qlString = "select m from Member as m" +
                " where m.username = :username";

        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertEquals("member1", findMember.getUsername());
    }

    @Test
    void startQueryDSL() throws Exception {
        //given
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember m = new QMember("m");

        //when
        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1"))
                .fetchOne();

        //then
        assertEquals("member1", findMember.getUsername() );
     }

    @Test
    void startQueryDSL1() throws Exception {
        //given
        // JPAQueryFactory 필드로 뺌.
        QMember m = new QMember("m");

        //when
        Member findMember = this.queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1"))
                .fetchOne();

        //then
        assertEquals("member1", findMember.getUsername() );
    }

    @Test
    void startQueryDSL2() throws Exception {
        QMember m = QMember.member;
        //when
        Member findMember = this.queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member2"))
                .fetchOne();

        //then
        assertEquals("member2", findMember.getUsername() );
    }

    @Test
    void startQueryDSL3() throws Exception {
        //when
        Member findMember = this.queryFactory
                .select(QMember.member)
                .from(QMember.member)
                .where(QMember.member.username.eq("member2"))
                .fetchOne();

        //then
        assertEquals("member2", findMember.getUsername() );
    }
}
