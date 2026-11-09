package wtf.obsidian.modules.combat;

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

























import net.minecraft.util.math.MathHelper;

public class TpsSyncModule extends Module {
   public static TpsSyncModule INSTANCE = new TpsSyncModule();

   public TpsSyncModule() {
      super("TpsSync", "Синхронизация с TPS сервера", Category.COMBAT);
   }

   public float getCurrentTPS() {
      if (Obsidian.INSTANCE != null && Obsidian.INSTANCE.tpsCalc != null) {
         float tps = Obsidian.INSTANCE.tpsCalc.getTPS();
         return MathHelper.clamp(tps, 0.10000000149011612f, 20.0f);
      } else {
         return 20.0f;
      }
   }

   public long getAdjustedCooldown(long baseCooldown) {
      if (!this.isEnable()) {
         return baseCooldown;
      } else {
         float tps = this.getCurrentTPS();
         if (tps >= 20.0f) {
            return baseCooldown;
         } else {
            float multiplier = 20.0f / tps;
            float additionalFactor = 1.0f
               + (20.0f - tps) * 0.05000000074505806f;
            long adjusted = (long)((float)baseCooldown * multiplier * additionalFactor);
            return Math.min(adjusted, 3000L);
         }
      }
   }

   public boolean canAttack(long lastAttackTime, long baseCooldown, long currentTime) {
      if (!this.isEnable()) {
         return (currentTime >= lastAttackTime + baseCooldown);
      } else {
         long adjustedCooldown = this.getAdjustedCooldown(baseCooldown);
         return (currentTime >= lastAttackTime + adjustedCooldown);
      }
   }
}
