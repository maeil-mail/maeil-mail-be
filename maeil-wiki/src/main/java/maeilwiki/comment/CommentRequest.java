package maeilwiki.comment;

import maeilwiki.member.Member;

public record CommentRequest(String answer, boolean isAnonymous) {

    public Comment toComment(Member member, Long wikiId) {
        return new Comment(answer, isAnonymous, member, wikiId);
    }
}
