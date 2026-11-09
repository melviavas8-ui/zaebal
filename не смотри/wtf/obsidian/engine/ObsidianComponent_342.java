package wtf.obsidian.engine;

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
























import lombok.Generated;

public class ObsidianComponent_342 implements IInstance {
   public DiscordIPC discordDaemonThread;
   public long APPLICATION_ID;
   public boolean running;
   public String image;
   public String site;
   public String telegram;
   public String state = "";
   public static DiscordRichPresence discordRichPresence = new DiscordRichPresence();
   public static ObsidianComponent_282 discordRPC = ObsidianComponent_282.INSTANCE;
   public void cppInit() {
      this.discordDaemonThread = new DiscordIPC(this);
      this.APPLICATION_ID = 1500216831762366494L;
      this.running = true;
      this.site = "https://obsidianclient.fun/";
      this.telegram = "https://t.me/soezproject";
   }

   public void init() {
      this.cppInit();
      ObsidianComponent_401 handlers = new ObsidianComponent_198().build();
      ObsidianComponent_282.INSTANCE.Discord_Initialize(String.valueOf(this.APPLICATION_ID), handlers, true, "");
      discordRichPresence.startTimestamp = System.currentTimeMillis() / (1000L);
      discordRPC.Discord_UpdatePresence(discordRichPresence);
      new Thread(() -> {
         while (this.running) {
            try {
               discordRichPresence.details = "Name » " + KeybindsHudElement2.getUsername();
               discordRichPresence.largeImageKey = this.image;
               discordRichPresence.button_label_1 = "Купить";
               discordRichPresence.button_url_1 = this.site;
               discordRichPresence.button_label_2 = "Телеграмм";
               discordRichPresence.button_url_2 = this.telegram;
               ObsidianComponent_282.INSTANCE.Discord_UpdatePresence(discordRichPresence);
               Thread.sleep(2000L);
            } catch (InterruptedException var2) {
               Thread.currentThread().interrupt();
               return;
            }
         }
      }, "Discord-RPC-Updater").start();
      this.discordDaemonThread.start();
   }

   public ObsidianComponent_342 start() {
      this.init();
      return this;
   }

   public void stopRPC() {
      this.running = false;
      ObsidianComponent_282.INSTANCE.Discord_Shutdown();
      if (this.discordDaemonThread != null) {
         this.discordDaemonThread.interrupt();
      }
   }

   @Generated
   public DiscordIPC getDiscordDaemonThread() {
      return this.discordDaemonThread;
   }

   @Generated
   public long getAPPLICATION_ID() {
      return this.APPLICATION_ID;
   }

   @Generated
   public boolean isRunning() {
      return this.running;
   }

   @Generated
   public String getImage() {
      return this.image;
   }

   @Generated
   public String getSite() {
      return this.site;
   }

   @Generated
   public String getTelegram() {
      return this.telegram;
   }

   @Generated
   public String getState() {
      return this.state;
   }
}
