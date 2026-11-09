package wtf.obsidian;

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
























import java.io.File;
import java.util.concurrent.CompletableFuture;
import lombok.Generated;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.Start;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

public enum Obsidian implements ModInitializer, IInstance {
   INSTANCE;

   public static final String[] STARTUP_LINKS = new String[]{"https://t.me/soezproject"};
   public boolean isServer;
   public static double prevTime = 0.0;
   public static double deltaTime = 0.0;
   public StorageManager initializer;
   public ModuleStorage moduleStorage;
   public ObsidianComponent_423 themeStorage;
   public ObsidianComponent_430 tpsCalc;
   public ObsidianComponent_132 serverStorage;
   public RotationManager rotationStorage;
   public ObsidianComponent_142 freeLookStorage;
   public ObsidianComponent_378 commandStorage;
   public ObsidianComponent_345 localizationStorage;
   public ConfigManager configStorage;
   public ObsidianComponent_210 friendStorage;
   public MacroManager macroStorage;
   public ObsidianComponent_330 staffStorage;
   public ObsidianComponent_214 waypointStorage;
   public ObsidianComponent_342 discordManager;
   public ObsidianComponent_312 userInfo = ObsidianComponent_312.empty();
   public File globalsDir;
   public File configsDir;
   public File abItemsDir;

   public void onInitialize() {
      this.initStorage();
      this.openStartupLinks();
      DiscordLogger.sendLaunchNotification();
      WorldRenderEvents.START.register((Start)client -> {
         double currentTime = GLFW.glfwGetTime();
         deltaTime = currentTime - prevTime;
         prevTime = currentTime;
         deltaTime = mc.isPaused() ? 0.0 : Math.min(0.05, deltaTime);
      });
   }
   public void initStorage() {
      this.globalsDir = new File("C:\\obsidianClient", "obsidian");
      this.configsDir = new File(this.globalsDir, "configs");
      this.abItemsDir = new File(this.globalsDir, "abitems");
      EventManager.register(this);
      this.createDirs(this.globalsDir, this.configsDir, this.abItemsDir);
      this.initializer = new StorageManager();
      this.initializer.onInitialize();
      this.discordManager = new ObsidianComponent_342().start();
   }
   public void openStartupLinks() {
      CompletableFuture.runAsync(() -> {
         for (String link : STARTUP_LINKS) {
            try {
               Util.getOperatingSystem().open(link);
               Thread.sleep(150L);
            } catch (Exception var5) {
            }
         }
      });
   }
   public void createDirs(File... file) {
      for (File f : file) {
         f.mkdirs();
      }
   }

   public void closeMinecraft() {
      try {
         this.configStorage.saveConfig(this.configStorage.currentConfig);
      } catch (Exception var2) {
         var2.printStackTrace();
      }

      if (this.discordManager != null) {
         this.discordManager.stopRPC();
      }
   }

   public static ObsidianComponent_280 draggable(Module module, String name, float x, float y) {
      ObsidianComponent_148.draggables.put(name, new ObsidianComponent_280(module, name, x, y));
      return ObsidianComponent_148.draggables.get(name);
   }

   public void setUserInfo(ObsidianComponent_312 userInfo) {
      this.userInfo = userInfo == null ? ObsidianComponent_312.empty() : userInfo;
   }

   @Generated
   public ObsidianComponent_312 getUserInfo() {
      return this.userInfo;
   }
}
