package maeilwiki.member.dto;

import com.querydsl.core.annotations.QueryProjection;

public record MemberThumbnail(
        String name,
        String profileImageUrl,
        String github
) {

    @QueryProjection
    public MemberThumbnail {
    }
}
