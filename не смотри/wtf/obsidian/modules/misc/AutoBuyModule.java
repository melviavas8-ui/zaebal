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

























public class AutoBuyModule extends Module {
   public static AutoBuyModule INSTANCE = new AutoBuyModule();
   public ObsidianComponent_261 openKey = new ObsidianComponent_261("Бинд гуи", -1);

   public AutoBuyModule() {
      super("AutoBuy", Category.MISC);
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.openKey;
      this.addSettings(var10001);
   }
}
