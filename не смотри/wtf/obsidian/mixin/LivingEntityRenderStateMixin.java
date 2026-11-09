package wtf.obsidian.mixin;

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























import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;


@Mixin({LivingEntityRenderState.class})
public class LivingEntityRenderStateMixin implements ObsidianComponent_144 {
   @Unique
   private boolean obsidian$seeInvisiblesTarget;

   @Override
   public boolean obsidian$isSeeInvisiblesTarget() {
      return this.obsidian$seeInvisiblesTarget;
   }

   @Override
   public void obsidian$setSeeInvisiblesTarget(boolean value) {
      this.obsidian$seeInvisiblesTarget = value;
   }
}
