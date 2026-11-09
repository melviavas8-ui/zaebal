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

























import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class DeathCoordModule extends Module {
   public static DeathCoordModule INSTANCE = new DeathCoordModule();
   private final BooleanSetting copyToClipboard = new BooleanSetting("Копировать в буфер", true);
   public BlockPos deathPos = null;
   public boolean isDead = false;

   public DeathCoordModule() {
      super("DeathCoord", "Показывает координаты смерти", Category.MISC);
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.copyToClipboard;
      this.addSettings(var10001);
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.isDead = false;
      this.deathPos = null;
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.player != null && mc.world != null) {
         if (mc.player.getHealth() <= 0.0f && !this.isDead) {
            this.isDead = true;
            this.deathPos = mc.player.getBlockPos();
            String coords = "X: " + this.deathPos.getX() + " Y: " + this.deathPos.getY() + " Z: " + this.deathPos.getZ();
            String dimension = this.getDimension();
            String message = "§cВы умерли! §f" + coords + " §7(" + dimension + ")";
            mc.player.sendMessage(Text.literal(message), false);
            if (this.copyToClipboard.isState()) {
               mc.keyboard.setClipboard(this.deathPos.getX() + " " + this.deathPos.getY() + " " + this.deathPos.getZ());
            }
         }

         if (mc.player.getHealth() > 0.0f && this.isDead) {
            this.isDead = false;
         }
      }
   }
   public String getDimension() {
      if (mc.world == null) {
         return "Unknown";
      } else {
         String dimension = mc.world.getRegistryKey().getValue().toString();
         if (dimension.contains("overworld")) {
            return "Overworld";
         } else if (dimension.contains("nether")) {
            return "Nether";
         } else {
            return dimension.contains("end") ? "End" : dimension;
         }
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.isDead = false;
      this.deathPos = null;
   }
}
