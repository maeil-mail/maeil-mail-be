package maeilmail.subscribequestion;

import static maeilmail.question.QQuestion.question;
import static maeilmail.subscribe.QSubscribe.subscribe;
import static maeilmail.subscribequestion.QSubscribeQuestion.subscribeQuestion;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import maeilmail.PaginationResponse;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
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

    public WeeklySubscribeQuestionResponse queryWeeklyQuestions(String email, String category, Long year, Long month, Long week) {
        LocalDate firstDayOfMonth = LocalDate.of(Math.toIntExact(year), Math.toIntExact(month), 1);
        LocalDate firstMonday = firstDayOfMonth.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
        LocalDate baseDateStart = firstMonday.plusWeeks(week - 1);
        LocalDate baseDateEnd = baseDateStart.plusDays(1);
        List<WeeklySubscribeQuestionSummary> result = queryFactory.select(projectionWeeklySubscribeQuestionSummary())
                .from(subscribeQuestion)
                .join(subscribe).on(subscribeQuestion.subscribe.eq(subscribe))
                .where(eqEmail(email)
                        .and(subscribe.deletedAt.isNull())
                        .and(eqCategory(category))
                        .and(subscribeQuestion.isSuccess)
                        .and(subscribeQuestion.createdAt.between(baseDateStart.atStartOfDay(), baseDateEnd.atStartOfDay())))
                .fetch();
        int size = result.size();
        for (int i = 0; i < size; i++) {
            result.get(i).setIndex((long) i + 1);
        }
        String weekLabel = month + "월 " + week + "주차";

        return new WeeklySubscribeQuestionResponse(weekLabel, result);
    }

    public PaginationResponse<SubscribeQuestionSummary> pageByEmailAndCategory(String email, String category, Pageable pageable) {
        JPAQuery<Long> countQuery = queryFactory.select(subscribeQuestion.count())
                .from(subscribeQuestion)
                .join(subscribe).on(subscribeQuestion.subscribe.eq(subscribe))
                .where(eqEmail(email)
                        .and(subscribe.deletedAt.isNull())
                        .and(eqCategory(category))
                        .and(subscribeQuestion.isSuccess));

        JPAQuery<SubscribeQuestionSummary> resultQuery = queryFactory.select(projectionSubscribeQuestionSummary())
                .from(subscribeQuestion)
                .join(subscribe).on(subscribeQuestion.subscribe.eq(subscribe))
                .join(question).on(subscribeQuestion.question.eq(question))
                .where(eqEmail(email)
                        .and(subscribe.deletedAt.isNull())
                        .and(eqCategory(category))
                        .and(subscribeQuestion.isSuccess))
                .offset(pageable.getOffset())
                .orderBy(subscribeQuestion.id.desc())
                .limit(pageable.getPageSize());

        appendOrderCondition(pageable, resultQuery);

        Page<SubscribeQuestionSummary> pageResult = PageableExecutionUtils.getPage(resultQuery.fetch(), pageable, countQuery::fetchOne);
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

    private void appendOrderCondition(Pageable pageable, JPAQuery<SubscribeQuestionSummary> resultQuery) {
        for (Sort.Order order : pageable.getSort()) {
            PathBuilder<Question> entityPath = new PathBuilder<>(question.getType(), question.getMetadata());
            Expression orderExpression = entityPath.get(order.getProperty());
            OrderSpecifier orderSpecifier = new OrderSpecifier<>(order.isAscending() ? Order.ASC : Order.DESC, orderExpression);
            resultQuery.orderBy(orderSpecifier);
        }
    }

    private QSubscribeQuestionSummary projectionSubscribeQuestionSummary() {
        return new QSubscribeQuestionSummary(
                question.id,
                question.title,
                question.category.stringValue().lower()
        );
    }

    private QWeeklySubscribeQuestionSummary projectionWeeklySubscribeQuestionSummary() {
        return new QWeeklySubscribeQuestionSummary(
                question.id,
                question.title
        );
    }
}
