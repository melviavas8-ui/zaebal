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
























import lombok.Generated;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class ObsidianComponent_214 implements IInstance {
   private static final Identifier ARROW_TEXTURE = Identifier.of("obsidian", "textures/arrows/gps.png");
   private final AnimationUtil alphaAnimation = new AnimationUtil(
      0.0f, 8.5f, ObsidianComponent_57.CUBIC_OUT
   );
   public float animatedYaw;
   public ObsidianComponent_153 activeWaypoint = null;

   public ObsidianComponent_214() {
      EventManager.register(this);
   }

   public void set(ObsidianComponent_153 waypoint) {
      this.activeWaypoint = waypoint;
   }

   public void remove(ObsidianComponent_153 waypoint) {
      if (this.activeWaypoint != null && this.activeWaypoint.equals(waypoint)) {
         this.activeWaypoint = null;
      }
   }

   public void clear() {
      this.activeWaypoint = null;
   }

   public boolean isEmpty() {
      return (this.activeWaypoint == null);
   }

   @EventTarget
   public void onRender2D(EventRender2D event) {
      if (mc.player != null && mc.world != null) {
         this.alphaAnimation
            .update(
               this.activeWaypoint == null
                  ? 0.0f
                  : 1.0f
            );
         float alpha = MathHelper.clamp(
            this.alphaAnimation.getValue(),
            0.0f,
            1.0f
         );
         if (this.activeWaypoint != null && !(alpha <= 0.019999999552965164f)) {
            float centerX = (float)mc.getWindow().getScaledWidth() * 0.5f;
            float centerY = (float)mc.getWindow().getScaledHeight() * 0.25f;
            float size = 40.0f;
            double deltaX = this.activeWaypoint.getX() - mc.player.getX();
            double deltaZ = this.activeWaypoint.getZ() - mc.player.getZ();
            int distance = (int)MathLerpUtil.round(MathHelper.sqrt((float)(deltaX * deltaX + deltaZ * deltaZ)));
            float targetYaw = (float)(-Math.toDegrees(Math.atan2(deltaX, deltaZ))) - mc.gameRenderer.getCamera().getYaw();
            this.animatedYaw = this.interpolateAngle(this.animatedYaw, targetYaw, 0.18000000715255737f);
            int color = ObsidianComponent_olorUtil.applyAlpha(ObsidianComponent_olorUtil.getThemeColor(), alpha);
            FontRenderer font = ObsidianComponent_201.getFont("sf_regular", 12);
            if (font != null) {
               String distanceText = distance + "m.";
               font.draw(
                  event.getContext().getMatrices(),
                  distanceText,
                  centerX
                     - font.getWidth(distanceText) * 0.5f
                     + 1.5f,
                  centerY + 7.5f,
                  ObsidianComponent_olorUtil.applyAlpha(-1, alpha)
               );
            }

            event.getContext().getMatrices().push();
            event.getContext().getMatrices().translate(centerX, centerY, 0.0f);
            event.getContext().getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(this.animatedYaw));
            event.getContext().getMatrices().translate(-centerX, -centerY, 0.0F);
            float drawX = centerX - size * 0.5f;
            float drawY = centerY - size * 0.5f;
            RenderUtil.drawImage(event.getContext().getMatrices(), ARROW_TEXTURE, drawX, drawY, size, size, color);
            event.getContext().getMatrices().pop();
         }
      }
   }
   public float interpolateAngle(float current, float target, float factor) {
      float delta = MathHelper.wrapDegrees(target - current);
      return current + delta * factor;
   }

   @Generated
   public ObsidianComponent_153 getActiveWaypoint() {
      return this.activeWaypoint;
   }
}
