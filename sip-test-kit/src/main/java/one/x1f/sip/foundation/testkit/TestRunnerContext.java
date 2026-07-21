package one.x1f.sip.foundation.testkit;

import static one.x1f.sip.foundation.testkit.util.TestKitHelper.createGraalJSContext;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.graalvm.polyglot.Context;
import org.springframework.stereotype.Service;

@Service
public class TestRunnerContext {
  private final ConcurrentHashMap<UUID, Context> contexts = new ConcurrentHashMap<>();

  public Context getOrCreate(UUID executionId) {
    return contexts.computeIfAbsent(executionId, id -> createGraalJSContext());
  }

  public void remove(UUID executionId) {
    Context ctx = contexts.remove(executionId);
    if (ctx != null) ctx.close();
  }
}
