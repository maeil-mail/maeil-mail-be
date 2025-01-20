package maeilmail.support;

import java.util.List;

public record PaginationResponse<T>(Boolean isLastPage, Long totalPage, List<T> data) {
}
