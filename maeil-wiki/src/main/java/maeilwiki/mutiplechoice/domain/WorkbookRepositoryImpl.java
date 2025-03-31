package maeilwiki.mutiplechoice.domain;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
class WorkbookRepositoryImpl implements WorkbookRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void bulkSave(Questions questions) {
        List<Object> keys = bulkSaveQuestions(questions);
        bulkSaveOptions(questions.options(), keys);
    }

    private List<Object> bulkSaveQuestions(Questions questions) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        List<WorkbookQuestion> questionEntities = questions.questions();

        jdbcTemplate.batchUpdate(questionPsc(), questionsPss(questionEntities), keyHolder);

        List<Map<String, Object>> keyList = keyHolder.getKeyList();
        return keyList.stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .toList();
    }

    private PreparedStatementCreator questionPsc() {
        String sql = """
                insert into multiple_choice_question (title, correct_answer_explanation, created_at, updated_at, workbook_id) 
                values (?, ?, ?, ?, ?)
                """;

        return con -> con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    }

    private BatchPreparedStatementSetter questionsPss(List<WorkbookQuestion> questionEntities) {
        return new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                WorkbookQuestion question = questionEntities.get(i);
                Workbook workbook = question.getWorkbook();
                LocalDateTime now = LocalDateTime.now();
                ps.setString(1, question.getTitle());
                ps.setString(2, question.getCorrectAnswerExplanation());
                ps.setObject(3, now);
                ps.setObject(4, now);
                ps.setLong(5, workbook.getId());
            }

            @Override
            public int getBatchSize() {
                return questionEntities.size();
            }
        };
    }

    private void bulkSaveOptions(List<Options> optionsList, List<Object> keys) {
        String sql = """
                insert into multiple_choice_option (content, is_correct_answer, created_at, updated_at, question_id) 
                values (?, ?, ?, ?, ?)
                """;

        List<Object[]> batchArgs = createOptionBatchArgs(optionsList, keys);

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private List<Object[]> createOptionBatchArgs(List<Options> optionsList, List<Object> keys) {
        List<Object[]> batchArgs = new ArrayList<>();
        int size = optionsList.size();

        for (int i = 0; i < size; i++) {
            Options options = optionsList.get(i);
            Long questionId = (Long) keys.get(i);
            List<Option> optionEntities = options.options();

            optionEntities.stream()
                    .map(it -> mapToOptionBatchArg(it, questionId))
                    .forEach(batchArgs::add);
        }

        return batchArgs;
    }

    private Object[] mapToOptionBatchArg(Option option, Long questionId) {
        LocalDateTime now = LocalDateTime.now();

        return new Object[]{option.getContent(), option.isCorrectAnswer(), now, now, questionId};
    }
}
