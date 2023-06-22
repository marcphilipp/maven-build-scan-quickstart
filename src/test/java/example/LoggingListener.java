package example;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.time.LocalTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LoggingListener implements TestExecutionListener {

	Map<TestIdentifier, Exception> inProgress = new ConcurrentHashMap<>();
	Set<TestIdentifier> done = ConcurrentHashMap.newKeySet();

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		inProgress.clear();
		done.clear();
	}

	@Override
	public void dynamicTestRegistered(TestIdentifier testIdentifier) {
		log("Dynamic Test Registered: %s", testIdentifier);
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		log("Execution Started: %s", testIdentifier);
		Exception currentStackTrace = stackTrace();
		Exception previousStackTrace = inProgress.put(testIdentifier, currentStackTrace);
		if (previousStackTrace != null) {
			log("[!!!] TestIdentifier has already been reported as started: %s", testIdentifier);
			logThrowable(previousStackTrace);
			logThrowable(currentStackTrace);
		}
		if (done.contains(testIdentifier)) {
			logWithThrowable("[!!!] TestIdentifier has already been reported as done: %s", stackTrace(), testIdentifier);
		}
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		log("Execution Skipped: %s", testIdentifier);
		Exception previousStackTrace = inProgress.get(testIdentifier);
		if (previousStackTrace != null) {
			log("[!!!] TestIdentifier has already been reported as started: %s", testIdentifier);
			logThrowable(previousStackTrace);
			logThrowable(stackTrace());
		}
		if (!done.add(testIdentifier)) {
			logWithThrowable("[!!!] TestIdentifier has already been reported as done: %s", stackTrace(), testIdentifier);
		}
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		logWithThrowable("Execution Finished: %s", testExecutionResult.getThrowable().orElse(null),
			testIdentifier);
		if (!done.add(testIdentifier)) {
			logWithThrowable("[!!!] TestIdentifier has already been reported as done: %s", stackTrace(), testIdentifier);
		}
		inProgress.remove(testIdentifier);
	}

	private static Exception stackTrace() {
		return new RuntimeException("stack trace at " + LocalTime.now());
	}

	private void log(String message, Object... args) {
		logWithThrowable(message, null, args);
	}

	private void logWithThrowable(String message, Throwable t, Object... args) {
		System.out.printf(message + "%n", args);
		logThrowable(t);
	}

	private static void logThrowable(Throwable t) {
		if (t != null) {
			t.printStackTrace(System.out);
		}
	}

}
