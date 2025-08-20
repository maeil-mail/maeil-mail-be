package maeilmail.subscribe.command.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemporalSubscribeRepository extends JpaRepository<TemporalSubscribe, Long> {

    List<TemporalSubscribe> findAllByEmail(String email);

    void removeAllByIdIn(List<Long> ids);
}
