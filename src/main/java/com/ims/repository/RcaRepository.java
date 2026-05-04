package com.ims.repository;

import com.ims.model.RCA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for RCA entity - PostgreSQL
 */
@Repository
public interface RcaRepository extends JpaRepository<RCA, Long> {
    
    Optional<RCA> findByRcaId(String rcaId);
    
    Optional<RCA> findByWorkItemId(Long workItemId);
    
    boolean existsByWorkItemId(Long workItemId);
    
    @Query("SELECT r FROM RCA r WHERE r.workItemId = :workItemId")
    Optional<RCA> findByWorkItemIdWithWorkItem(@Param("workItemId") Long workItemId);
    
    @Query("SELECT r FROM RCA r WHERE r.submittedAt >= :since ORDER BY r.submittedAt DESC")
    List<RCA> findRecentRcas(@Param("since") java.time.Instant since);
    
    @Query("SELECT COUNT(r) FROM RCA r WHERE r.submittedAt >= :since")
    long countRecentRcas(@Param("since") java.time.Instant since);
}