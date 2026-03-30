package com.tracker.github.repository;

import com.tracker.github.entity.Commit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitRepository extends JpaRepository<Commit, Long> {
}
