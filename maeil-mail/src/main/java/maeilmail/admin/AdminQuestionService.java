package maeilmail.admin;

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import maeilmail.question.Question;
import maeilmail.question.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class AdminQuestionService {

    private final QuestionRepository questionRepository;

    @Transactional
    public void createQuestion(Question question) {
        questionRepository.save(question);
    }

    @Transactional
    public void updateQuestion(Question question) {
        Question found = questionRepository.findById(question.getId())
                .orElseThrow(NoSuchElementException::new);
        found.setTitle(question.getTitle());
        found.setContent(question.getContent());
        found.setCustomizedTitle(question.getCustomizedTitle());
        found.setCategory(question.getCategory());
    }
}
