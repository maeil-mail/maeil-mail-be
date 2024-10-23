package maeilmail.mail;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailEventRepository extends JpaRepository<MailEvent, Long> {

    List<MailEvent> findMailEventByCreatedAtBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);
}
