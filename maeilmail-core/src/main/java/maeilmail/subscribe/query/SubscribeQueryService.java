package maeilmail.subscribe.query;

import static maeilmail.subscribe.command.domain.QSubscribe.subscribe;

import java.util.List;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscribeQueryService {

    private final JPAQueryFactory queryFactory;

    public List<SubscribeEmail> findAllWithUniqueEmail() {
        return queryFactory.select(new QSubscribeEmail(subscribe.id.min(), subscribe.email))
                .from(subscribe)
                .where(subscribe.deletedAt.isNull())
                .groupBy(subscribe.email)
                .fetch();
    }
}
