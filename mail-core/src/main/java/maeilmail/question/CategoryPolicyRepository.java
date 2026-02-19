package maeilmail.question;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryPolicyRepository extends JpaRepository<CategoryPolicy, Long> {
    Optional<CategoryPolicy> findByCategory(QuestionCategory category);
}
