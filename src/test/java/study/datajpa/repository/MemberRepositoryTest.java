package study.datajpa.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    void testMember() {
        System.out.println(memberRepository.getClass());
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);
        Member foundMember = memberRepository.findById(savedMember.getId()).get();

        assertThat(foundMember.getId()).isEqualTo(member.getId());
        assertThat(foundMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(foundMember).isEqualTo(member);
    }

    @Test
    void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        // 단건 조회 검증
        Member foundMember1 = memberRepository.findById(member1.getId()).get();
        Member foundMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(foundMember1).isEqualTo(member1);
        assertThat(foundMember2).isEqualTo(member2);

        // 리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        // 카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    void findByUsernameAndAgeGreaterThan() {

        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void findHelloBy() {
        List<Member> result = memberRepository.findTop3HelloBy();
        List<Member> result2 = memberRepository.findDistinctMemberBy();
    }

    @Test
    void testNamedQuery() {

        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsername("AAA");
        Member foundMember1 = result.get(0);
        Member foundMember2 = result.get(1);
        assertThat(foundMember1).isEqualTo(m1);
        assertThat(foundMember2).isEqualTo(m2);
    }

    @Test
    void testQuery() {

        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findMember("AAA", 10);
        assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    void findUsernameList() {

        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> usernameList = memberRepository.findUsernameList();
        for (String s : usernameList) {
            System.out.println("s = " + s);
        }
    }

    @Test
    void findMemberDto() {
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("AAA", 10);
        m1.setTeam(team);
        memberRepository.save(m1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
    }

    @Test
    void findByNames() {

        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    @Test
    void returnType() {

        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> foundMembers = memberRepository.findListByUsername("AAA");
        Member foundMember = memberRepository.findMemberByUsername("AAA");
        Optional<Member> foundMemberOptional = memberRepository.findOptionalByUsername("AAA");

        // 리스트로 반환 할 경우 조회된 데이터가 없으면 빈 컬렉션을 반환한다. null이 아님에 유의!
        List<Member> result = memberRepository.findListByUsername("CCC");
        System.out.println("result.size() = " + result.size());

        // 단건 조회일 경우 조회된 데이터가 없으면 null을 반환한다.
        Member notFoundMember = memberRepository.findMemberByUsername("CCC");
        System.out.println("notFoundMember = " + notFoundMember);

        // 데이터가 있을지 없을지 모르는 경우 옵셔널을 사용하는 것을 권장
        Optional<Member> notFoundMemberOptional = memberRepository.findOptionalByUsername("CCC");
        System.out.println("notFoundMemberOptional = " + notFoundMemberOptional);

        memberRepository.save(new Member("CCC", 20));
        memberRepository.save(new Member("CCC", 30));

        // 옵셔널 단건 조회시 조회된 데이터가 2개 이상이면 IncorrectResultSizeDataAccessException 예외가 발생
        // NonUniqueResultException 예외가 발생하면 스프링 데이터 JPA가 스프링 프레임워크 예외인 IncorrectResultSizeDataAccessException로 변환
        Assertions.assertThrows(IncorrectResultSizeDataAccessException.class, () -> memberRepository.findOptionalByUsername("CCC"));
    }

    @Test
    void paging() {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        // when
        List<Member> list = memberRepository.findByAge(age, pageRequest);
        Page<Member> page = memberRepository.findPageByAge(age, pageRequest);
        Slice<Member> slice = memberRepository.findSliceByAge(age, pageRequest);

        // API에서 리턴할 때 DTO로 변환해서 리턴해야 함!
        Page<MemberDto> pageMemberDto = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));

        // then

        // List - 단순히 limit, offset 적용된 쿼리
        assertThat(list.size()).isEqualTo(3);

        // Page - limit, offset 쿼리와 count 쿼리
        List<Member> content = page.getContent();
        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0); // 페이지 번호
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();

        // Slice - limit 가 size + 1
        List<Member> sliceContent = slice.getContent();
        assertThat(sliceContent.size()).isEqualTo(3);
        assertThat(slice.getNumber()).isEqualTo(0); // 페이지 번호
        assertThat(slice.isFirst()).isTrue();
        assertThat(slice.hasNext()).isTrue();
    }

    @Test
    void bulkUpdate() {
        // given
        memberRepository.save(new Member("member1", 19));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        // when
        int resultCount = memberRepository.bulkAgePlus(20);

        // 벌크 연산 이후 조회시 영속성 컨텍스트를 초기화하고 조회해야 함
        // 벌크 연산은 영속성 컨텍스트에 반영되지 않고 바로 DB로 반영되기 때문
        // 또는 @Modifying 어노테이션의 clearAutomatically 속성을 true로 주면 됨
        //em.clear();

        List<Member> result = memberRepository.findByUsername("member5");
        Member member = result.get(0);
        assertThat(member.getAge()).isEqualTo(41);
        System.out.println("member = " + member);

        // then
        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    void findMemberLazy() {
        // given
        // member1 -> teamA
        // member2 -> teamB

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        // when
        List<Member> members = memberRepository.findAll();
        System.out.println("==========================");
        List<Member> members2 = memberRepository.findMemberFetchJoin();
        List<Member> members3 = memberRepository.findMemberEntityGraph();
        List<Member> members4 = memberRepository.findEntityGraphByUsername("member1");

        for (Member member : members) {
            System.out.println("member.getUsername() = " + member.getUsername());
            System.out.println("member.getTeam().getClass() = " + member.getTeam().getClass());
            System.out.println("member.getTeam().getName() = " + member.getTeam().getName());
        }
    }

    @Test
    void queryHint() {
        // given
        Member member = new Member("member1", 10);
        memberRepository.save(member);
        memberRepository.save(new Member("member", 20));
        memberRepository.save(new Member("member", 30));
        memberRepository.save(new Member("member", 40));
        memberRepository.save(new Member("member", 50));
        em.flush();
        em.clear();

        // when
        Member foundMember = memberRepository.findReadOnlyByUsername("member1");
        foundMember.setUsername("member2");

        em.flush();
    }

    @Test
    void lock() {
        // given
        Member member = new Member("member1", 10);
        memberRepository.save(member);
        em.flush();
        em.clear();

        // when
        List<Member> result = memberRepository.findLockByUsername("member1");

        em.flush();
    }

    @Test
    void callCustom() {
        List<Member> result = memberRepository.findMemberCustom();
    }

    @Test
    void specification() {
        // given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        // when
        Specification<Member> spec = MemberSpec.username("m1").and(MemberSpec.teamName("teamA"));
        List<Member> result = memberRepository.findAll(spec);

        // then
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void queryByExample() {
        // given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        // when
        // probe: 필드에 데이터가 있는 실제 도메인 객체
        // 엔티티 자체가 검색조건
        Member member = new Member("m1");
        Team team = new Team("teamA");
        member.setTeam(team);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("age");

        Example<Member> example = Example.of(member, matcher);

        List<Member> result = memberRepository.findAll(example);

        // then
        assertThat(result.get(0).getUsername()).isEqualTo("m1");
    }

    @Test
    void projections() {
        // given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        // when
        List<UsernameOnly> result1 = memberRepository.findProjections1ByUsername("m1");

        for (UsernameOnly usernameOnly : result1) {
            System.out.println("usernameOnly = " + usernameOnly.getUsername());
        }

        List<UsernameOnlyDto> result2 = memberRepository.findProjections2ByUsername("m1");

        for (UsernameOnlyDto usernameOnlyDto : result2) {
            System.out.println("usernameOnlyDto = " + usernameOnlyDto.getUsername());
        }

        List<UsernameOnlyDto> result3 = memberRepository.findProjections3ByUsername("m1", UsernameOnlyDto.class);
        for (UsernameOnlyDto usernameOnlyDto : result3) {
            System.out.println("usernameOnlyDto = " + usernameOnlyDto.getUsername());
        }

        List<NestedClosedProjections> result4 = memberRepository.findProjections3ByUsername("m1", NestedClosedProjections.class);
        for (NestedClosedProjections nestedClosedProjections : result4) {
            System.out.println("username = " + nestedClosedProjections.getUsername());
            System.out.println("teamName = " + nestedClosedProjections.getTeam().getName());
        }
    }

    @Test
    void nativeQuery() {
        // given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        // when
        Member result = memberRepository.findByNativeQuery("m1");
        System.out.println("result = " + result);

        Page<MemberProjection> result2 = memberRepository.findByNativeProjection(PageRequest.of(0, 10));
        List<MemberProjection> content = result2.getContent();
        for (MemberProjection memberProjection : content) {
            System.out.println("memberProjection.getUsername() = " + memberProjection.getUsername());
            System.out.println("memberProjection.getTeamName() = " + memberProjection.getTeamName());
        }
    }
}