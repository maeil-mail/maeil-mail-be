package maeilwiki.wiki.application;

import lombok.RequiredArgsConstructor;
import maeilsupport.PaginationResponse;
import maeilwiki.member.application.MemberIdentity;
import maeilwiki.member.application.MemberService;
import maeilwiki.member.domain.Member;
import maeilwiki.wiki.domain.MultipleChoiceWiki;
import maeilwiki.wiki.domain.MultipleChoiceWikiRepository;
import maeilwiki.wiki.dto.MultipleChoiceWikiSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class MultipleChoiceWikiService {

    private final MultipleChoiceWikiRepository multipleChoiceWikiRepository;
    private final MemberService memberService;

    @Transactional
    public Long create(MemberIdentity identity, MultipleChoiceWikiRequest request) {
        Member member = memberService.findById(identity.id());
        MultipleChoiceWiki multipleChoiceWiki = request.toMultipleChoiceWiki(member.getId());

        return multipleChoiceWikiRepository.save(multipleChoiceWiki).getId();
    }

    @Transactional
    public void remove(MemberIdentity identity, Long wikiId) {
        MultipleChoiceWiki multipleChoiceWiki = multipleChoiceWikiRepository.findById(wikiId)
                .orElseThrow(NoSuchElementException::new);
        validateOwner(identity, multipleChoiceWiki);

        multipleChoiceWiki.remove();
    }

    private void validateOwner(MemberIdentity identity, MultipleChoiceWiki multipleChoiceWiki) {
        if (!identity.canAccessToResource(multipleChoiceWiki.getMemberId())) {
            throw new IllegalStateException("자신의 위키만 삭제할 수 있습니다.");
        }
    }

    public MultipleChoiceWikiResponse getWikiById(MemberIdentity identity, Long id) {
        MultipleChoiceWiki multipleChoiceWiki = multipleChoiceWikiRepository.findById(id).orElseThrow(NoSuchElementException::new);
        Member member = memberService.findById(multipleChoiceWiki.getMemberId());
        return MultipleChoiceWikiResponse.of(multipleChoiceWiki, member);
    }

    public PaginationResponse<MultipleChoiceWikiResponse> pageByCategory(String category, Pageable pageable) {
        Page<MultipleChoiceWikiSummary> pageResults = multipleChoiceWikiRepository.pageByCategory(category, pageable);
        List<MultipleChoiceWikiResponse> wikiResponses = pageResults.getContent()
                .stream()
                .map(MultipleChoiceWikiResponse::of)
                .toList();

        return new PaginationResponse<>(pageResults.isLast(), (long) pageResults.getTotalPages(), wikiResponses);
    }
}
