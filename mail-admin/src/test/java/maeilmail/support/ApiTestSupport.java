package maeilmail.support;

import maeilmail.admin.api.AdminApi;
import maeilmail.admin.application.AdminNoticeService;
import maeilmail.admin.application.AdminQuestionService;
import maeilmail.admin.domain.AdminNoticeRepository;
import maeilmail.question.QuestionQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {AdminApi.class})
@Import(AdminWebMvcConfig.class)
public abstract class ApiTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected QuestionQueryService questionQueryService;

    @MockitoBean
    protected AdminQuestionService adminQuestionService;

    @MockitoBean
    protected AdminNoticeService adminNoticeService;

    @MockitoBean
    protected AdminNoticeRepository adminNoticeRepository;
}
