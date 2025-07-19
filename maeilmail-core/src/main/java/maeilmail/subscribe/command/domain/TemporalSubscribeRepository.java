package maeilmail.subscribe.command.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemporalSubscribeRepository extends JpaRepository<TemporalSubscribe, Long> {

    Optional<TemporalSubscribe> findByEmail(String email);
}
