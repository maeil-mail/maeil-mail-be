package maeilwiki.wiki;

import maeilwiki.member.Member;

record WikiRequest(String question, String questionDetail, String category, boolean isAnonymous) {

    public Wiki toWiki(Member member) {
        if (questionDetail == null || questionDetail.isBlank()) {
            return new Wiki(question, category, isAnonymous, member);
        }

        return new Wiki(question, questionDetail, category, isAnonymous, member);
    }
}
