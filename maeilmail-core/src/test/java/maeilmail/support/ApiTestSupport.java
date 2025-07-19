package maeilmail.support;

import maeilmail.admin.AdminApi;
import maeilmail.admin.AdminNoticeRepository;
import maeilmail.admin.AdminNoticeService;
import maeilmail.admin.AdminQuestionService;
import maeilmail.question.QuestionQueryService;
import maeilmail.subscribe.api.SubscribeQuestionApi;
import maeilmail.subscribe.query.SubscribeQuestionQueryService;
import maeilwiki.member.api.MemberIdentityCookieHelper;
import maeilwiki.member.infra.MemberTokenAuthorizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {SubscribeQuestionApi.class, AdminApi.class})
public abstract class ApiTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected QuestionQueryService questionQueryService;

    @MockBean
    protected SubscribeQuestionQueryService subscribeQuestionQueryService;

    @MockBean
    protected MemberTokenAuthorizer memberTokenAuthorizer;

    @MockBean
    protected MemberIdentityCookieHelper memberIdentityCookieHelper;

    @MockBean
    protected AdminQuestionService adminQuestionService;

    @MockBean
    protected AdminNoticeService adminNoticeService;

    @MockBean
    protected AdminNoticeRepository adminNoticeRepository;
}
