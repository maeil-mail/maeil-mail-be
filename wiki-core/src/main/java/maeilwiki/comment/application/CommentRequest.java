package maeilwiki.comment.application;

import maeilwiki.comment.domain.Comment;
import maeilwiki.member.domain.Member;

public record CommentRequest(String answer, boolean isAnonymous) {

    public Comment toComment(Member member, Long wikiId) {
        return new Comment(answer, isAnonymous, member, wikiId);
    }
}
