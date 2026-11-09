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























public class StorageManager implements IInstance {
   public void onInitialize() {
      EventManager.register(this);
      this.initStorages();
   }

   public void initStorages() {
      Obsidian.INSTANCE.moduleStorage = new ModuleStorage();
      Obsidian.INSTANCE.themeStorage = new ObsidianComponent_423();
      Obsidian.INSTANCE.tpsCalc = new ObsidianComponent_430();
      EventManager.register(Obsidian.INSTANCE.tpsCalc);
      Obsidian.INSTANCE.localizationStorage = new ObsidianComponent_345();
      Obsidian.INSTANCE.freeLookStorage = new ObsidianComponent_142();
      Obsidian.INSTANCE.rotationStorage = new RotationManager();
      Obsidian.INSTANCE.friendStorage = new ObsidianComponent_210();
      Obsidian.INSTANCE.macroStorage = new MacroManager();
      Obsidian.INSTANCE.staffStorage = new ObsidianComponent_330();
      Obsidian.INSTANCE.waypointStorage = new ObsidianComponent_214();
      Obsidian.INSTANCE.commandStorage = new ObsidianComponent_378();
      Obsidian.INSTANCE.configStorage = new ConfigManager();
   }
}
