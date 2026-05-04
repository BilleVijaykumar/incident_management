package com.ims.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "signals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Signal {
    
    @Id
    private String id;
    
    private String componentId;
    
    private String componentType;
    
    private String severity;
    
    private String message;
    
    private Instant timestamp;
    
    private String workItemId;
    
    private Instant processedAt;
}