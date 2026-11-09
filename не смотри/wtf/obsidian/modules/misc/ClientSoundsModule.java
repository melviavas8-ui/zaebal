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

























public class ClientSoundsModule extends Module {
   public static ClientSoundsModule INSTANCE = new ClientSoundsModule();
   public final ModeSetting stateSounds;
   public final SliderSetting volume;

   public ClientSoundsModule() {
      super("ClientSounds", "Добавляет звуки клиента", Category.MISC);
      String[] var10005 = new String[6];
      var10005[0] = "Первый";
      var10005[1] = "Второй";
      var10005[2] = "Третий";
      var10005[3] = "Четвертый";
      var10005[4] = "Пятый";
      var10005[5] = "Шестой";
      this.stateSounds = new ModeSetting("Режим", "Нет", var10005);
      this.volume = new SliderSetting(
         "Громкость",
         50.0f,
         1.0f,
         100.0f,
         0.5f
      );
      Setting[] var10001 = new Setting[2];
      var10001[0] = this.stateSounds;
      var10001[1] = this.volume;
      this.addSettings(var10001);
   }
}
