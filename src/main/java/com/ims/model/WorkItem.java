package com.ims.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * WorkItem Entity - JPA entity for work item management
 * Stored in PostgreSQL
 */
@Entity
@Table(name = "work_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "work_item_id", unique = true, nullable = false)
    private String workItemId;
    
    @Column(nullable = false)
    private String componentId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "signal_count")
    private Long signalCount;
    
    /**
     * Severity levels for work items
     */
    public enum Severity {
        P0, P1, P2, P3, P4
    }
    
    /**
     * Work item lifecycle states
     */
    public enum Status {
        OPEN,
        INVESTIGATING,
        RESOLVED,
        CLOSED
    }
}