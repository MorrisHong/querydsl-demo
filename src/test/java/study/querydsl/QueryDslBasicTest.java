package study.querydsl;

import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals("member1", findMember.getUsername());
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
        assertEquals("member1", findMember.getUsername());
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
        assertEquals("member2", findMember.getUsername());
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
        assertEquals("member2", findMember.getUsername());
    }

    @Test
    void search() throws Exception {
        List<Member> findMembers = queryFactory
                .selectFrom(QMember.member)
                .where(QMember.member.username.startsWith("member")
                        .and(QMember.member.age.between(11, 39)))
                .fetch();

        assertEquals(2, findMembers.size());
    }

    @Test
    void search_and_param() throws Exception {
        List<Member> findMembers = queryFactory
                .selectFrom(QMember.member)
                .where(
                        QMember.member.username.startsWith("member"),
                        QMember.member.age.between(11, 39)
                )
                .fetch();

        assertEquals(2, findMembers.size());
    }

    @Test
    void resultFetch() {
        List<Member> fetch = queryFactory
                .selectFrom(QMember.member)
                .fetch();

        assertThrows(NonUniqueResultException.class, () -> {
            Member fetchOne = queryFactory
                    .selectFrom(QMember.member)
                    .fetchOne();
        });

        Member fetchFirst = queryFactory
                .selectFrom(QMember.member)
                .fetchFirst();

        QueryResults<Member> results = queryFactory
                .selectFrom(QMember.member)
                .fetchResults();
        long total = results.getTotal();
        List<Member> content = results.getResults();
    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단, 2에서 회원이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    void sort() throws Exception {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));
        em.persist(new Member("member7", 100));

        List<Member> result = queryFactory
                .selectFrom(QMember.member)
                .where(QMember.member.age.eq(100))
                .orderBy(
                        QMember.member.age.desc(),
                        QMember.member.username.asc().nullsLast()
                )
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member member7 = result.get(2);
        Member memberNull = result.get(3);

        assertEquals("member5", member5.getUsername());
        assertEquals("member6", member6.getUsername());
        assertEquals("member7", member7.getUsername());
        assertNull(memberNull.getUsername());
    }

    @Test
    void paging1() throws Exception {
        List<Member> fetch = queryFactory
                .selectFrom(QMember.member)
                .orderBy(QMember.member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        assertEquals(2, fetch.size());
    }

    @Test
    void paging2() throws Exception {
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(QMember.member)
                .orderBy(QMember.member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertEquals(4, queryResults.getTotal());
        assertEquals(2, queryResults.getResults().size());
        assertEquals(1, queryResults.getOffset());
        assertEquals(2, queryResults.getLimit());
    }

    @Test
    void aggregation() throws Exception {
        List<Tuple> result = queryFactory
                .select(
                        QMember.member.count(),
                        QMember.member.age.sum(),
                        QMember.member.age.avg(),
                        QMember.member.age.max(),
                        QMember.member.age.min()
                )
                .from(QMember.member)
                .fetch();

        Tuple tuple = result.get(0);
        assertEquals(4, tuple.get(QMember.member.count()));
        assertEquals(100, tuple.get(QMember.member.age.sum()));
        assertEquals(25, tuple.get(QMember.member.age.avg()));
        assertEquals(40, tuple.get(QMember.member.age.max()));
        assertEquals(10, tuple.get(QMember.member.age.min()));
    }


    /**
     * 팀의 이름과 각 팀의 평균 연령 구하기.
     */
    @Test
    void groupBy() throws Exception {
        List<Tuple> result = queryFactory
                .select(QTeam.team.name, QMember.member.age.avg())
                .from(QMember.member)
                .join(QMember.member.team, QTeam.team)
                .groupBy(QTeam.team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertEquals("teamA", teamA.get(QTeam.team.name));
        assertEquals("teamB", teamB.get(QTeam.team.name));
        assertEquals(15, teamA.get(QMember.member.age.avg()));
        assertEquals(35, teamB.get(QMember.member.age.avg()));

    }
}
