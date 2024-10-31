package maeilmail.subscribe.core;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.DistributedSupport;
import maeilmail.mail.MailMessage;
import maeilmail.mail.MailSender;
import maeilmail.question.QuestionCategory;
import maeilmail.question.QuestionSummary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class SendQuestionScheduler {

    private final MailSender mailSender;
    private final ChoiceQuestionPolicy choiceQuestionPolicy;
    private final SubscribeQuestionView subscribeQuestionView;
    private final SubscribeRepository subscribeRepository;
    private final DistributedSupport distributedSupport;


    private final String noticeTitle = "[매일메일] 감사 인사 및 안내 사항";
    private final String notice = """
                            안녕하세요, 매일메일을 이용해 주셔서 진심으로 감사드립니다. <br/>
                            2주밖에 되지 않은 부족함 많은 신생 서비스이지만, 관심을 가지고 지켜봐 주셔서 감사합니다. <br/>
                            여러분의 관심 덕분에 서비스가 빠르게 발전할 수 있었습니다. <br/>
                            앞으로도 초심 잃지 않고 열심히 개선해 나가겠습니다. <br/>
                            <br/>
                            새로운 소식 <br/>
                            1. 팀 소개 페이지 [https://bit.ly/maeil-mail-intro] <br/>
                            '매일메일이 왜 이런 서비스를 운영하는지', '어떤 사람들이 만들고 있는지' 등에 대해 궁금해하시는 분들이 계셔서 팀 소개 페이지를 제작했습니다. 혹시 매일메일에 대해 더 알고 싶다면 한 번 읽어봐 주시면 감사하겠습니다! 😄(Home에서도 팀 소개 페이지에 접근 가능합니다.) <br/>
                            <br/>
                            2. 꼬리 질문 컨텐츠 <br/>
                            답변 컨텐츠 제작 시, 깊이 있게 고민해볼 수 있는 꼬리 질문 관련 내용을 추가하고 있습니다. 아직 모든 질문에 적용하지는 못했지만, 추가하는 중이니 간간이 받아보실 수 있을 것 같습니다. 기대해주세요! <br/>
                            <br/>
                            3. 발송 수 증가로 인한 수동 발송 <br/>
                            현재 메일 발송 수가 예상보다 빠르게 증가하여 메일 발송 서버가 전송량을 감당하지 못하는 상황입니다. 빠르게 고치고 있지만, 처리에 어려움이 있어 다음 주 중으로 완성될 것으로 파악됩니다. 그동안은 인간 봇이 메일을 수동으로 보내드릴 예정입니다. 간혹 휴먼 에러가 발생할 수 있는 점 너른 양해 부탁드립니다. 최대한 빠르게 수정하도록 하겠습니다. 🙇🏻‍♂️ <br/>
                            <br/>
                            4. 여유를 위한 주말 미발송 <br/>
                            주말까지 면접 질문을 받는 것이 심적인 부담을 높이는 이유가 될 수 있겠다는 의견을 반영하여, 이번 주부터 주말에는 메일 발송을 쉬어가기로 했습니다. 주말은 다음주를 위해 잠시 쉬거나 복습을 하며 충분한 쉼을 취하셨으면 좋겠습니다. :) <br/>
                            <br/>
                            내일이면 주말이니 마지막 하루 무탈하게 보내시고 주말 간 푹 쉬시길 바라겠습니다. <br/>
                            긴 글 읽어주셔서 감사합니다. <br/>
                            매일메일 드림. <br/>
            """;

    @Scheduled(cron = "0 0 7 1/1 * ?", zone = "Asia/Seoul")
    public void sendMail() {
        log.info("메일 전송을 시작합니다.");
        LocalDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();
        List<Subscribe> subscribes = subscribeRepository.findAllByCreatedAtBefore(now);
        log.info("{}명의 사용자에게 메일을 전송합니다.", subscribes.size());

        subscribes.stream()
                .filter(it -> distributedSupport.isMine(it.getId()))
                .map(this::choiceQuestion)
                .filter(Objects::nonNull)
                .forEach(mailSender::sendMail);

        log.info("질문 전송을 종료합니다.");
    }

    @Scheduled(cron = "0 50 6 1/1 * ?", zone = "Asia/Seoul")
    public void sendNotice() {
        log.info("공지 전송을 시작합니다.");
        List<Subscribe> subscribes = subscribeRepository.findAll();
        Set<String> actualEmails = subscribes.stream()
                .map(Subscribe::getEmail)
                .collect(Collectors.toSet());

        log.info("{}명의 사용자에게 공지를 전송합니다.", actualEmails.size());

        actualEmails.stream()
                .map(this::createNotice)
                .forEach(mailSender::sendMail);

        log.info("공지 전송을 종료합니다.");
    }

    private MailMessage createNotice(String email) {
        return new MailMessage(email, noticeTitle, notice, "notice");
    }


    private MailMessage choiceQuestion(Subscribe subscribe) {
        try {
            QuestionSummary question = choiceQuestionPolicy.choice(subscribe, LocalDate.now());
            String subject = createSubject(question);
            String text = createText(question);
            return new MailMessage(subscribe.getEmail(), subject, text, createQuestionType(subscribe.getCategory()));
        } catch (Exception e) {
            log.info("면접 질문 선택 실패 = {}", e.getMessage());
            return null;
        }
    }

    private String createSubject(QuestionSummary question) {
        if (question.customizedTitle() == null) {
            return "오늘의 면접 질문을 보내드려요.";
        }

        return question.customizedTitle();
    }

    private String createText(QuestionSummary question) {
        HashMap<Object, Object> attribute = new HashMap<>();
        attribute.put("questionId", question.id());
        attribute.put("question", question.title());

        return subscribeQuestionView.render(attribute);
    }

    private String createQuestionType(QuestionCategory category) {
        String type = subscribeQuestionView.getType();
        if (QuestionCategory.BACKEND.equals(category)) {
            return type + "-backend";
        }

        return type + "-frontend";
    }
}
