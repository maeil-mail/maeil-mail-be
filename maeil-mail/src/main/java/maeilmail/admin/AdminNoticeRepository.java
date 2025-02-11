package maeilmail.admin;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AdminNoticeRepository extends JpaRepository<AdminNotice, Long> {

    @Query("""
            select new maeilmail.admin.AdminNoticeResponse(n.id, n.title, n.content, n.reservedAt)
            from AdminNotice n
            """
    )
    List<AdminNoticeResponse> queryAll();
}
