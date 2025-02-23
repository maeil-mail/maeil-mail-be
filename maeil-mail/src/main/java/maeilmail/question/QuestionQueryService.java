package maeilmail.question;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilsupport.PaginationResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

import static maeilmail.question.QQuestion.question;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionQueryService {

    private final JPAQueryFactory queryFactory;

    public PaginationResponse<QuestionSummary> pageByCategory(String category, String searchParam, Pageable pageable) {
        JPAQuery<Long> countQuery = queryFactory.select(question.count())
                .from(question)
                .where(eqCategory(category));
        JPAQuery<QuestionSummary> resultQuery = queryFactory.select(projectionQuestionSummary())
                .from(question)
                .where(eqCategory(category), eqSearchParam(searchParam))
                .offset(pageable.getOffset())
                .orderBy(question.id.desc())
                .limit(pageable.getPageSize());

        Page<QuestionSummary> pageResult = PageableExecutionUtils.getPage(resultQuery.fetch(), pageable, countQuery::fetchOne);
        return new PaginationResponse<>(pageResult.isLast(), (long) pageResult.getTotalPages(), pageResult.getContent());
    }

    @Cacheable(key = "#category", cacheNames = {"question"})
    public List<QuestionSummary> queryAllByCategory(String category) {
        return queryFactory.select(projectionQuestionSummary())
                .from(question)
                .where(question.category.eq(QuestionCategory.from(category)))
                .orderBy(question.id.asc())
                .fetch();
    }

    private BooleanExpression eqCategory(String category) {
        if (category == null || "all".equalsIgnoreCase(category)) {
            return null;
        }

        return question.category.eq(QuestionCategory.from(category));
    }

    private BooleanExpression eqSearchParam(String searchParam) {
        if (searchParam == null || searchParam.isEmpty()) {
            return null;
        }
        return question.title.likeIgnoreCase(searchParam)
                .or(question.content.likeIgnoreCase(searchParam));
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
                question.category.stringValue().lower(),
                question.createdAt,
                question.updatedAt
        );
    }

    private QuestionSummary requireExist(QuestionSummary result) {
        if (result == null) {
            throw new NoSuchElementException();
        }

        return result;
    }
}
