package maeilwiki.mutiplechoice.application;

import java.util.ArrayList;
import java.util.List;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MultipleChoiceService {

    private final MemberService memberService;
    private final WorkbookRepository workbookRepository;

    @Transactional
    public void create(MemberIdentity identity, WorkbookRequest request) {
        Member member = memberService.findById(identity.id());
        Workbook workBook = request.toWorkbook(member);
        workbookRepository.save(workBook);

        Questions questions = generateQuestions(request, workBook);
        workbookRepository.bulkSave(questions);
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
