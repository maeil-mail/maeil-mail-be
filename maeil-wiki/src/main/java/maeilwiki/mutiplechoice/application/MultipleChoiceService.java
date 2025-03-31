package maeilwiki.mutiplechoice.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import maeilwiki.member.application.MemberIdentity;
import maeilwiki.member.application.MemberService;
import maeilwiki.member.domain.Member;
import maeilwiki.mutiplechoice.domain.Option;
import maeilwiki.mutiplechoice.domain.Options;
import maeilwiki.mutiplechoice.domain.Questions;
import maeilwiki.mutiplechoice.domain.Workbook;
import maeilwiki.mutiplechoice.domain.WorkbookQuestion;
import maeilwiki.mutiplechoice.domain.WorkbookRepository;
import maeilwiki.mutiplechoice.dto.OptionSummary;
import maeilwiki.mutiplechoice.dto.WorkbookQuestionSummary;
import maeilwiki.mutiplechoice.dto.WorkbookSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MultipleChoiceService {

    private final MemberService memberService;
    private final JdbcQuestionsBatchInsertManager jdbcQuestionsBatchInsertManager;
    private final WorkbookRepository workbookRepository;

    @Transactional(readOnly = true)
    public WorkbookResponse getWorkbookById(Long workbookId) {
        WorkbookSummary workbookSummary = workbookRepository.queryOneById(workbookId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 객관식 문제집입니다."));
        List<QuestionResponse> questionResponses = findQuestions(workbookId);

        return WorkbookResponse.withQuestions(workbookSummary, questionResponses);
    }

    private List<QuestionResponse> findQuestions(Long workbookId) {
        List<WorkbookQuestionSummary> workbookQuestionSummaries = workbookRepository.queryQuestionsByWorkbookId(workbookId);
        List<Long> questionIds = workbookQuestionSummaries.stream()
                .map(WorkbookQuestionSummary::id)
                .toList();
        Map<Long, List<OptionSummary>> bundledOptions = findOptions(questionIds);

        return workbookQuestionSummaries.stream()
                .map(buildQuestions(bundledOptions))
                .toList();
    }

    private Map<Long, List<OptionSummary>> findOptions(List<Long> questionIds) {
        List<OptionSummary> optionSummaries = workbookRepository.queryOptionsByQuestionIdsIn(questionIds);

        return optionSummaries.stream()
                .collect(Collectors.groupingBy(OptionSummary::questionId));
    }

    private Function<WorkbookQuestionSummary, QuestionResponse> buildQuestions(Map<Long, List<OptionSummary>> bundledOptions) {
        return questionSummary -> {
            List<OptionSummary> options = bundledOptions.get(questionSummary.id());
            List<OptionResponse> optionResponses = options.stream()
                    .map(OptionResponse::from)
                    .toList();

            return QuestionResponse.withOptions(questionSummary, optionResponses);
        };
    }

    @Transactional
    public WorkbookCreatedResponse create(MemberIdentity identity, WorkbookRequest request) {
        Member member = memberService.findById(identity.id());
        Workbook workBook = request.toWorkbook(member);
        workbookRepository.save(workBook);

        Questions questions = generateQuestions(request, workBook);
        jdbcQuestionsBatchInsertManager.batchInsert(questions);

        return new WorkbookCreatedResponse(workBook.getId());
    }

    private Questions generateQuestions(WorkbookRequest workBookRequest, Workbook workBook) {
        List<Options> options = new ArrayList<>();
        List<WorkbookQuestion> questions = new ArrayList<>();

        for (QuestionRequest questionRequest : workBookRequest.questions()) {
            WorkbookQuestion question = questionRequest.toQuestion(workBook);
            questions.add(question);
            options.add(generateOptions(questionRequest, question));
        }

        return new Questions(questions, options);
    }

    private Options generateOptions(QuestionRequest questionRequest, WorkbookQuestion question) {
        List<OptionRequest> optionsRequests = questionRequest.options();
        List<Option> options = optionsRequests.stream()
                .map(it -> it.toOption(question))
                .toList();

        return new Options(options);
    }
}
