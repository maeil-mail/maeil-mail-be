package maeilmail.subscribequestion;

import static maeilmail.question.QQuestion.question;
import static maeilmail.subscribe.QSubscribe.subscribe;
import static maeilmail.subscribequestion.QSubscribeQuestion.subscribeQuestion;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import maeilmail.PaginationResponse;
import maeilmail.question.QQuestionSummary;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.question.QuestionSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscribeQuestionQueryService {

    private final JPAQueryFactory queryFactory;

    public PaginationResponse<QuestionSummary> pageByEmailAndCategory(String email, String category, Pageable pageable) {
        JPAQuery<Long> countQuery = queryFactory.select(subscribeQuestion.count())
                .from(subscribeQuestion)
                .join(subscribe).on(subscribeQuestion.subscribe.eq(subscribe))
                .where(eqEmail(email)
                        .and(subscribe.deletedAt.isNull())
                        .and(eqCategory(category))
                        .and(subscribeQuestion.isSuccess));
        JPAQuery<QuestionSummary> resultQuery = queryFactory.select(projectionQuestionSummary())
                .from(subscribeQuestion)
                .join(subscribe).on(subscribeQuestion.subscribe.eq(subscribe))
                .join(question).on(subscribeQuestion.question.eq(question))
                .where(eqEmail(email)
                        .and(subscribe.deletedAt.isNull())
                        .and(eqCategory(category))
                        .and(subscribeQuestion.isSuccess))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        appendOrderCondition(pageable, resultQuery);

        Page<QuestionSummary> pageResult = PageableExecutionUtils.getPage(resultQuery.fetch(), pageable, countQuery::fetchOne);
        return new PaginationResponse<>(pageResult.isLast(), (long) pageResult.getTotalPages(), pageResult.getContent());
    }

    private BooleanExpression eqEmail(String email) {
        return subscribe.email.eq(email);
    }

    private BooleanExpression eqCategory(String category) {
        if (category == null || "all".equalsIgnoreCase(category)) {
            return null;
        }

        return question.category.eq(QuestionCategory.from(category));
    }

    private void appendOrderCondition(Pageable pageable, JPAQuery<QuestionSummary> resultQuery) {
        for (Sort.Order order : pageable.getSort()) {
            PathBuilder<Question> entityPath = new PathBuilder<>(question.getType(), question.getMetadata());
            Expression orderExpression = entityPath.get(order.getProperty());
            OrderSpecifier orderSpecifier = new OrderSpecifier<>(order.isAscending() ? Order.ASC : Order.DESC, orderExpression);
            resultQuery.orderBy(orderSpecifier);
        }
    }

    private QQuestionSummary projectionQuestionSummary() {
        return new QQuestionSummary(
                question.id,
                question.title,
                question.content,
                question.customizedTitle,
                question.category.stringValue().lower()
        );
    }
}
