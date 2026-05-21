package org.squirrelframework.foundation.fsm.impl;

import org.junit.Test;
import org.squirrelframework.foundation.fsm.MvelScriptManager;

/**
 * Issue #67: when an MVEL action fails with a RuntimeException that has no
 * cause, the failure-logging branch in {@link MvelActionImpl#execute} must not
 * itself throw a NullPointerException. The original RuntimeException must be
 * the exception that propagates out.
 */
public class MvelActionImplTest {

    private static class CauselessFailureScriptManager implements MvelScriptManager {
        @Override
        public <T> T eval(String script, Object context, Class<T> returnType) {
            throw new RuntimeException("mvel failed");
        }

        @Override
        public void compile(String script) {
        }

        @Override
        public boolean evalBoolean(String script, Object context) {
            return false;
        }
    }

    @Test
    public void executeShouldRethrowOriginalExceptionWhenCauseIsNull() {
        ExecutionContext executionContext =
                new ExecutionContext(new CauselessFailureScriptManager(), Object.class, new Class<?>[0]);
        MvelActionImpl<?, ?, ?, ?> action = new MvelActionImpl<>("noop:::1 > 0", executionContext);

        try {
            action.execute(null, null, null, null, null);
        } catch (NullPointerException npe) {
            throw new AssertionError("failure logging threw NullPointerException instead of " +
                    "rethrowing the original RuntimeException", npe);
        } catch (RuntimeException expected) {
            // Original MVEL failure is expected to propagate unchanged.
        }
    }
}
