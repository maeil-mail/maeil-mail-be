package maeilwiki.comment;

import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import maeilwiki.member.Member;
import maeilwiki.member.MemberRepository;
import maeilwiki.wiki.Wiki;
import maeilwiki.wiki.WikiRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class CommentService {

    private final WikiRepository wikiRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void comment(CommentRequest request, Long wikiId) {
        String uuid = UUID.randomUUID().toString();
        Member temporalMember = new Member(uuid, uuid, "GITHUB");
        memberRepository.save(temporalMember);
        Wiki wiki = wikiRepository.findById(wikiId)
                .orElseThrow(NoSuchElementException::new);
        Comment comment = request.toComment(temporalMember, wiki);

        commentRepository.save(comment);
    }
}
