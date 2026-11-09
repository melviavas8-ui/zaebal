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

























import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;

public class SeeInvisiblesModule extends Module {
   public static final float INVISIBLE_ALPHA = 0.7F;
   public static final int INVISIBLE_COLOR = Math.round(178.5f) << (24)
      | 16777215;
   public static SeeInvisiblesModule INSTANCE = new SeeInvisiblesModule();

   public SeeInvisiblesModule() {
      super("SeeInvisibles", "Показывает невидимых игроков", Category.RENDER);
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.player != null && mc.world != null) {
         for (PlayerEntity player : mc.world.getPlayers()) {
            if (this.shouldRenderInvisible(player)) {
               player.setInvisible(false);
            }
         }
      }
   }

   public boolean shouldRenderInvisible(PlayerEntity player) {
      return (!(!this.isEnable()
            || mc.player == null
            || player == null
            || player == mc.player
            || !player.isInvisible() && !player.hasStatusEffect(StatusEffects.INVISIBILITY)));
   }
}
