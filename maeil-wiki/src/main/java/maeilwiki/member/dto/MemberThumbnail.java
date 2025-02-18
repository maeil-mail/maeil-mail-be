package maeilwiki.member.dto;

import com.querydsl.core.annotations.QueryProjection;

public record MemberThumbnail(
        Long id,
        String name,
        String profileImage,
        String github
) {

    @QueryProjection
    public MemberThumbnail {
    }
}
