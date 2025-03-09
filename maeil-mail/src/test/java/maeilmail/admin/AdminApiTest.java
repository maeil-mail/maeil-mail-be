package maeilmail.admin;

import maeilmail.question.QuestionSummary;
import maeilmail.support.ApiTestSupport;
import maeilsupport.PaginationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminApiTest extends ApiTestSupport {

    @Value("${admin.secret}")
    private String secret;

    @Test
    @DisplayName("페이징 처리된 질문지를 조회할때 기본 값은 page = 0, pageSize = 10, category = 'all' 이다.")
    void getQuestionsDefault() throws Exception {
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        ArgumentCaptor<String> categoryCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> searchParamCaptor = ArgumentCaptor.forClass(String.class);
        PaginationResponse<QuestionSummary> response = new PaginationResponse<>(true, 0L, Collections.emptyList());
        when(questionQueryService.queryAllByCategoryAndSearchParam(any(), any(), any()))
                .thenReturn(response);

        mockMvc.perform(get("/admin/question").header("Authorization", "Basic " + secret))
                .andDo(print())
                .andExpect(status().isOk());

        verify(questionQueryService, times(1))
                .queryAllByCategoryAndSearchParam(categoryCaptor.capture(), searchParamCaptor.capture(), pageableCaptor.capture());
        Pageable actualPageable = pageableCaptor.getValue();
        String actualCategory = categoryCaptor.getValue();
        String actualSearchParam = searchParamCaptor.getValue();

        assertAll(
                () -> assertThat(actualCategory).isEqualTo("all"),
                () -> assertThat(actualSearchParam).isEqualTo(""),
                () -> assertThat(actualPageable.getPageSize()).isEqualTo(10),
                () -> assertThat(actualPageable.getPageNumber()).isEqualTo(0)
        );
    }
}
