package maeilmail.question;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query(value = """
            select new maeilmail.question.QuestionSummary(q)
            from Question q
            where
                upper(:categoryName) = 'ALL' OR
                upper(:categoryName) = q.category
            """)
    List<QuestionSummary> queryAll(String categoryName);

    List<Question> findAllByCategoryOrderByIdAsc(QuestionCategory category);
}
