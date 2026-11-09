package wtf.obsidian.engine;

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
























import net.minecraft.util.math.Vec3d;
public class ObsidianComponent_229 {
   public final Vec3d position;
   public final float bodyYaw;
   public final float netHeadYaw;
   public final float headPitch;
   public final float limbSwing;
   public final float limbSwingAmount;
   public final boolean sneaking;
   public final float height;
   public final long startTime;
public ObsidianComponent_229(
      Vec3d position, float bodyYaw, float netHeadYaw, float headPitch, float limbSwing, float limbSwingAmount, boolean sneaking, float height, long startTime
   ) {
      this.position = position;
      this.bodyYaw = bodyYaw;
      this.netHeadYaw = netHeadYaw;
      this.headPitch = headPitch;
      this.limbSwing = limbSwing;
      this.limbSwingAmount = limbSwingAmount;
      this.sneaking = sneaking;
      this.height = height;
      this.startTime = startTime;
   }
}
