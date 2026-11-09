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

























import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class AimBotModule extends Module {
   public static AimBotModule INSTANCE = new AimBotModule();
   public final MultiBooleanSetting targetTypes;
   public final SliderSetting range;
   public final SliderSetting aimTime;
   public final BooleanSetting silentRotations;
   public final BooleanSetting showCrosshair;
   public final SliderSetting crosshairSize;
   public LivingEntity target;
   public boolean isAiming;
   public float aimProgress;
   public ObsidianComponent_olorSetting targetRotation;

   public AimBotModule() {
      super("AimBot", "Авто-наведение для лука и арбалета", Category.COMBAT);
      BooleanSetting[] var10004 = new BooleanSetting[5];
      var10004[0] = new BooleanSetting("Игроки", true);
      var10004[1] = new BooleanSetting("В броне", true);
      var10004[2] = new BooleanSetting("Без брони", false);
      var10004[3] = new BooleanSetting("Мобы", false);
      var10004[4] = new BooleanSetting("Зомби", false);
      this.targetTypes = new MultiBooleanSetting("Типы целей", var10004);
      this.range = new SliderSetting(
         "Дистанция",
         40.0f,
         10.0f,
         100.0f,
         1.0f
      );
      this.aimTime = new SliderSetting(
         "Время наводки (тики)",
         10.0f,
         0.0f,
         40.0f,
         1.0f
      );
      this.silentRotations = new BooleanSetting("Тихие повороты", true);
      this.showCrosshair = new BooleanSetting("Показать прицел", true);
      this.crosshairSize = new SliderSetting(
         "Размер прицела",
         1.0f,
         0.30000001192092896f,
         3.0f,
         0.10000000149011612f
      );
      this.target = null;
      this.isAiming = false;
      this.aimProgress = 0.0f;
      this.targetRotation = null;
      Setting[] var10001 = new Setting[6];
      var10001[0] = this.targetTypes;
      var10001[1] = this.range;
      var10001[2] = this.aimTime;
      var10001[3] = this.silentRotations;
      var10001[4] = this.showCrosshair;
      var10001[5] = this.crosshairSize;
      this.addSettings(var10001);
   }
   public Identifier getCrosshairTexture() {
      return Identifier.of("obsidian", "textures/cross/hit.png");
   }
   public boolean isHoldingBowOrCrossbow() {
      ItemStack mainHand = mc.player.getMainHandStack();
      ItemStack offHand = mc.player.getOffHandStack();
      return (!(!(mainHand.getItem() instanceof BowItem)
            && !(mainHand.getItem() instanceof CrossbowItem)
            && !(offHand.getItem() instanceof BowItem)
            && !(offHand.getItem() instanceof CrossbowItem)));
   }
   public boolean isUsingBowOrCrossbow() {
      return (mc.player.isUsingItem() && this.isHoldingBowOrCrossbow());
   }
   public boolean isValidTarget(LivingEntity entity) {
      if (entity == mc.player) {
         return false;
      } else if (!entity.isAlive() || entity.getHealth() <= 0.0f) {
         return false;
      } else if (!(entity instanceof PlayerEntity)) {
         if (entity instanceof ZombieEntity) {
            return this.targetTypes.is("Зомби");
         } else {
            return (entity instanceof HostileEntity ? this.targetTypes.is("Мобы") : false);
         }
      } else if (!this.targetTypes.is("Игроки")) {
         return false;
      } else if (Obsidian.INSTANCE.friendStorage.isFriend(entity.getName().getString())) {
         return false;
      } else {
         boolean hasArmor = false;
         PlayerEntity player = (PlayerEntity)entity;

         for (ItemStack armor : player.getArmorItems()) {
            if (!armor.isEmpty()) {
               hasArmor = true;
               break;
            }
         }

         if (this.targetTypes.is("В броне") && hasArmor) {
            return true;
         } else if (this.targetTypes.is("Без брони") && !hasArmor) {
            return true;
         } else {
            return (!this.targetTypes.is("В броне") && !this.targetTypes.is("Без брони"));
         }
      }
   }
   public LivingEntity findBestTarget() {
      List<LivingEntity> targets = new ArrayList<>();
      Box searchBox = mc.player.getBoundingBox().expand((double)this.range.getValue().floatValue());

      for (LivingEntity entity : mc.world.getEntitiesByClass(LivingEntity.class, searchBox, e -> true)) {
         if (this.isValidTarget(entity)) {
            double dist = (double)mc.player.distanceTo(entity);
            if (!(dist > (double)this.range.getValue().floatValue())) {
               targets.add(entity);
            }
         }
      }

      if (targets.isEmpty()) {
         return null;
      } else {
         targets.sort(Comparator.comparingDouble(entityx -> (double)mc.player.distanceTo(entityx)));
         return targets.get(0);
      }
   }
   public ObsidianComponent_olorSetting calculateBowRotation(LivingEntity target) {
      Vec3d eyes = mc.player.getEyePos();
      Vec3d targetPos = target.getBoundingBox().getCenter();
      double dx = targetPos.x - eyes.x;
      double dy = targetPos.y - eyes.y;
      double dz = targetPos.z - eyes.z;
      double distance = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, distance)));
      return new ObsidianComponent_olorSetting(yaw, pitch);
   }

   @EventTarget
   public void onRender3D(RenderEvent event) {
      if (this.showCrosshair.isState() && this.target != null && this.isAiming) {
         float partialTicks = event.getTickDelta();
         Vec3d targetPos = new Vec3d(
            MathHelper.lerp((double)partialTicks, this.target.lastRenderX, this.target.getX()),
            MathHelper.lerp((double)partialTicks, this.target.lastRenderY, this.target.getY())
               + (double)this.target.getHeight() / 2.0,
            MathHelper.lerp((double)partialTicks, this.target.lastRenderZ, this.target.getZ())
         );
         Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
         MatrixStack matrices = event.getMatrices();
         double renderX = targetPos.x - cameraPos.x;
         double renderY = targetPos.y - cameraPos.y;
         double renderZ = targetPos.z - cameraPos.z;
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 1);
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.disableCull();
         RenderSystem.setShaderTexture(0, this.getCrosshairTexture());
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
         matrices.push();
         matrices.translate(renderX, renderY, renderZ);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mc.gameRenderer.getCamera().getYaw()));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mc.gameRenderer.getCamera().getPitch()));
         float size = this.crosshairSize.get() * 0.5f;
         int alpha = (int)(255.0f * this.aimProgress);
         int color = ObsidianComponent_olorUtil.getThemeColor();
         int r = color >> 16 & 255;
         int g = color >> (8) & 255;
         int b = color & (255);
         Matrix4f matrix = matrices.peek().getPositionMatrix();
         BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         buffer.vertex(matrix, -size, -size, 0.0f)
            .texture(0.0f, 1.0f)
            .color(r, g, b, alpha);
         buffer.vertex(matrix, -size, size, 0.0f)
            .texture(0.0f, 0.0f)
            .color(r, g, b, alpha);
         buffer.vertex(matrix, size, size, 0.0f)
            .texture(1.0f, 0.0f)
            .color(r, g, b, alpha);
         buffer.vertex(matrix, size, -size, 0.0f)
            .texture(1.0f, 1.0f)
            .color(r, g, b, alpha);
         BufferRenderer.drawWithGlobalProgram(buffer.end());
         matrices.pop();
         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(true);
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
      }
   }

   @EventTarget
   public void onGameUpdate(Event_93 e) {
      if (mc.player != null && mc.world != null) {
         this.isAiming = this.isUsingBowOrCrossbow();
         if (this.isAiming) {
            LivingEntity newTarget = this.findBestTarget();
            if (newTarget != null) {
               if (this.target != newTarget) {
                  this.target = newTarget;
                  this.aimProgress = 0.0f;
               }

               ObsidianComponent_olorSetting newRotation = this.calculateBowRotation(this.target);
               float maxStep = 1.0f
                  / Math.max(1.0f, this.aimTime.getValue().floatValue());
               this.aimProgress = Math.min(this.aimProgress + maxStep, 1.0f);
               float currentYaw = mc.player.getYaw();
               float currentPitch = mc.player.getPitch();
               float targetYaw = newRotation.getYaw();
               float targetPitch = newRotation.getPitch();
               float yawDiff = MathHelper.wrapDegrees(targetYaw - currentYaw);
               float pitchDiff = targetPitch - currentPitch;
               float stepYaw = yawDiff * this.aimProgress;
               float stepPitch = pitchDiff * this.aimProgress;
               this.targetRotation = new ObsidianComponent_olorSetting(currentYaw + stepYaw, currentPitch + stepPitch);
            }
         } else {
            this.target = null;
            this.targetRotation = null;
            this.aimProgress = 0.0f;
         }
      }
   }

   @EventTarget
   public void onUpdate(Event_93 ignoredghj) {
      if (this.target != null && this.isAiming && this.targetRotation != null) {
         if (this.silentRotations.isState()) {
            float gcd = ObsidianComponent_390.getGCD();
            float yaw = this.targetRotation.getYaw();
            float pitch = this.targetRotation.getPitch();
            yaw -= (yaw - mc.player.getYaw()) % gcd;
            pitch -= (pitch - mc.player.getPitch()) % gcd;
            RotationManager.update(
               new ObsidianComponent_olorSetting(yaw, pitch),
               180.0f,
               180.0f,
               45.0f,
               45.0f,
               0,
               2,
               false
            );
         } else {
            mc.player.setYaw(this.targetRotation.getYaw());
            mc.player.setPitch(this.targetRotation.getPitch());
         }
      }
   }

   public LivingEntity getTarget() {
      return this.target;
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.target = null;
      this.isAiming = false;
      this.aimProgress = 0.0f;
      this.targetRotation = null;
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.target = null;
      this.isAiming = false;
      this.aimProgress = 0.0f;
      this.targetRotation = null;
   }
}
