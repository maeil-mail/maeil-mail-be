package maeilmail.admin;

import org.springframework.data.jpa.repository.JpaRepository;

interface AdminNoticeRepository extends JpaRepository<AdminNotice, Long> {
}
