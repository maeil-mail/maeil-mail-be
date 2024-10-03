package maeilmail.question;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class QuestionQueryService {

    public QuestionSummary queryOneById(Long id) {

        return null;
    }
}
