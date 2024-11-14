package maeilmail.mail;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MailEventRepository extends JpaRepository<MailEvent, Long> {
}
