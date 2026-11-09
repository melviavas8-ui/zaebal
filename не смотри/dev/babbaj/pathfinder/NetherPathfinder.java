package dev.babbaj.pathfinder;

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






















import org.tukaani.xz.XZInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class NetherPathfinder {
   public static int CACHE_MISS_GENERATE = 0;
   public static int CACHE_MISS_AIR = 1;
   public static int CACHE_MISS_SOLID = 2;
   private static final boolean IS_LOADED;

   public static native long newContext(long var0);

   public static native void freeContext(long var0);

   public static native void insertChunkData(long var0, int var2, int var3, boolean[] var4);

   public static native long getOrCreateChunk(long var0, int var2, int var3);

   public static native long getChunkPointer(long var0, int var2, int var3);

   public static native boolean hasChunkFromJava(long var0, int var2, int var3);

   public static native void cullFarChunks(long var0, int var2, int var3, int var4);

   public static native PathSegment pathFind(
      long var0, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8, boolean var9, int var10, boolean var11
   );

   private static native void raytrace0(long var0, int var2, int var3, double[] var4, double[] var5, boolean[] var6, double[] var7);

   public static void raytrace(long var0, int var2, int var3, double[] var4, double[] var5, boolean[] var6, double[] var7) {
      if (var4.length >= var3 * 3 && var5.length >= var3 * 3 && var6.length >= var3 && (var7 == null || var7.length >= var3 * 3)) {
         raytrace0(var0, var2, var3, var4, var5, var6, var7);
      } else {
         throw new IllegalArgumentException("Bad array lengths idiot");
      }
   }

   private static native int isVisibleMulti0(long var0, int var2, int var3, double[] var4, double[] var5, boolean var6);

   public static int isVisibleMulti(long var0, int var2, int var3, double[] var4, double[] var5, boolean var6) {
      if (var4.length >= var3 * 3 && var5.length >= var3 * 3) {
         return isVisibleMulti0(var0, var2, var3, var4, var5, var6);
      } else {
         throw new IllegalArgumentException("Bad array lengths idiot");
      }
   }

   public static native boolean isVisible(long var0, int var2, double var3, double var5, double var7, double var9, double var11, double var13);

   public static native boolean cancel(long var0);

   static native long getX2Index();

   public static boolean isThisSystemSupported() {
      return IS_LOADED;
   }

   private static String getNativeLibName() {
      if (Integer.parseInt(System.getProperty("sun.arch.data.model")) != 64) {
         throw new UnsupportedOperationException("Unsupported architecture (64-bit required)");
      } else {
         String var0 = System.getProperty("os.name").toLowerCase();
         String var1;
         if (!(var1 = System.getProperty("os.arch").toLowerCase()).contains("arm") && !var1.contains("aarch64")) {
            if (!var1.equals("x86_64") && !var1.equals("amd64")) {
               throw new UnsupportedOperationException("Unsupported architecture: ".concat(String.valueOf(var1)));
            }

            var1 = "x86_64";
         } else {
            var1 = "aarch64";
         }

         if (var0.contains("linux")) {
            return "libnether_pathfinder-" + var1 + ".so";
         } else if (var0.contains("windows")) {
            return "nether_pathfinder-" + var1 + ".dll";
         } else if (var0.contains("mac")) {
            return "libnether_pathfinder-" + var1 + ".dylib";
         } else {
            throw new UnsupportedOperationException("Unsupported operating system: ".concat(String.valueOf(var0)));
         }
      }
   }

   private static byte[] getNativeLib(String libName) {
      try (InputStream is = NetherPathfinder.class.getClassLoader().getResourceAsStream("natives.zip.xz")) {
         if (is == null) {
            throw new NullPointerException("Resource natives.zip.xz not found");
         }
         try (XZInputStream xz = new XZInputStream(is);
              ZipInputStream zip = new ZipInputStream(xz)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
               if (entry.getName().equals(libName)) {
                  ByteArrayOutputStream out = new ByteArrayOutputStream();
                  byte[] buffer = new byte[4096];
                  int read;
                  while ((read = zip.read(buffer)) != -1) {
                     out.write(buffer, 0, read);
                  }
                  return out.toByteArray();
               }
            }
         }
      } catch (IOException e) {
         throw new RuntimeException("Failed to read natives.zip.xz", e);
      }
      throw new NullPointerException("Failed to find pathfinder library: " + libName);
   }

   private static void tryLoadLibrary() {
      try {
         String name = getNativeLibName();
         byte[] bytes = getNativeLib(name);
         String[] parts = name.split("\\.");
         Path temp = Files.createTempFile(parts[0], "." + parts[1]);
         System.out.println("[nether-pathfinder] Created temp file at " + temp.toAbsolutePath());
         try {
            Files.write(temp, bytes);
            System.load(temp.toAbsolutePath().toString());
         } finally {
            try {
               Files.delete(temp);
            } catch (IOException ignored) {
               System.err.println("[nether-pathfinder] Failed to delete temp file");
            }
            if (!temp.toFile().delete()) {
               temp.toFile().deleteOnExit();
            }
         }
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   static {
      boolean var0 = false;

      try {
         tryLoadLibrary();
         System.out.println("[nether-pathfinder] Loaded shared library");
         var0 = true;
      } catch (Throwable var2) {
         System.err.println("[nether-pathfinder] Failed to load shared library");
         var2.printStackTrace();
      }

      IS_LOADED = var0;
   }
}
