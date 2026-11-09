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
























import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ObsidianComponent_135 {
   private final DiscordRichPresence richPresence = new DiscordRichPresence();

   public ObsidianComponent_135 setSmallImage(String var1) {
      return this.setSmallImage(var1, "");
   }

   public ObsidianComponent_135 setDetails(String var1) {
      if (var1 != null && !var1.isEmpty()) {
         this.richPresence.details = var1.substring(0, Math.min(var1.length(), 128));
      }

      return this;
   }

   public ObsidianComponent_135 setLargeImage(String var1, String var2) {
      this.richPresence.largeImageKey = var1;
      this.richPresence.largeImageText = var2;
      return this;
   }

   public ObsidianComponent_135 setState(String var1) {
      if (var1 != null && !var1.isEmpty()) {
         this.richPresence.state = var1.substring(0, Math.min(var1.length(), 128));
      }

      return this;
   }

   public ObsidianComponent_135 setInstance(boolean var1) {
      if ((this.richPresence.button_label_1 == null || !this.richPresence.button_label_1.isEmpty())
         && (this.richPresence.button_label_2 == null || !this.richPresence.button_label_2.isEmpty())) {
         this.richPresence.instance = var1 ? 1 : 0;
      }

      return this;
   }

   public ObsidianComponent_135 setButtons(ObsidianComponent_394 var1) {
      return this.setButtons(Collections.singletonList(var1));
   }

   public ObsidianComponent_135 setSmallImage(String var1, String var2) {
      this.richPresence.smallImageKey = var1;
      this.richPresence.smallImageText = var2;
      return this;
   }

   public ObsidianComponent_135 setParty(String var1, int var2, int var3) {
      if ((this.richPresence.button_label_1 == null || !this.richPresence.button_label_1.isEmpty())
         && (this.richPresence.button_label_2 == null || !this.richPresence.button_label_2.isEmpty())) {
         this.richPresence.partyId = var1;
         this.richPresence.partySize = var2;
         this.richPresence.partyMax = var3;
      }

      return this;
   }

   public ObsidianComponent_135 setButtons(List<ObsidianComponent_394> var1) {
      if (var1 != null && !var1.isEmpty()) {
         int var2 = Math.min(var1.size(), 2);
         this.richPresence.button_label_1 = var1.get(0).getLabel();
         this.richPresence.button_url_1 = var1.get(0).getUrl();
         if (var2 == 2) {
            this.richPresence.button_label_2 = var1.get(1).getLabel();
            this.richPresence.button_url_2 = var1.get(1).getUrl();
         }
      }

      return this;
   }

   public ObsidianComponent_135 setStartTimestamp(OffsetDateTime var1) {
      this.richPresence.startTimestamp = var1.toEpochSecond();
      return this;
   }

   public ObsidianComponent_135 setSecrets(String var1, String var2, String var3) {
      if ((this.richPresence.button_label_1 == null || !this.richPresence.button_label_1.isEmpty())
         && (this.richPresence.button_label_2 == null || !this.richPresence.button_label_2.isEmpty())) {
         this.richPresence.matchSecret = var1;
         this.richPresence.joinSecret = var2;
         this.richPresence.spectateSecret = var3;
      }

      return this;
   }

   public void setButtons(ObsidianComponent_394 var1, ObsidianComponent_394 var2) {
      ObsidianComponent_394[] var10001 = new ObsidianComponent_394[2];
      var10001[0] = var1;
      var10001[1] = var2;
      this.setButtons(Arrays.asList(var10001));
   }

   public void setStartTimestamp(long var1) {
      this.richPresence.startTimestamp = var1;
   }

   public ObsidianComponent_135 setSecrets(String var1, String var2) {
      if ((this.richPresence.button_label_1 == null || !this.richPresence.button_label_1.isEmpty())
         && (this.richPresence.button_label_2 == null || !this.richPresence.button_label_2.isEmpty())) {
         this.richPresence.joinSecret = var1;
         this.richPresence.spectateSecret = var2;
      }

      return this;
   }

   public ObsidianComponent_135 setEndTimestamp(long var1) {
      this.richPresence.endTimestamp = var1;
      return this;
   }

   public ObsidianComponent_135 setEndTimestamp(OffsetDateTime var1) {
      this.richPresence.endTimestamp = var1.toEpochSecond();
      return this;
   }

   public ObsidianComponent_135 setLargeImage(String var1) {
      return this.setLargeImage(var1, "");
   }

   public DiscordRichPresence build() {
      return this.richPresence;
   }
}
