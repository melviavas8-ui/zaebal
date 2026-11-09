package wtf.obsidian.modules.render;

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

























public class RemovalsModule extends Module {
   public static RemovalsModule INSTANCE = new RemovalsModule();
   public final MultiBooleanSetting elements;

   public RemovalsModule() {
      super("Removals", "Убирает выбранные элементы рендера", Category.RENDER);
      BooleanSetting[] var10004 = new BooleanSetting[9];
      var10004[0] = new BooleanSetting("Огонь", false);
      var10004[1] = new BooleanSetting("Плохие эффекты", false);
      var10004[2] = new BooleanSetting("Оверлей в блоке", false);
      var10004[3] = new BooleanSetting("Частицы", false);
      var10004[4] = new BooleanSetting("Погода", false);
      var10004[5] = new BooleanSetting("Облака", false);
      var10004[6] = new BooleanSetting("Блок-сущности", false);
      var10004[7] = new BooleanSetting("Тени", false);
      var10004[8] = new BooleanSetting("Анимацию тотема", false);
      this.elements = new MultiBooleanSetting("Элементы", var10004);
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.elements;
      this.addSettings(var10001);
   }

   public boolean isEnabled(String element) {
      return (this.isEnable() && this.elements.is(element));
   }

   public boolean isTotemAnimationDisabled() {
      return this.isEnabled("Анимацию тотема");
   }
}
