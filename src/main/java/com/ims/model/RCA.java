package com.ims.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "rcas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RCA {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_item_id", nullable = false, unique = true)
    private WorkItem workItem;
    
    @Column(nullable = false)
    private String rootCauseCategory;
    
    @Column(nullable = false)
    private Instant incidentStartTime;
    
    @Column(nullable = false)
    private Instant incidentEndTime;
    
    @Column(length = 4000)
    private String fixApplied;
    
    @Column(length = 4000)
    private String preventionSteps;
    
    @Column(nullable = false)
    private Instant submittedAt;
    
    private boolean complete;
    
    public boolean isComplete() {
        return rootCauseCategory != null && !rootCauseCategory.isBlank()
            && incidentStartTime != null && incidentEndTime != null
            && fixApplied != null && !fixApplied.isBlank()
            && preventionSteps != null && !preventionSteps.isBlank();
    }
}