package maeilmail;

import java.util.List;

public record PaginationResponse<T>(boolean isLastPage, List<T> data) {
}
