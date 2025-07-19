package maeilmail.support;

import maeilmail.admin.AdminApi;
import maeilmail.admin.AdminNoticeRepository;
import maeilmail.admin.AdminNoticeService;
import maeilmail.admin.AdminQuestionService;
import maeilmail.question.QuestionQueryService;
import maeilmail.subscribe.query.SubscribeQuestionQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {AdminApi.class})
public abstract class ApiTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected QuestionQueryService questionQueryService;

    @MockBean
    protected SubscribeQuestionQueryService subscribeQuestionQueryService;

    @MockBean
    protected AdminQuestionService adminQuestionService;

    @MockBean
    protected AdminNoticeService adminNoticeService;

    @MockBean
    protected AdminNoticeRepository adminNoticeRepository;
}
