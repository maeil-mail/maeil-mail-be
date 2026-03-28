package maeilbatch.forward;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ForwardRepository extends JpaRepository<ForwardLog, Long> {
}
