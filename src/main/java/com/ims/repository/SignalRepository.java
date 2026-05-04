package com.ims.repository;

import com.ims.model.Signal;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB Repository for Signal documents
 */
@Repository
public interface SignalRepository extends MongoRepository<Signal, String> {
    
    List<Signal> findByComponentId(String componentId);
    
    List<Signal> findByWorkItemId(String workItemId);
    
    List<Signal> findByComponentIdAndTimestampBetween(String componentId, 
                                                        Instant start, 
                                                        Instant end);
    
    @Query("{'componentId': ?0, 'timestamp': {$gte: ?1, $lte: ?2}}")
    List<Signal> findSignalsInTimeWindow(String componentId, Instant start, Instant end);
    
    @Query("{'workItemId': ?0, 'timestamp': {$gte: ?1, $lte: ?2}}")
    List<Signal> findByWorkItemIdInTimeWindow(String workItemId, Instant start, Instant end);
    
    Optional<Signal> findTopByComponentIdOrderByTimestampDesc(String componentId);
    
    @Query("{'componentId': ?0, 'timestamp': {$gte: ?1}}")
    List<Signal> findRecentByComponent(String componentId, Instant since);
    
    long countByIncidentId(String incidentId);
    
    @Query("{'timestamp': {$gte: ?0}}")
    List<Signal> findSignalsSince(Instant since);
    
    @Query(value = "{}", sort = "{'timestamp': -1}")
    List<Signal> findTop100ByOrderByTimestampDesc();
}