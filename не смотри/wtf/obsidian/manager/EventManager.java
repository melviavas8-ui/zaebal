package wtf.obsidian.manager;

import wtf.obsidian.*;
import wtf.obsidian.manager.*;
import wtf.obsidian.utils.*;
import wtf.obsidian.engine.*;
import wtf.obsidian.event.*;
import wtf.obsidian.commands.*;
import wtf.obsidian.settings.*;
import wtf.obsidian.hud.*;
import wtf.obsidian.modules.combat.*;
import wtf.obsidian.modules.movement.*;
import wtf.obsidian.modules.render.*;
import wtf.obsidian.modules.player.*;
import wtf.obsidian.modules.misc.*;
import wtf.obsidian.mixin.*;
import wtf.obsidian.Module;
import wtf.obsidian.event.Event;

























import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public class EventManager {
   private static final ConcurrentHashMap<Class<?>, Object> classRegistry = new ConcurrentHashMap<>();
   private static final ConcurrentHashMap<Class<? extends Event>, List<ObsidianComponent_327>> invocationCache = new ConcurrentHashMap<>();
   private static final ConcurrentHashMap<String, Long> slowHandlerWarnings = new ConcurrentHashMap<>();
   private static final ConcurrentHashMap<String, Long> slowEventWarnings = new ConcurrentHashMap<>();
   private static final boolean PERF_DEBUG = Boolean.parseBoolean(System.getProperty("obsidian.perf.debug", "false"));
   private static final long SLOW_HANDLER_NANOS = Long.getLong("obsidian.perf.handlerMs", 8L)
      * (1000000L);
   private static final long SLOW_EVENT_NANOS = Long.getLong("obsidian.perf.eventMs", 18L)
      * (1000000L);
   private static final long WARN_COOLDOWN_NANOS = Long.getLong("obsidian.perf.cooldownMs", 1000L)
      * (1000000L);
   public static volatile boolean cacheDirty = true;

   public static void register(Object obj) {
      classRegistry.putIfAbsent(obj.getClass(), obj);
      cacheDirty = true;
   }

   public static void unregister(Object obj) {
      classRegistry.remove(obj.getClass());
      cacheDirty = true;
   }

   public static void clean() {
      classRegistry.clear();
      invocationCache.clear();
      cacheDirty = false;
   }

   public static void invoke(Event event) throws IllegalAccessException, InvocationTargetException, InstantiationException {
      long eventStart = PERF_DEBUG ? System.nanoTime() : 0L;
      if (cacheDirty) {
         rebuildCache();
      }

      List<ObsidianComponent_327> invocations = invocationCache.get(event.getClass());
      if (invocations != null && !invocations.isEmpty()) {
         for (ObsidianComponent_327 invocation : invocations) {
            if (classRegistry.containsKey(invocation.listener().getClass())) {
               Method method = invocation.method();
               method.setAccessible(true);
               long handlerStart = PERF_DEBUG ? System.nanoTime() : 0L;

               try {
                  Object var10001 = invocation.listener();
                  Object[] var10002 = new Object[1];
                  var10002[0] = event;
                  method.invoke(var10001, var10002);
               } finally {
                  if (PERF_DEBUG) {
                     long elapsed = System.nanoTime() - handlerStart;
                     if (elapsed >= SLOW_HANDLER_NANOS) {
                        logSlowHandler(event, invocation, elapsed);
                     }
                  }
               }
            }
         }

         if (PERF_DEBUG) {
            long elapsed = System.nanoTime() - eventStart;
            if (elapsed >= SLOW_EVENT_NANOS) {
               logSlowEvent(event, elapsed, invocations.size());
            }
         }
      }
   }

   public static boolean hasListeners(Class<? extends Event> eventClass) {
      if (cacheDirty) {
         rebuildCache();
      }

      List<ObsidianComponent_327> invocations = invocationCache.get(eventClass);
      return (invocations != null && !invocations.isEmpty());
   }
   public static synchronized void rebuildCache() {
      if (cacheDirty) {
         ConcurrentHashMap<Class<? extends Event>, List<ObsidianComponent_327>> rebuilt = new ConcurrentHashMap<>();

         for (Object listener : classRegistry.values()) {
            Method[] var3 = listener.getClass().getDeclaredMethods();
            int var4 = var3.length;

            for (int var5 = 0; var5 < var4; var5++) {
               Method method = var3[var5];
               if (method.isAnnotationPresent(EventTarget.class)) {
                  Class<?>[] parameters = method.getParameterTypes();
                  if (parameters.length == (1)
                     && Event.class.isAssignableFrom(parameters[0])) {
                     Class<? extends Event> eventClass = (Class<? extends Event>)parameters[0];
                     method.setAccessible(true);
                     rebuilt.computeIfAbsent(eventClass, key -> new ArrayList<>())
                        .add(new ObsidianComponent_327(listener, method, method.getAnnotation(EventTarget.class).priority()));
                  }
               }
            }
         }

         for (List<ObsidianComponent_327> invocations : rebuilt.values()) {
            invocations.sort((a, b) -> {
               int priorityCompare = Integer.compare(b.priority(), a.priority());
               if (priorityCompare != 0) {
                  return priorityCompare;
               } else {
                  int classCompare = a.listener().getClass().getName().compareTo(b.listener().getClass().getName());
                  return classCompare != 0 ? classCompare : a.method().getName().compareTo(b.method().getName());
               }
            });
         }

         invocationCache.clear();
         invocationCache.putAll(rebuilt);
         cacheDirty = false;
      }
   }
   public static void logSlowHandler(Event event, ObsidianComponent_327 invocation, long elapsedNanos) {
      String listenerName = invocation.listener().getClass().getSimpleName();
      String methodName = invocation.method().getName();
      String eventName = event.getClass().getSimpleName();
      String key = "handler:" + eventName + ":" + listenerName + "#" + methodName;
      if (canWarn(slowHandlerWarnings, key)) {
         PrintStream var10000 = System.out;
         Locale var10001 = Locale.ROOT;
         Object[] var10003 = new Object[4];
         var10003[0] = eventName;
         var10003[1] = listenerName;
         var10003[2] = methodName;
         var10003[3] = (double)elapsedNanos
            / 1000000.0;
         var10000.println(String.format(var10001, "[PerfDebug] Slow handler: %s -> %s#%s took %.2f ms", var10003));
      }
   }
   public static void logSlowEvent(Event event, long elapsedNanos, int invocationCount) {
      String eventName = event.getClass().getSimpleName();
      String key = "event:" + eventName;
      if (canWarn(slowEventWarnings, key)) {
         PrintStream var10000 = System.out;
         Locale var10001 = Locale.ROOT;
         Object[] var10003 = new Object[3];
         var10003[0] = eventName;
         var10003[1] = (double)elapsedNanos
            / 1000000.0;
         var10003[2] = invocationCount;
         var10000.println(String.format(var10001, "[PerfDebug] Slow event: %s took %.2f ms for %d handlers", var10003));
      }
   }
   public static boolean canWarn(ConcurrentHashMap<String, Long> warnings, String key) {
      long now = System.nanoTime();
      Long lastWarn = warnings.get(key);
      if (lastWarn != null && now - lastWarn < WARN_COOLDOWN_NANOS) {
         return false;
      } else {
         warnings.put(key, now);
         return true;
      }
   }
}
