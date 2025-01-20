package maeilwiki.wiki;

import lombok.RequiredArgsConstructor;
import maeilwiki.member.Member;
import maeilwiki.member.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class WikiService {

    private final WikiRepository wikiRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void create(WikiRequest request) {
        Member temporalMember = new Member("temp", "1234", "GITHUB");
        memberRepository.save(temporalMember);
        Wiki wiki = request.toWiki(temporalMember); // TODO : 로그인 구현

        wikiRepository.save(wiki);
    }
}
