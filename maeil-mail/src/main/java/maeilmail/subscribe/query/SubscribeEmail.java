package maeilmail.subscribe.query;

import com.querydsl.core.annotations.QueryProjection;

public record SubscribeEmail(Long id, String email) {

    @QueryProjection
    public SubscribeEmail {
    }
}
