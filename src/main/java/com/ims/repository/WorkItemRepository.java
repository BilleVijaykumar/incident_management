package com.ims.repository;

import com.ims.model.WorkItem;
import com.ims.model.WorkItem.Status;
import com.ims.model.WorkItem.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for WorkItem entity - PostgreSQL
 */
@Repository
public interface WorkItemRepository extends JpaRepository<WorkItem, Long> {
    
    Optional<WorkItem> findByWorkItemId(String workItemId);
    
    List<WorkItem> findByStatus(Status status);
    
    List<WorkItem> findByStatusIn(List<Status> statuses);
    
    List<WorkItem> findBySeverity(Severity severity);
    
    List<WorkItem> findByComponentId(String componentId);
    
    @Query("SELECT w FROM WorkItem w WHERE w.status IN :statuses ORDER BY w.createdAt DESC")
    List<WorkItem> findActiveWorkItems(@Param("statuses") List<Status> statuses);
    
    @Query("SELECT COUNT(w) FROM WorkItem w WHERE w.status = :status")
    long countByStatus(@Param("status") Status status);
    
    @Query("SELECT w.severity, COUNT(w) FROM WorkItem w WHERE w.status IN :statuses GROUP BY w.severity")
    List<Object[]> countBySeverityAndStatus(@Param("statuses") List<Status> statuses);
}