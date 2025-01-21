package maeilwiki.comment;

import maeilwiki.member.Member;
import maeilwiki.wiki.Wiki;

record CommentRequest(String answer, boolean isAnonymous) {

    public Comment toComment(Member member, Wiki wiki) {
        return new Comment(answer, isAnonymous, member, wiki);
    }
}
