package wtf.obsidian.utils;

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
























import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ProtocolUtil {
   public static final int MC_1_19_PROTOCOL = 759;
   public static final long CACHE_TIME_MS = 1500L;
   private static final Pattern VERSION_PATTERN = Pattern.compile("1\\.(\\d+)");
   public static long nextRefreshAt;
   public static boolean belowOneNineteen;

   public ProtocolUtil() {
   }

   public static boolean isTargetProtocolBelowOneNineteen() {
      long now = System.currentTimeMillis();
      if (now < nextRefreshAt) {
         return belowOneNineteen;
      } else {
         belowOneNineteen = resolveBelowOneNineteen();
         nextRefreshAt = now + (1500L);
         return belowOneNineteen;
      }
   }
   public static boolean resolveBelowOneNineteen() {
      try {
         Class<?> viaFabricPlusClass = Class.forName("com.viaversion.viafabricplus.ViaFabricPlus");
         Object impl = viaFabricPlusClass.getMethod("getImpl").invoke(null);
         if (impl == null) {
            return false;
         } else {
            Object targetVersion = invokeNoArg(impl, "getTargetVersion");
            if (targetVersion == null) {
               return false;
            } else {
               Integer protocolId = readProtocolId(targetVersion);
               return (protocolId != null && protocolId < (759));
            }
         }
      } catch (Throwable var4) {
         return false;
      }
   }
   public static Object invokeNoArg(Object instance, String methodName) {
      try {
         Method method = instance.getClass().getMethod(methodName);
         return Modifier.isPublic(method.getModifiers()) && method.getParameterCount() == 0 ? method.invoke(instance) : null;
      } catch (Throwable var3) {
         return null;
      }
   }
   public static Integer readProtocolId(Object targetVersion) {
      try {
         Method getVersion = targetVersion.getClass().getMethod("getVersion");
         if (getVersion.invoke(targetVersion) instanceof Number number) {
            return number.intValue();
         }
      } catch (Throwable var9) {
      }

      try {
         Method[] var11 = targetVersion.getClass().getMethods();
         int var13 = var11.length;

         for (int number = 0; number < var13; number++) {
            Method method = var11[number];
            if (Modifier.isPublic(method.getModifiers()) && method.getParameterCount() == 0) {
               Class<?> returnType = method.getReturnType();
               if (returnType == int.class || returnType == Integer.class) {
                  String name = method.getName().toLowerCase();
                  if ((name.contains("version") || name.contains("protocol") || name.contains("id")) && method.invoke(targetVersion) instanceof Number numberx) {
                     return numberx.intValue();
                  }
               }
            }
         }
      } catch (Throwable var10) {
      }

      Matcher matcher = VERSION_PATTERN.matcher(String.valueOf(targetVersion));
      if (matcher.find()) {
         int minor = Integer.parseInt(matcher.group(1));
         return minor >= 19 ? 759 : 758;
      } else {
         return null;
      }
   }
}
