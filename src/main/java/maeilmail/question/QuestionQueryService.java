package maeilmail.question;

import java.util.NoSuchElementException;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class QuestionQueryService {

    private final JPAQueryFactory queryFactory;

    public QuestionSummary queryOneById(Long id) {
        QQuestion question = QQuestion.question;
        QuestionSummary result = queryFactory.select(getExpr(question))
                .from(question)
                .where(question.id.eq(id))
                .fetchOne();

        return requireExist(result);
    }

    private QQuestionSummary getExpr(QQuestion question) {
        return new QQuestionSummary(
                question.id,
                question.title,
                question.content,
                question.category.stringValue().toLowerCase()
        );
    }

    private QuestionSummary requireExist(QuestionSummary result) {
        if (result == null) {
            throw new NoSuchElementException();
        }

        return result;
    }
}
