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

























import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class ArrowsModule extends Module {
   public static ArrowsModule INSTANCE = new ArrowsModule();
   private static final Identifier FIRST_ARROW_TEXTURE = Identifier.of("obsidian", "textures/arrows/arrow.png");
   private static final Identifier SECOND_ARROW_TEXTURE = Identifier.of("obsidian", "textures/arrows/arr.png");
   private static final Identifier MAMA_ARROW_TEXTURE = Identifier.of("obsidian", "textures/arrows/arrowsnurik.png");
   public final ModeSetting type;
   public final SliderSetting radius;
   public final SliderSetting size;
   public final SliderSetting glowRadius;
   public final Map<UUID, ObsidianComponent_223> states;
   public final Set<UUID> seenPlayers;

   public ArrowsModule() {
      super("Arrows", "Красивые стрелочки на энтити", Category.RENDER);
      String[] var10005 = new String[3];
      var10005[0] = "Первый";
      var10005[1] = "Второй";
      var10005[2] = "Третий";
      this.type = new ModeSetting("Вид", "Первый", var10005);
      this.radius = new SliderSetting(
         "Радиус",
         58.0f,
         30.0f,
         120.0f,
         1.0f
      );
      this.size = new SliderSetting(
         "Размер",
         13.0f,
         8.0f,
         28.0f,
         0.5f
      );
      this.glowRadius = new SliderSetting(
         "Свечение",
         7.5f,
         0.0f,
         20.0f,
         0.5f
      );
      this.states = new HashMap<>();
      this.seenPlayers = new HashSet<>();
      Setting[] var10001 = new Setting[4];
      var10001[0] = this.type;
      var10001[1] = this.radius;
      var10001[2] = this.size;
      var10001[3] = this.glowRadius;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onRender(EventRender2D event) {
      if (mc.player != null && mc.world != null && !mc.options.hudHidden) {
         if (mc.options.getPerspective() != Perspective.FIRST_PERSON) {
            this.fadeAllStates();
         } else {
            float partialTicks = event.getPartialTicks();
            float centerX = (float)mc.getWindow().getScaledWidth() * 0.5f;
            float centerY = (float)mc.getWindow().getScaledHeight() * 0.5f;
            float arrowSize = this.size.get();
            float y = centerY - this.radius.get();
            float playerYaw = this.getReferenceYaw(partialTicks);
            Vec3d selfPos = this.getReferencePos(partialTicks);
            this.seenPlayers.clear();

            for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
               if (player != mc.player && player.isAlive() && !player.isSpectator() && !this.isGhostPlayer(player)) {
                  UUID uuid = player.getUuid();
                  ObsidianComponent_223 state = this.states.computeIfAbsent(uuid, id -> new ObsidianComponent_223());
                  this.seenPlayers.add(uuid);
                  int color = this.getPlayerColor(player);
                  float targetYaw = this.getRelativeYaw(player, partialTicks, playerYaw, selfPos);
                  state.rotation = this.interpolateAngle(state.rotation, targetYaw, 0.18000000715255737f);
                  state.alpha = this.approach(
                     state.alpha, 1.0f, 0.11999999731779099f
                  );
                  float alpha = MathHelper.clamp(
                     state.alpha, 0.0f, 1.0f
                  );
                  if (!(alpha <= 0.009999999776482582f)) {
                     int drawColor = ObsidianComponent_olorUtil.applyAlpha(color, alpha);
                     int shadowColor = ObsidianComponent_olorUtil.applyAlpha(
                        ObsidianComponent_olorUtil.darken(color, 0.550000011920929f),
                        alpha * 0.6499999761581421f
                     );
                     this.renderArrow(event.getContext().getMatrices(), centerX, centerY, y, arrowSize, state.rotation, drawColor, shadowColor);
                  }
               }
            }

            this.states
               .entrySet()
               .removeIf(
                  entry -> {
                     if (this.seenPlayers.contains(entry.getKey())) {
                        return false;
                     } else {
                        ObsidianComponent_223 statex = entry.getValue();
                        statex.alpha = this.approach(
                           statex.alpha,
                           0.0f,
                           0.10000000149011612f
                        );
                        return (statex.alpha <= 0.019999999552965164f);
                     }
                  }
               );
         }
      } else {
         this.states.clear();
      }
   }
   public void renderArrow(MatrixStack matrices, float centerX, float centerY, float y, float size, float rotation, int color, int shadowColor) {
      Identifier ARROW;
      if (this.type.getIndex() == 0) {
         ARROW = FIRST_ARROW_TEXTURE;
      } else if (this.type.getIndex() == 1) {
         ARROW = SECOND_ARROW_TEXTURE;
      } else {
         ARROW = MAMA_ARROW_TEXTURE;
      }

      matrices.push();
      matrices.translate(centerX, centerY, 0.0f);
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotation));
      matrices.translate(-centerX, -centerY, 0.0f);
      float x = centerX - size * 0.5f;
      RenderUtil.drawImage(matrices, ARROW, x, y + size * 0.07999999821186066f, size, size, shadowColor);
      RenderUtil.drawImage(matrices, ARROW, x, y, size, size, color);
      matrices.pop();
   }
   public void fadeAllStates() {
      this.states
         .entrySet()
         .removeIf(
            entry -> {
               ObsidianComponent_223 state = entry.getValue();
               state.alpha = this.approach(
                  state.alpha, 0.0f, 0.10000000149011612f
               );
               return (state.alpha <= 0.019999999552965164f);
            }
         );
   }
   public float approach(float current, float target, float factor) {
      factor = MathHelper.clamp(factor, 0.0f, 1.0f);
      return MathHelper.lerp(factor, current, target);
   }
   public int getPlayerColor(AbstractClientPlayerEntity player) {
      String name = player.getName().getString();
      boolean isFriend = (Obsidian.INSTANCE.friendStorage != null && Obsidian.INSTANCE.friendStorage.isFriend(name));
      return isFriend
         ? ObsidianComponent_olorUtil.rgb(80, 170, 255)
         : ObsidianComponent_olorUtil.getThemeColor();
   }
   public float getRelativeYaw(Entity entity, float partialTicks, float playerYaw, Vec3d selfPos) {
      Vec3d entityPos = MathLerpUtil.interpolate(entity, partialTicks);
      double dx = entityPos.x - selfPos.x;
      double dz = entityPos.z - selfPos.z;
      float yaw = (float)(-Math.toDegrees(Math.atan2(dx, dz)));
      return MathHelper.wrapDegrees(yaw - playerYaw);
   }
   public float getReferenceYaw(float partialTicks) {
      return ObsidianComponent_142.isActive() ? ObsidianComponent_142.getFreeYaw() : MathHelper.lerp(partialTicks, mc.player.prevYaw, mc.player.getYaw());
   }
   public Vec3d getReferencePos(float partialTicks) {
      return ObsidianComponent_142.isActive() && mc.gameRenderer != null && mc.gameRenderer.getCamera() != null
         ? mc.gameRenderer.getCamera().getPos()
         : MathLerpUtil.interpolate(mc.player, partialTicks);
   }
   public float interpolateAngle(float current, float target, float factor) {
      float delta = MathHelper.wrapDegrees(target - current);
      return current + delta * factor;
   }
   public boolean isGhostPlayer(AbstractClientPlayerEntity player) {
      if (player.getCustomName() != null) {
         String name = player.getCustomName().getString();
         if (name != null && name.startsWith("Ghost_")) {
            return true;
         }
      }

      return ("OtherClientPlayerEntity".equals(player.getClass().getSimpleName())
            && player.getPitch() == -30.0f);
   }
}
