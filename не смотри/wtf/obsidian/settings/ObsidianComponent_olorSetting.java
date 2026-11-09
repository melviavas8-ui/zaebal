package wtf.obsidian.settings;

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
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ObsidianComponent_olorSetting implements IInstance {
   public float yaw;
   public float pitch;

   public ObsidianComponent_olorSetting(Entity entity) {
      this.yaw = entity.getYaw();
      this.pitch = entity.getPitch();
   }

   public float getDelta(ObsidianComponent_olorSetting target) {
      float yawDelta = MathHelper.wrapDegrees(target.getYaw() - this.yaw);
      float pitchDelta = target.getPitch() - this.pitch;
      return (float)Math.hypot((double)Math.abs(yawDelta), (double)Math.abs(pitchDelta));
   }

   public double getDeltaDouble(ObsidianComponent_olorSetting target) {
      double yawDelta = (double)MathHelper.wrapDegrees(target.getYaw() - this.yaw);
      double pitchDelta = (double)MathHelper.wrapDegrees(target.getPitch() - this.pitch);
      return Math.hypot(yawDelta, pitchDelta);
   }

   public static Vector2f camera() {
      return new Vector2f(cameraYaw(), cameraPitch());
   }

   public static float cameraYaw() {
      return MathHelper.wrapDegrees(
         mc.gameRenderer.getCamera().getYaw()
            + (float)(mc.gameRenderer.getCamera().isThirdPerson() ? 180 : 0)
      );
   }

   public static float cameraPitch() {
      return (float)(mc.gameRenderer.getCamera().isThirdPerson() ? -1 : 1)
         * mc.gameRenderer.getCamera().getPitch();
   }

   public static ObsidianComponent_olorSetting from(PlayerEntity player, Entity target) {
      Vec3d playerPos = player.getCameraPosVec(0.0f);
      Vec3d targetPos = target.getPos()
         .add(
            0.0,
            (double)target.getHeight() * 0.5,
            0.0
         );
      double dx = targetPos.x - playerPos.x;
      double dy = targetPos.y - playerPos.y;
      double dz = targetPos.z - playerPos.z;
      double distanceXZ = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, distanceXZ)));
      return new ObsidianComponent_olorSetting(yaw, pitch);
   }

   public final Vec3d toVector() {
      float f = this.pitch * 0.01745329238474369f;
      float g = -this.yaw * 0.01745329238474369f;
      float h = MathHelper.cos(g);
      float i = MathHelper.sin(g);
      float j = MathHelper.cos(f);
      float k = MathHelper.sin(f);
      return new Vec3d((double)(i * j), (double)(-k), (double)(h * j));
   }

   @Generated
   public float getYaw() {
      return this.yaw;
   }

   @Generated
   public float getPitch() {
      return this.pitch;
   }

   @Generated
   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   @Generated
   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof ObsidianComponent_olorSetting other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else if (Float.compare(this.getYaw(), other.getYaw()) != 0) {
         return false;
      } else {
         return (!(Float.compare(this.getPitch(), other.getPitch()) != 0));
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof ObsidianComponent_olorSetting;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + Float.floatToIntBits(this.getYaw());
      return result * 59 + Float.floatToIntBits(this.getPitch());
   }

   @Generated
   @Override
   public String toString() {
      return "Rotation(yaw=" + this.getYaw() + ", pitch=" + this.getPitch() + ")";
   }

   @Generated
   public ObsidianComponent_olorSetting() {
   }

   @Generated
   public ObsidianComponent_olorSetting(float yaw, float pitch) {
      this.yaw = yaw;
      this.pitch = pitch;
   }
}
