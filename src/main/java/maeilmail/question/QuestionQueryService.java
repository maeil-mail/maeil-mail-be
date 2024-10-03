package maeilmail.question;

import static maeilmail.question.QQuestion.question;

import java.util.List;
import java.util.NoSuchElementException;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class QuestionQueryService {

    private final JPAQueryFactory queryFactory;

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
