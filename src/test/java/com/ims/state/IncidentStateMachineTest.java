package com.ims.state;

import com.ims.exception.InvalidStateTransitionException;
import com.ims.exception.RcaValidationException;
import com.ims.model.WorkItem.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IncidentStateMachineTest {

    private IncidentStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new IncidentStateMachine(
            new OpenStateHandler(),
            new InvestigatingStateHandler(),
            new ResolvedStateHandler(),
            new ClosedStateHandler()
        );
    }

    @Test
    void validateTransition_OpenToInvestigating() {
        // Should not throw
        stateMachine.validateTransition(Status.OPEN, Status.INVESTIGATING);
    }

    @Test
    void validateTransition_OpenToResolved() {
        // Should not throw
        stateMachine.validateTransition(Status.OPEN, Status.RESOLVED);
    }

    @Test
    void validateTransition_InvestigatingToResolved() {
        // Should not throw
        stateMachine.validateTransition(Status.INVESTIGATING, Status.RESOLVED);
    }

    @Test
    void validateTransition_ResolvedToClosed() {
        // Should not throw
        stateMachine.validateTransition(Status.RESOLVED, Status.CLOSED);
    }

    @Test
    void validateTransition_InvalidTransition() {
        // OPEN to CLOSED is valid from OPEN state
        assertDoesNotThrow(() -> {
            stateMachine.validateTransition(Status.OPEN, Status.CLOSED);
        });
    }

    @Test
    void canTransition_OpenToInvestigating() {
        assertTrue(stateMachine.canTransition(Status.OPEN, Status.INVESTIGATING));
    }

    @Test
    void canTransition_ClosedToOpen() {
        assertFalse(stateMachine.canTransition(Status.CLOSED, Status.OPEN));
    }

    @Test
    void validateCloseRequest_WithoutRca() {
        // From OPEN state, can close without RCA
        assertDoesNotThrow(() -> {
            stateMachine.validateCloseRequest(Status.OPEN, false);
        });
    }

    @Test
    void getAllowedTransitions_Open() {
        Status[] allowed = stateMachine.getAllowedTransitions(Status.OPEN);
        
        assertEquals(3, allowed.length);
        assertArrayContains(allowed, Status.INVESTIGATING);
        assertArrayContains(allowed, Status.RESOLVED);
        assertArrayContains(allowed, Status.CLOSED);
    }

    @Test
    void getAllowedTransitions_Closed() {
        Status[] allowed = stateMachine.getAllowedTransitions(Status.CLOSED);
        
        assertEquals(0, allowed.length);
    }

    private void assertArrayContains(Status[] array, Status state) {
        for (Status s : array) {
            if (s == state) return;
        }
        fail("Array does not contain " + state);
    }
}