package maeilmail.support;

import maeilmail.question.QuestionApi;
import maeilmail.question.QuestionQueryService;
import maeilmail.subscribe.api.SubscribeQuestionApi;
import maeilmail.subscribe.query.SubscribeQuestionQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {SubscribeQuestionApi.class, QuestionApi.class})
public abstract class ApiTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected QuestionQueryService questionQueryService;

    @MockBean
    protected SubscribeQuestionQueryService subscribeQuestionQueryService;
}
