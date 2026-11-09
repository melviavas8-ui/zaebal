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






















import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.Generated;

public abstract class Module implements IInstance {
   public String name;
   public String description;
   public int key;
   public Category category;
   public boolean isOpen;
   public boolean enable;
   private final List<Setting> settings = new ArrayList<>();
   public final AnimationUtil animka;
   public final AnimationUtil arrayAnimka;

   public Module(String name, String description, Category category) {
      this.animka = new AnimationUtil(
         60.0f, 11.0f, ObsidianComponent_57.LINEAR
      );
      this.arrayAnimka = new AnimationUtil(
         0.0f, 11.0f, ObsidianComponent_57.LINEAR
      );
      this.name = name;
      this.description = description;
      this.category = category;
      this.key = -1;
   }

   public Module(String name, Category category) {
      this.animka = new AnimationUtil(
         60.0f, 11.0f, ObsidianComponent_57.LINEAR
      );
      this.arrayAnimka = new AnimationUtil(
         0.0f, 11.0f, ObsidianComponent_57.LINEAR
      );
      this.name = name;
      this.description = "NULLABLE";
      this.category = category;
      this.key = -1;
   }

   public void onEnable() {
      this.enable = true;
      EventManager.register(this);
      this.animka.update(1.0f);
      ObsidianComponent_283.push(this.name, this.category.getIcons(), true);
   }

   public void onDisable() {
      this.enable = false;
      EventManager.unregister(this);
      this.animka.update(0.0f);
      ObsidianComponent_283.push(this.name, this.category.getIcons(), false);
   }

   public void toggle() {
      this.enable = (!this.enable);
      if (this.enable) {
         this.onEnable();
      } else {
         this.onDisable();
      }
   }

   public void setEnabled(boolean state) {
      boolean lastState = this.enable;
      this.enable = state;

      try {
         if (state) {
            this.onEnable();
         } else if (lastState) {
            this.onDisable();
         }
      } catch (Exception var4) {
         this.enable = false;
         this.onDisable();
      }
   }

   public void addSettings(Setting... settings) {
      if (settings != null && settings.length != 0) {
         Arrays.stream(settings).filter(Objects::nonNull).forEach(this.settings::add);
      }
   }

   public String getDisplayName() {
      return Obsidian.INSTANCE.localizationStorage == null ? this.name : Obsidian.INSTANCE.localizationStorage.translate(this.name);
   }

   public String getDisplayDescription() {
      return Obsidian.INSTANCE.localizationStorage == null ? this.description : Obsidian.INSTANCE.localizationStorage.translate(this.description);
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public String getDescription() {
      return this.description;
   }

   @Generated
   public int getKey() {
      return this.key;
   }

   @Generated
   public Category getCategory() {
      return this.category;
   }

   @Generated
   public boolean isOpen() {
      return this.isOpen;
   }

   @Generated
   public boolean isEnable() {
      return this.enable;
   }

   @Generated
   public List<Setting> getSettings() {
      return this.settings;
   }

   @Generated
   public AnimationUtil getAnimka() {
      return this.animka;
   }

   @Generated
   public AnimationUtil getArrayAnimka() {
      return this.arrayAnimka;
   }

   @Generated
   public void setName(String name) {
      this.name = name;
   }

   @Generated
   public void setDescription(String description) {
      this.description = description;
   }

   @Generated
   public void setKey(int key) {
      this.key = key;
   }

   @Generated
   public void setCategory(Category category) {
      this.category = category;
   }

   @Generated
   public void setOpen(boolean isOpen) {
      this.isOpen = isOpen;
   }

   @Generated
   public void setEnable(boolean enable) {
      this.enable = enable;
   }
}
