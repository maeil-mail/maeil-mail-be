package maeilmail.admin.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import jakarta.servlet.ServletException;
import maeilmail.PaginationResponse;
import maeilmail.question.QuestionSummary;
import maeilmail.support.ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;

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

    @Test
    @DisplayName("Authorization 헤더가 없으면 인증에 실패한다.")
    void authFailWhenNoAuthorizationHeader() throws Exception {
        assertThrows(ServletException.class, () ->
                mockMvc.perform(get("/admin/question"))
                        .andDo(print()));
    }

    @Test
    @DisplayName("Authorization 헤더에 'Basic ' 접두사가 없으면 인증에 실패한다.")
    void authFailWhenMissingBasicPrefix() throws Exception {
        assertThrows(ServletException.class, () ->
                mockMvc.perform(get("/admin/question"))
                        .andDo(print()));
    }

    @Test
    @DisplayName("잘못된 시크릿이면 인증에 실패한다.")
    void authFailWhenWrongSecret() throws Exception {
        assertThrows(ServletException.class, () ->
                mockMvc.perform(get("/admin/question"))
                        .andDo(print()));
    }
}
