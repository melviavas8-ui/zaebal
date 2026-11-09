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

























import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;

public class ViewModelModule extends Module {
   public static ViewModelModule INSTANCE = new ViewModelModule();
   public final SliderSetting mainHandX = new SliderSetting(
      "Правая рука X",
      0.0f,
      -2.0f,
      2.0f,
      0.009999999776482582f
   );
   public final SliderSetting mainHandY = new SliderSetting(
      "Правая рука Y",
      0.0f,
      -2.0f,
      2.0f,
      0.009999999776482582f
   );
   public final SliderSetting mainHandZ = new SliderSetting(
      "Правая рука Z",
      0.0f,
      -2.0f,
      2.0f,
      0.009999999776482582f
   );
   public final SliderSetting offHandX = new SliderSetting(
      "Левая рука X",
      0.0f,
      -2.0f,
      2.0f,
      0.009999999776482582f
   );
   public final SliderSetting offHandY = new SliderSetting(
      "Левая рука Y",
      0.0f,
      -2.0f,
      2.0f,
      0.009999999776482582f
   );
   public final SliderSetting offHandZ = new SliderSetting(
      "Левая рука Z",
      0.0f,
      -2.0f,
      2.0f,
      0.009999999776482582f
   );
   public final BooleanSetting onlyAura = new BooleanSetting("Только с аурой", false);

   public ViewModelModule() {
      super("ViewModel", "Оффсеты рук от первого лица", Category.RENDER);
      Setting[] var10001 = new Setting[7];
      var10001[0] = this.mainHandX;
      var10001[1] = this.mainHandY;
      var10001[2] = this.mainHandZ;
      var10001[3] = this.offHandX;
      var10001[4] = this.offHandY;
      var10001[5] = this.offHandZ;
      var10001[6] = this.onlyAura;
      this.addSettings(var10001);
   }

   public void applyHandPosition(MatrixStack matrices, Arm arm) {
      if (arm == Arm.RIGHT) {
         matrices.translate(this.mainHandX.get(), this.mainHandY.get(), this.mainHandZ.get());
      } else {
         matrices.translate(this.offHandX.get(), this.offHandY.get(), this.offHandZ.get());
      }
   }
}
