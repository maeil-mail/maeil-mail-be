package maeilmail.mail;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailEventRepository extends JpaRepository<MailEvent, Long> {

    List<MailEvent> findMailEventByDate(LocalDate date);
}
