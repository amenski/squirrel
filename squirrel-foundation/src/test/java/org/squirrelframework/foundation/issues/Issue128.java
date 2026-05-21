package org.squirrelframework.foundation.issues;

import org.junit.Assert;
import org.junit.Test;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.annotation.ContextInsensitive;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

/**
 * Issue #128: when the state machine is in a nested state, canAccept must
 * return true for events handled by a transition defined on an ancestor
 * state, because internalFire delegates declined events up the parent chain.
 */
public class Issue128 {

    enum Issue128State {A, A1, A2}

    enum Issue128Event {PARENT_EVENT, CHILD_EVENT, UNKNOWN}

    @ContextInsensitive
    @StateMachineParameters(stateType = Issue128State.class, eventType = Issue128Event.class, contextType = Void.class)
    static class Issue128StateMachine extends AbstractUntypedStateMachine {
    }

    private Issue128StateMachine buildMachine() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(Issue128StateMachine.class);
        builder.defineSequentialStatesOn(Issue128State.A, Issue128State.A1, Issue128State.A2);
        builder.internalTransition().within(Issue128State.A).on(Issue128Event.PARENT_EVENT);
        builder.localTransition().from(Issue128State.A1).to(Issue128State.A2).on(Issue128Event.CHILD_EVENT);
        return builder.newUntypedStateMachine(Issue128State.A);
    }

    @Test
    public void canAcceptShouldFindEventHandledByParentStateWhileInChildState() {
        Issue128StateMachine fsm = buildMachine();
        fsm.start();
        Assert.assertEquals(Issue128State.A1, fsm.getCurrentState());

        Assert.assertTrue("event handled by child state must be acceptable",
                fsm.canAccept(Issue128Event.CHILD_EVENT));
        Assert.assertTrue("event handled by parent state must be acceptable while in child state",
                fsm.canAccept(Issue128Event.PARENT_EVENT));
        Assert.assertFalse("unknown event must not be acceptable",
                fsm.canAccept(Issue128Event.UNKNOWN));
    }
}
