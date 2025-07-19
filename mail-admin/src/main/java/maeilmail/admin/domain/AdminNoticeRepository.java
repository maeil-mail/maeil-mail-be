package maeilmail.admin.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import maeilmail.admin.application.AdminNoticeResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AdminNoticeRepository extends JpaRepository<AdminNotice, Long> {

    @Query("""
            select new maeilmail.admin.application.AdminNoticeResponse(n.id, n.title, n.content, n.reservedAt)
            from AdminNotice n
            """
    )
    List<AdminNoticeResponse> queryAll();

    Optional<AdminNotice> findByReservedAt(LocalDate reservedAt);
}
