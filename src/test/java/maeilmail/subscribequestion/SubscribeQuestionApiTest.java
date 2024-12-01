package maeilmail.subscribequestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import maeilmail.PaginationResponse;
import maeilmail.question.QuestionSummary;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SubscribeQuestionApi.class)
class SubscribeQuestionApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubscribeQuestionQueryService subscribeQuestionQueryService;

    @Disabled
    @DisplayName("페이징 처리된 질문지를 조회할때 기본 값은 page = 0, pageSize = 10, category = 'all' 이다.")
    @Test
    void getSubscribeQuestionDefault() throws Exception {
        String email = "test@gmail.com";
        ArgumentCaptor<String> categoryCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        PaginationResponse<SubscribeQuestionSummary> response = new PaginationResponse<>(true, 0L, Collections.emptyList());

        when(subscribeQuestionQueryService.pageByEmailAndCategory(anyString(), anyString(), any()))
                .thenReturn(response);

        mockMvc.perform(get("/subscribe-question").queryParam("email", email))
                .andDo(print())
                .andExpect(status().isOk());

        verify(subscribeQuestionQueryService, times(1))
                .pageByEmailAndCategory(anyString(), categoryCaptor.capture(), pageableCaptor.capture());
        Pageable actualPageable = pageableCaptor.getValue();
        String actualCategory = categoryCaptor.getValue();

        assertAll(
                () -> assertThat(actualCategory).isEqualTo("all"),
                () -> assertThat(actualPageable.getPageSize()).isEqualTo(10),
                () -> assertThat(actualPageable.getPageNumber()).isEqualTo(0)
        );
    }
}
