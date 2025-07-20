package maeilmail.support;

import maeilmail.admin.api.AdminApi;
import maeilmail.admin.application.AdminNoticeService;
import maeilmail.admin.application.AdminQuestionService;
import maeilmail.admin.domain.AdminNoticeRepository;
import maeilmail.question.QuestionQueryService;
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
    protected AdminQuestionService adminQuestionService;

    @MockBean
    protected AdminNoticeService adminNoticeService;

    @MockBean
    protected AdminNoticeRepository adminNoticeRepository;
}
