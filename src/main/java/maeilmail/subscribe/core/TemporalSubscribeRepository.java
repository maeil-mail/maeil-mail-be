package maeilmail.subscribe.core;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface TemporalSubscribeRepository extends JpaRepository<TemporalSubscribe, Long> {

    Optional<TemporalSubscribe> findByEmail(String email);
}
