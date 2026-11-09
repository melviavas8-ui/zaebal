package wtf.obsidian.modules.misc;

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

public class NoPushModule extends Module {
   public static NoPushModule INSTANCE = new NoPushModule();
   public MultiBooleanSetting collisionList;

   public NoPushModule() {
      super("NoPush", "Отключает коллизию", Category.MISC);
      BooleanSetting[] var10004 = new BooleanSetting[4];
      var10004[0] = new BooleanSetting("Блоки", true);
      var10004[1] = new BooleanSetting("Вода", false);
      var10004[2] = new BooleanSetting("Удочик", true);
      var10004[3] = new BooleanSetting("Игроки", true);
      this.collisionList = new MultiBooleanSetting("Коллизия", var10004);
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.collisionList;
      this.addSettings(var10001);
   }

   @Generated
   public MultiBooleanSetting getCollisionList() {
      return this.collisionList;
   }

   @Generated
   public void setCollisionList(MultiBooleanSetting collisionList) {
      this.collisionList = collisionList;
   }
}
