package maeilmail.subscribe.command.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscribeRepository extends JpaRepository<Subscribe, Long> {

    Optional<Subscribe> findByEmailAndTokenAndDeletedAtIsNull(String email, String token);

    List<Subscribe> findAllByEmailAndDeletedAtIsNull(String email);
}
