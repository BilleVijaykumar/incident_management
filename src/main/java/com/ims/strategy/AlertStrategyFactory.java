package com.ims.strategy;

import com.ims.dto.SignalRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Alert Strategy Factory - Creates and manages alert strategies
 */
@Component
public class AlertStrategyFactory {
    
    private final Map<String, AlertStrategy> strategies;
    
    public AlertStrategyFactory(List<AlertStrategy> strategyList) {
        this.strategies = strategyList.stream()
            .collect(Collectors.toMap(
                s -> s.getComponentType().toUpperCase(),
                Function.identity()
            ));
    }
    
    /**
     * Get strategy for component type
     */
    public AlertStrategy getStrategy(String componentType) {
        AlertStrategy strategy = strategies.get(componentType.toUpperCase());
        if (strategy == null) {
            // Return a default strategy for unknown types
            return new DefaultAlertStrategy(componentType);
        }
        return strategy;
    }
    
    /**
     * Determine severity using appropriate strategy
     */
    public String determineSeverity(SignalRequest signal) {
        AlertStrategy strategy = getStrategy(signal.componentType());
        return strategy.determineSeverity(signal);
    }
    
    /**
     * Default strategy for unknown component types
     */
    private static class DefaultAlertStrategy implements AlertStrategy {
        
        private final String componentType;
        
        DefaultAlertStrategy(String componentType) {
            this.componentType = componentType;
        }
        
        @Override
        public String getPriority() {
            return "P3";
        }
        
        @Override
        public boolean isCritical() {
            return false;
        }
        
        @Override
        public String[] getNotificationChannels() {
            return new String[]{"slack"};
        }
        
        @Override
        public String determineSeverity(SignalRequest signal) {
            return "P3";
        }
        
        @Override
        public String getComponentType() {
            return componentType;
        }
    }
}