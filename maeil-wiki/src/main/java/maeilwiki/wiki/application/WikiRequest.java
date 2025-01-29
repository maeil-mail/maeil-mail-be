package maeilwiki.wiki.application;

import maeilwiki.member.domain.Member;
import maeilwiki.wiki.domain.Wiki;

public record WikiRequest(String question, String questionDetail, String category, boolean isAnonymous) {

    public Wiki toWiki(Member member) {
        if (questionDetail == null || questionDetail.isBlank()) {
            return new Wiki(question, category, isAnonymous, member);
        }

        return new Wiki(question, questionDetail, category, isAnonymous, member);
    }
}
