package maeilmail.question;

import static maeilmail.question.QQuestion.question;

import java.util.List;
import java.util.NoSuchElementException;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import maeilmail.PaginationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionQueryService {

    private final JPAQueryFactory queryFactory;

    public PaginationResponse<QuestionSummary> pageByCategory(String category, Pageable pageable) {
        JPAQuery<Long> countQuery = queryFactory.select(question.count())
                .from(question)
                .where(eqCategory(category));
        JPAQuery<QuestionSummary> resultQuery = queryFactory.select(projectionQuestionSummary())
                .from(question)
                .where(eqCategory(category))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        appendOrderCondition(pageable, resultQuery);

        Page<QuestionSummary> pageResult = PageableExecutionUtils.getPage(resultQuery.fetch(), pageable, countQuery::fetchOne);
        return new PaginationResponse<>(pageResult.isLast(), (long) pageResult.getTotalPages(), pageResult.getContent());
    }

    private void appendOrderCondition(Pageable pageable, JPAQuery<QuestionSummary> resultQuery) {
        for (Sort.Order order : pageable.getSort()) {
            PathBuilder<Question> entityPath = new PathBuilder<>(question.getType(), question.getMetadata());
            Expression orderExpression = entityPath.get(order.getProperty());
            OrderSpecifier orderSpecifier = new OrderSpecifier<>(order.isAscending() ? Order.ASC : Order.DESC, orderExpression);
            resultQuery.orderBy(orderSpecifier);
        }
    }

    public List<QuestionSummary> queryAllByCategory(String category) {
        return queryFactory.select(projectionQuestionSummary())
                .from(question)
                .where(eqCategory(category))
                .orderBy(question.id.asc())
                .fetch();
    }

    private BooleanExpression eqCategory(String category) {
        if (category == null || "all".equalsIgnoreCase(category)) {
            return null;
        }

        return question.category.eq(QuestionCategory.from(category));
    }

    public QuestionSummary queryOneById(Long id) {
        QuestionSummary result = queryFactory.select(projectionQuestionSummary())
                .from(question)
                .where(question.id.eq(id))
                .fetchOne();

        return requireExist(result);
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

    private QuestionSummary requireExist(QuestionSummary result) {
        if (result == null) {
            throw new NoSuchElementException();
        }

        return result;
    }
}
