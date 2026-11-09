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

























import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class TargetESPModule extends Module {
   
   public static final float GHOST_ALPHA_MULT = 0.6F;
   public static final float CELKA_SPEED_MULT = 1.2F;
   public static final float SCALE_FACTOR = 0.007F;
   public static final long CUBE_ATTACH_LIFE_MS = 560L;
   public static final long CUBE_FADE_LIFE_MS = 320L;
   public static final int MAX_CUBE_PARTICLES = 72;
   public static final byte[][] CUBE_EDGES;
   public final ModeSetting mode;
   public final SliderSetting size;
   public final SliderSetting ringRadius;
   public final SliderSetting ringSpeed;
   public final SliderSetting rotateSpeed;
   public final BooleanSetting hurtColor;
   public final SliderSetting bmwGhostCount;
   public final SliderSetting bmwGhostLife;
   public final SliderSetting bmwStrengthXZ;
   public final SliderSetting bmwStrengthY;
   public float appearValue;
   public float scaleValue;
   public float rotProgress;
   public float rotFrom;
   public float rotTo;
   public long lastRotateUpdate;
   public LivingEntity lastTarget;
   public LivingEntity lastHandledTarget;
   public Vec3d lastTargetPos;
   public float lastTargetHeight;
   public float lastTargetWidth;
   public final CopyOnWriteArrayList<ObsidianComponent_322> bmwPoints;
   public float crystalRotationAngle;
   public float crystalAnimation;
   public float spawnAccumulator;
   public long lastCubeTime;
   public final ArrayList<ObsidianComponent_238> cubeParticles;
   public final ArrayList<ObsidianComponent_238> renderCubeParticles;
   public static final float SPAWN_INTERVAL = 0.022F;
   public static final int PARTICLES_PER_SPAWN = 1;

   public TargetESPModule() {
      super("TargetESP", "Отображения таргета", Category.RENDER);
      String[] var10005 = new String[6];
      var10005[0] = "Картинка 1";
      var10005[1] = "Картинка 2";
      var10005[2] = "Кольцо";
      var10005[3] = "Души";
      var10005[4] = "Кубы";
      var10005[5] = "Кристаллы";
      this.mode = new ModeSetting("Режим", "Картинка 1", var10005);
      this.size = new SliderSetting(
         "Размер",
         1.149999976158142f,
         0.6000000238418579f,
         2.5f,
         0.05000000074505806f
      );
      this.ringRadius = new SliderSetting(
         "Радиус кольца",
         0.5f,
         0.30000001192092896f,
         1.5f,
         0.05F
      );
      this.ringSpeed = new SliderSetting(
         "Скорость кольца",
         1.0f,
         0.30000001192092896f,
         3.0f,
         0.10000000149011612f
      );
      this.rotateSpeed = new SliderSetting(
         "Скорость вращения",
         1.2000000476837158f,
         0.20000000298023224f,
         4.0f,
         0.05000000074505806f
      );
      this.hurtColor = new BooleanSetting("Окрашивание при ударе", true);
      this.bmwGhostCount = new SliderSetting(
         "Кол-во призраков",
         3.0f,
         2.0f,
         5.0f,
         1.0f
      );
      this.bmwGhostLife = new SliderSetting(
         "Время жизни (мс)",
         350.0f,
         150.0f,
         500.0f,
         25.0F
      );
      this.bmwStrengthXZ = new SliderSetting(
         "Цикл XZ",
         2000.0f,
         1000.0f,
         5000.0f,
         100.0f
      );
      this.bmwStrengthY = new SliderSetting(
         "Цикл Y",
         1700.0f,
         1000.0f,
         5000.0f,
         100.0f
      );
      this.appearValue = 0.0f;
      this.scaleValue = 0.0f;
      this.rotProgress = 0.0f;
      this.rotFrom = -280.0f;
      this.rotTo = 280.0f;
      this.lastRotateUpdate = System.currentTimeMillis();
      this.lastTarget = null;
      this.lastHandledTarget = null;
      this.lastTargetPos = null;
      this.lastTargetHeight = 1.7999999523162842f;
      this.lastTargetWidth = 0.6000000238418579f;
      this.bmwPoints = new CopyOnWriteArrayList<>();
      this.crystalRotationAngle = 0.0f;
      this.crystalAnimation = 0.0f;
      this.spawnAccumulator = 0.0f;
      this.lastCubeTime = 0L;
      this.cubeParticles = new ArrayList<>();
      this.renderCubeParticles = new ArrayList<>();
      this.size.visible(this::isImageMode);
      this.rotateSpeed.visible(this::isImageMode);
      this.bmwGhostCount.visible(() -> this.mode.is("Райдер"));
      this.bmwGhostLife.visible(() -> this.mode.is("Райдер"));
      this.bmwStrengthXZ.visible(() -> this.mode.is("Райдер"));
      this.bmwStrengthY.visible(() -> this.mode.is("Райдер"));
      this.ringRadius.visible(() -> this.mode.is("Кольцо"));
      this.ringSpeed.visible(() -> this.mode.is("Кольцо"));
      Setting[] var10001 = new Setting[10];
      var10001[0] = this.mode;
      var10001[1] = this.size;
      var10001[2] = this.rotateSpeed;
      var10001[3] = this.hurtColor;
      var10001[4] = this.ringRadius;
      var10001[5] = this.ringSpeed;
      var10001[6] = this.bmwGhostCount;
      var10001[7] = this.bmwGhostLife;
      var10001[8] = this.bmwStrengthXZ;
      var10001[9] = this.bmwStrengthY;
      this.addSettings(var10001);
   }

   @Override
   public void onDisable() {
      this.appearValue = 0.0f;
      this.scaleValue = 0.0f;
      this.lastTarget = null;
      this.lastHandledTarget = null;
      this.lastTargetPos = null;
      this.rotProgress = 0.0f;
      this.rotFrom = -280.0f;
      this.rotTo = 280.0f;
      this.bmwPoints.clear();
      this.crystalRotationAngle = 0.0f;
      this.crystalAnimation = 0.0f;
      this.spawnAccumulator = 0.0f;
      this.lastCubeTime = 0L;
      this.cubeParticles.clear();
      this.renderCubeParticles.clear();
      super.onDisable();
   }
   public boolean isImageMode() {
      return !this.mode.is("Картинка 1") && !this.mode.is("Картинка 2")
         ? false : true;
   }
   public Identifier getCaptureTexture() {
      return this.mode.is("Картинка 2")
         ? Identifier.of("obsidian", "textures/targetesp/targetesp_3.png")
         : Identifier.of("obsidian", "textures/targetesp/targetesp_2.png");
   }
   public Identifier getBloomTexture() {
      return Identifier.of("obsidian", "textures/targetesp/bloom.png");
   }
   public int getESPColor() {
      int color = ObsidianComponent_olorUtil.getThemeColor();
      if ((color >> (24) & 255) == 0) {
         color |= -16777216;
      }

      return color;
   }
   public float animateTo(float current, float target, float delta) {
      if (current < target) {
         current = Math.min(current + delta, target);
      } else if (current > target) {
         current = Math.max(current - delta, target);
      }

      return current;
   }
   public float getDistanceScale(Vec3d cameraPos, double worldX, double worldY, double worldZ) {
      double dx = worldX - cameraPos.x;
      double dy = worldY - cameraPos.y;
      double dz = worldZ - cameraPos.z;
      double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
      return (float)Math.max(
         0.1,
         distance * 0.007000000216066837
      );
   }

   @EventTarget(
      priority = -100
   )
   public void onRender3D(RenderEvent event) {
      if (mc != null && mc.player != null && mc.world != null) {
         KillAuraModule aura = ModuleManager.aura;
         boolean auraEnabled = (aura != null && aura.isEnable());
         LivingEntity target = auraEnabled ? aura.getTarget() : null;
         boolean hasTarget = (target != null && target.isAlive());
         float speed = 0.05000000074505806f;
         this.appearValue = this.animateTo(
            this.appearValue,
            hasTarget ? 1.0f : 0.0f,
            speed
         );
         this.scaleValue = this.animateTo(
            this.scaleValue,
            hasTarget ? 1.0f : 0.5f,
            speed
         );
         if (hasTarget) {
            this.lastTarget = target;
            this.lastHandledTarget = target;
         }

         if (this.mode.is("Кристаллы")) {
            float crystalSpeed = hasTarget
               ? 0.07000000029802322f
               : 0.04500000178813934f;
            this.crystalAnimation = this.animateTo(
               this.crystalAnimation,
               hasTarget ? 1.0f : 0.0f,
               crystalSpeed
            );
            if (hasTarget) {
               this.crystalRotationAngle = this.crystalRotationAngle + 0.800000011920929f;
            }
         }

         if (!(this.appearValue <= 0.0010000000474974513f)
            || hasTarget
            || this.mode.is("Кристаллы") && !(this.crystalAnimation <= 0.0010000000474974513f)) {
            if (hasTarget && target != null) {
               float td = event.getTickDelta();
               this.lastTargetPos = new Vec3d(
                  MathHelper.lerp((double)td, target.lastRenderX, target.getX()),
                  MathHelper.lerp((double)td, target.lastRenderY, target.getY()),
                  MathHelper.lerp((double)td, target.lastRenderZ, target.getZ())
               );
               this.lastTargetHeight = target.getHeight();
               this.lastTargetWidth = target.getWidth();
            }

            if (this.lastTargetPos != null) {
               if (this.mode.is("Райдер")) {
                  if (hasTarget && target != null) {
                     this.addBMWGhosts(
                        target,
                        event.getTickDelta(),
                        Math.max(1, Math.round(this.bmwGhostCount.getValue().floatValue())),
                        Math.max(1, Math.round(this.bmwGhostLife.getValue().floatValue())),
                        this.getESPColor()
                     );
                  }

                  this.bmwPoints.removeIf(ObsidianComponent_322::shouldRemove);
                  this.drawBMW3D(event);
               } else if (this.mode.is("Кристаллы")) {
                  LivingEntity crystalTarget = hasTarget ? target : this.lastTarget;
                  if ((crystalTarget != null || this.lastTargetPos != null)
                     && this.crystalAnimation > 0.009999999776482582f) {
                     this.renderCrystals3D(event.getMatrices(), crystalTarget, event.getTickDelta());
                  }
               } else {
                  if (this.isImageMode()) {
                     this.renderMarker3D(event);
                  }

                  if (this.mode.is("Души")) {
                     this.drawSouls3D(event);
                  }

                  if (this.mode.is("Призраки")) {
                     this.drawCelka3D(event);
                  }

                  if (this.mode.is("Кольцо")) {
                     this.drawRing3D(event);
                  }

                  if (this.mode.is("Кубы")) {
                     this.renderCubes(event, target, hasTarget);
                  }
               }
            }
         } else {
            this.lastTarget = null;
            this.lastTargetPos = null;
         }
      }
   }
   public void renderCubes(RenderEvent event, LivingEntity target, boolean hasTarget) {
      long now = System.currentTimeMillis();
      if (this.lastCubeTime == (0L)) {
         this.lastCubeTime = now;
      }

      float dt = Math.min(
         (float)(now - this.lastCubeTime) / 1000.0f,
         0.10000000149011612f
      );
      this.lastCubeTime = now;
      if (Float.isFinite(dt) && mc.gameRenderer != null && mc.gameRenderer.getCamera() != null) {
         if (hasTarget && target != null) {
            this.lastTarget = target;
            this.spawnAccumulator += dt;

            while (this.spawnAccumulator >= 0.02199999988079071f) {
               this.spawnAccumulator = this.spawnAccumulator - 0.02199999988079071f;
               if (this.cubeParticles.size() >= 72) {
                  break;
               }

               for (int i = 0; i < (1); i++) {
                  double rand = Math.random() * 360.0;
                  double px = Math.cos(Math.toRadians(rand)) * 0.7;
                  double py = 0.02
                     + Math.random() * 0.1;
                  double pz = Math.sin(Math.toRadians(rand)) * 0.7;
                  this.cubeParticles.add(new ObsidianComponent_238(target, px, py, pz));
               }
            }
         } else {
            this.spawnAccumulator = 0.0F;
         }

         this.renderCubeParticles.clear();

         for (int i = this.cubeParticles.size() - (1); i >= 0; i--) {
            ObsidianComponent_238 particle = this.cubeParticles.get(i);

            try {
               particle.update(dt, now, hasTarget ? target : null);
               if (particle.shouldRemove(now)) {
                  this.cubeParticles.remove(i);
               } else {
                  this.renderCubeParticles.add(particle);
               }
            } catch (Throwable var29) {
               this.cubeParticles.remove(i);
            }
         }

         if (!this.renderCubeParticles.isEmpty()) {
            float partialTicks = event.getTickDelta();
            MatrixStack matrices = event.getMatrices();
            Vec3d camPos = mc.gameRenderer.getCamera().getPos();
            LivingEntity colorTarget = hasTarget ? target : this.lastTarget;
            float hurtPC = this.getHurtPC(colorTarget);
            int baseColor = this.getESPColor();
            int redColor = ObsidianComponent_olorUtil.rgb(255, 3, 3);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            RenderSystem.depthMask(false);
            RenderSystem.blendFunc(770, 1);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            BufferBuilder faceBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            boolean hasFaces = false;
            int i = 0;

            for (int size = this.renderCubeParticles.size(); i < size; i++) {
               ObsidianComponent_238 particle = this.renderCubeParticles.get(i);

               try {
                  int particleColor = particle.getRenderColor(baseColor, redColor, hurtPC, now);
                  if ((particleColor >> 24 & (255)) > 0
                     && particle.appendCubeFaces(faceBuilder, matrices, camPos, partialTicks, particleColor)) {
                     hasFaces = true;
                  }
               } catch (Throwable var28) {
               }
            }

            if (hasFaces) {
               BufferRenderer.drawWithGlobalProgram(faceBuilder.end());
            }

            BufferBuilder lineBuilder = Tessellator.getInstance().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            boolean hasLines = false;
            int ix = 0;

            for (int size = this.renderCubeParticles.size(); ix < size; ix++) {
               ObsidianComponent_238 particle = this.renderCubeParticles.get(ix);

               try {
                  int particleColor = particle.getRenderColor(baseColor, redColor, hurtPC, now);
                  if ((particleColor >> 24 & (255)) > 0
                     && particle.appendCubeLines(lineBuilder, matrices, camPos, partialTicks, particleColor)) {
                     hasLines = true;
                  }
               } catch (Throwable var27) {
               }
            }

            if (hasLines) {
               BufferRenderer.drawWithGlobalProgram(lineBuilder.end());
            }

            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.setShaderTexture(0, this.getBloomTexture());
            BufferBuilder bloomBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            boolean hasBloom = false;
            float camYaw = mc.gameRenderer.getCamera().getYaw();
            float camPitch = mc.gameRenderer.getCamera().getPitch();
            int ixx = 0;

            for (int size = this.renderCubeParticles.size(); ixx < size; ixx++) {
               ObsidianComponent_238 particle = this.renderCubeParticles.get(ixx);

               try {
                  int particleColor = particle.getRenderColor(baseColor, redColor, hurtPC, now);
                  if (particle.appendBloom(bloomBuilder, matrices, camPos, camYaw, camPitch, partialTicks, particleColor, now)) {
                     hasBloom = true;
                  }
               } catch (Throwable var26) {
               }
            }

            if (hasBloom) {
               BufferRenderer.drawWithGlobalProgram(bloomBuilder.end());
            }

            RenderSystem.depthMask(true);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
         }
      }
   }
   public void drawRing3D(RenderEvent event) {
      if (!(this.appearValue <= 0.0010000000474974513f) && this.lastTargetPos != null) {
         float partialTicks = mc.getRenderTickCounter().getTickDelta(true);
         LivingEntity target = this.lastTarget;
         Vec3d vec;
         float entityHeight;
         if (target != null && target.isAlive()) {
            vec = new Vec3d(
               MathHelper.lerp((double)partialTicks, target.lastRenderX, target.getX()),
               MathHelper.lerp((double)partialTicks, target.lastRenderY, target.getY()),
               MathHelper.lerp((double)partialTicks, target.lastRenderZ, target.getZ())
            );
            entityHeight = target.getHeight();
         } else {
            vec = this.lastTargetPos;
            entityHeight = this.lastTargetHeight;
         }

         Vec3d cam = mc.gameRenderer.getCamera().getPos();
         double x = vec.x - cam.x;
         double y = vec.y - cam.y;
         double z = vec.z - cam.z;
         double duration = 2000.0 / (double)this.ringSpeed.get();
         double elapsed = (double)(System.currentTimeMillis() % (long)duration);
         boolean side = (elapsed > duration / 2.0);
         double progress = elapsed / (duration / 2.0);
         if (side) {
            progress -= 1.0;
         } else {
            progress = 1.0 - progress;
         }

         progress = progress < 0.5
            ? 2.0 * progress * progress
            : 1.0
               - Math.pow(
                     Double.longBitsToDouble(-4611686018427387904L) * progress
                        + 2.0,
                     2.0
                  )
                  / 2.0;
         double eased = (double)entityHeight
            / 1.2
            * (
               progress > 0.5
                  ? 1.0 - progress
                  : progress
            )
            * (double)(side ? -1 : 1);
         int baseCol = this.getESPColor();
         float hurtPC = this.getHurtPC(target);
         int redCol = ObsidianComponent_olorUtil.rgb(255, 3, 3);
         int mainColor = this.overCol(baseCol, redCol, hurtPC);
         int colorWithAlpha = this.setAlpha(mainColor, 0.8823529481887817f * this.appearValue);
         int colorTransparent = this.setAlpha(mainColor, 0.003921568859368563f * this.appearValue);
         int colorFull = this.setAlpha(mainColor, this.appearValue);
         double radius = (double)this.ringRadius.get();
         MatrixStack matrices = event.getMatrices();
         Matrix4f matrix = matrices.peek().getPositionMatrix();
         RenderSystem.depthMask(false);
         RenderSystem.disableDepthTest();
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 1);
         RenderSystem.disableCull();
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

         for (int i = 0; i <= 360; i++) {
            double rad = Math.toRadians((double)i);
            float px = (float)(x + Math.cos(rad) * radius);
            float pz = (float)(z + Math.sin(rad) * radius);
            float py1 = (float)(y + (double)entityHeight * progress);
            float py2 = (float)(y + (double)entityHeight * progress + eased);
            buffer.vertex(matrix, px, py1, pz).color(colorWithAlpha);
            buffer.vertex(matrix, px, py2, pz).color(colorTransparent);
         }

         BufferRenderer.drawWithGlobalProgram(buffer.end());
         RenderSystem.lineWidth(1.5f);
         BufferBuilder lineBuffer = Tessellator.getInstance().begin(DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

         for (int i = 0; i <= (360); i++) {
            double rad = Math.toRadians((double)i);
            float px = (float)(x + Math.cos(rad) * radius);
            float pz = (float)(z + Math.sin(rad) * radius);
            float py = (float)(y + (double)entityHeight * progress);
            lineBuffer.vertex(matrix, px, py, pz).color(colorFull);
         }

         BufferRenderer.drawWithGlobalProgram(lineBuffer.end());
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
      }
   }
   public int setAlpha(int color, float alpha) {
      alpha = Math.max(
         0.0f, Math.min(1.0f, alpha)
      );
      return color & (16777215)
         | (int)(alpha * 255.0f) << (24);
   }

   @EventTarget(
      priority = -100
   )
   public void onRender2D(EventRender2D event) {
      if (this.mode.is("Кристаллы")
         && !(this.crystalAnimation <= 0.0010000000474974513f)
         && this.lastTargetPos != null) {
         LivingEntity crystalTarget = this.lastTarget != null && this.lastTarget.isAlive() ? this.lastTarget : null;
         this.drawCrystalGlow2D(event.getContext().getMatrices(), crystalTarget);
      }
   }
   public int multAlpha(int color, float mult) {
      int a = (int)((float)(color >> (24) & 255) * mult);
      a = Math.max(0, Math.min(255, a));
      return a << (24) | color & (16777215);
   }
   public int replAlpha(int color, int alpha) {
      alpha = Math.max(0, Math.min(255, alpha));
      return alpha << (24) | color & 16777215;
   }

   public int overCol(int color1, int color2, float factor) {
      factor = Math.max(0.0f, Math.min(1.0f, factor));
      int r1 = color1 >> (16) & 255;
      int g1 = color1 >> (8) & (255);
      int b1 = color1 & 255;
      int a1 = color1 >> (24) & (255);
      int r2 = color2 >> 16 & 255;
      int g2 = color2 >> (8) & 255;
      int b2 = color2 & 255;
      int a2 = color2 >> (24) & (255);
      int r = (int)((float)r1 + (float)(r2 - r1) * factor);
      int g = (int)((float)g1 + (float)(g2 - g1) * factor);
      int b = (int)((float)b1 + (float)(b2 - b1) * factor);
      int a = (int)((float)a1 + (float)(a2 - a1) * factor);
      return a << 24
         | r << 16
         | g << 8
         | b;
   }
   public float getHurtPC(LivingEntity target) {
      if (this.hurtColor.isState() && target != null) {
         float partialTicks = mc != null
            ? mc.getRenderTickCounter().getTickDelta(true)
            : 0.0f;
         float hurtTicks = MathHelper.clamp(
            (float)target.hurtTime - partialTicks, 0.0f, 10.0f
         );
         float progress = hurtTicks / 10.0f;
         return progress
            * progress
            * (3.0f - 2.0f * progress);
      } else {
         return 0.0f;
      }
   }
   public void drawBillboard(
      MatrixStack matrices, Vec3d cameraPos, double worldX, double worldY, double worldZ, float baseScreenSize, int color, float rotation
   ) {
      float distScale = this.getDistanceScale(cameraPos, worldX, worldY, worldZ);
      float half = baseScreenSize * distScale * 0.5f;
      this.drawBillboardInternal(matrices, cameraPos, worldX, worldY, worldZ, half, color, rotation);
   }
   public void drawStaticBillboard(
      MatrixStack matrices, Vec3d cameraPos, double worldX, double worldY, double worldZ, float worldSize, int color, float rotation
   ) {
      float half = worldSize * 0.5f;
      this.drawBillboardInternal(matrices, cameraPos, worldX, worldY, worldZ, half, color, rotation);
   }
   public void drawBillboardInternal(MatrixStack matrices, Vec3d cameraPos, double worldX, double worldY, double worldZ, float half, int color, float rotation) {
      int r = color >> 16 & 255;
      int g = color >> (8) & (255);
      int b = color & (255);
      int a = color >> 24 & (255);
      if (a > 0) {
         matrices.push();
         matrices.translate(worldX - cameraPos.x, worldY - cameraPos.y, worldZ - cameraPos.z);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mc.gameRenderer.getCamera().getYaw()));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mc.gameRenderer.getCamera().getPitch()));
         if (rotation != 0.0f) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotation));
         }

         Matrix4f matrix = matrices.peek().getPositionMatrix();
         BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         buffer.vertex(matrix, -half, -half, 0.0f)
            .texture(0.0f, 1.0f)
            .color(r, g, b, a);
         buffer.vertex(matrix, -half, half, 0.0f)
            .texture(0.0f, 0.0f)
            .color(r, g, b, a);
         buffer.vertex(matrix, half, half, 0.0f)
            .texture(1.0f, 0.0f)
            .color(r, g, b, a);
         buffer.vertex(matrix, half, -half, 0.0f)
            .texture(1.0f, 1.0f)
            .color(r, g, b, a);
         BufferRenderer.drawWithGlobalProgram(buffer.end());
         matrices.pop();
      }
   }
   public void renderMarker3D(RenderEvent event) {
      if (this.lastTargetPos != null && !(this.appearValue <= 0.0010000000474974513f)) {
         Vec3d cam = mc.gameRenderer.getCamera().getPos();
         double worldX = this.lastTargetPos.x;
         double worldY = this.lastTargetPos.y
            + (double)(
               (this.lastTargetHeight + 0.4000000059604645f) * 0.5f
            );
         double worldZ = this.lastTargetPos.z;
         float baseSize = this.size.getValue().floatValue() * 12.0f;
         float renderSize = baseSize * this.scaleValue;
         long now = System.currentTimeMillis();
         float dt = Math.max(
            0.0010000000474974513f,
            (float)(now - this.lastRotateUpdate) / 1000.0f
         );
         this.lastRotateUpdate = now;
         float cycleDuration = Math.max(
            0.3499999940395355f,
            2.200000047683716f / this.rotateSpeed.getValue().floatValue()
         );

         for (this.rotProgress += dt / cycleDuration;
            this.rotProgress >= 1.0f;
            this.rotTo = this.rotTo > 0.0f
               ? -280.0f
               : 280.0f
         ) {
            this.rotProgress = this.rotProgress - 1.0f;
            this.rotFrom = this.rotTo;
         }

         float accel = (float)ObsidianComponent_57.SINE_IN_OUT.ease((double)this.rotProgress);
         float rotation = MathHelper.lerp(accel, this.rotFrom, this.rotTo);
         float hurtPC = this.getHurtPC(this.lastTarget);
         int baseColor = this.multAlpha(this.getESPColor(), this.appearValue);
         int redColor = this.multAlpha(
            ObsidianComponent_olorUtil.rgb(255, 3, 3), this.appearValue
         );
         int color = this.overCol(baseColor, redColor, hurtPC);
         RenderSystem.enableBlend();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.disableCull();
         RenderSystem.blendFunc(770, 1);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
         RenderSystem.setShaderTexture(0, this.getCaptureTexture());
         this.drawBillboard(event.getMatrices(), cam, worldX, worldY, worldZ, renderSize, color, rotation);
         RenderSystem.enableCull();
         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
      }
   }
   public void drawSouls3D(RenderEvent event) {
      if (!(this.appearValue <= 0.0010000000474974513f) && this.lastTargetPos != null) {
         float partialTicks = mc.getRenderTickCounter().getTickDelta(true);
         LivingEntity target = this.lastTarget;
         Vec3d vec;
         float height;
         if (target != null && target.isAlive()) {
            vec = new Vec3d(
               MathHelper.lerp((double)partialTicks, target.lastRenderX, target.getX()),
               MathHelper.lerp((double)partialTicks, target.lastRenderY, target.getY()),
               MathHelper.lerp((double)partialTicks, target.lastRenderZ, target.getZ())
            );
            height = target.getHeight();
         } else {
            vec = this.lastTargetPos;
            height = this.lastTargetHeight;
         }

         Vec3d cam = mc.gameRenderer.getCamera().getPos();
         double baseX = vec.x;
         double baseY = vec.y + (double)(height / 2.0f);
         double baseZ = vec.z;
         double radius = 0.7;
         float fixedSize = 4.0f;
         long time = System.currentTimeMillis();
         float hurtPC = this.getHurtPC(target);
         int baseCol = this.getESPColor();
         int redCol = ObsidianComponent_olorUtil.rgb(255, 3, 3);
         RenderSystem.disableDepthTest();
         RenderSystem.enableBlend();
         RenderSystem.depthMask(false);
         RenderSystem.disableCull();
         RenderSystem.blendFunc(770, 1);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
         RenderSystem.setShaderTexture(0, this.getBloomTexture());
         MatrixStack matrices = event.getMatrices();

         for (int i = 0; i < 20; i++) {
            float trailFactor = 1.0f
               - (float)i / 20.0f * 0.699999988079071f;
            double angle = 0.15
               * ((double)time - (double)i * 10.0)
               / 25.0;
            double s = Math.sin(angle) * radius;
            double c = Math.cos(angle) * radius;
            double worldX = baseX + s;
            double worldY = baseY + c;
            double worldZ = baseZ - c;
            float sz = fixedSize * trailFactor;
            float alphaTrail = this.appearValue * 0.6000000238418579f;
            int col = this.multAlpha(baseCol, alphaTrail * this.appearValue);
            int red = this.multAlpha(redCol, alphaTrail * this.appearValue);
            int color = this.overCol(col, red, hurtPC);
            this.drawStaticBillboard(
               matrices,
               cam,
               worldX,
               worldY,
               worldZ,
               sz * 0.11999999731779099f,
               color,
               0.0f
            );
            int glowColor = this.multAlpha(color, 0.44999998807907104f);
            this.drawStaticBillboard(
               matrices,
               cam,
               worldX,
               worldY,
               worldZ,
               sz * 0.20999999344348907f,
               glowColor,
               0.0f
            );
         }

         for (int i = 0; i < 20; i++) {
            float trailFactor = 1.0f
               - (float)i / 20.0f * 0.699999988079071f;
            double angle = 0.15
               * ((double)time - (double)i * 10.0)
               / 25.0;
            double s = Math.sin(angle) * radius;
            double c = Math.cos(angle) * radius;
            double worldX = baseX - s;
            double worldY = baseY + s;
            double worldZ = baseZ - c;
            float sz = fixedSize * trailFactor;
            float alphaTrail = this.appearValue * 0.6F;
            int col = this.multAlpha(baseCol, alphaTrail * this.appearValue);
            int red = this.multAlpha(
               ObsidianComponent_olorUtil.rgb(235, 7, 7),
               alphaTrail * this.appearValue
            );
            int color = this.overCol(col, red, hurtPC);
            this.drawStaticBillboard(
               matrices,
               cam,
               worldX,
               worldY,
               worldZ,
               sz * 0.11999999731779099f,
               color,
               0.0f
            );
            int glowColor = this.multAlpha(color, 0.44999998807907104f);
            this.drawStaticBillboard(
               matrices,
               cam,
               worldX,
               worldY,
               worldZ,
               sz * 0.20999999344348907f,
               glowColor,
               0.0f
            );
         }

         for (int i = 0; i < 20; i++) {
            float trailFactor = 1.0f
               - (float)i / 20.0f * 0.699999988079071f;
            double angle = 0.15
               * ((double)time - (double)i * 10.0)
               / 25.0;
            double s = Math.sin(angle) * radius;
            double c = Math.cos(angle) * radius;
            double worldX = baseX - s;
            double worldY = baseY - s;
            double worldZ = baseZ + c;
            float sz = fixedSize * trailFactor;
            float alphaTrail = this.appearValue * 0.6000000238418579f;
            int col = this.multAlpha(baseCol, alphaTrail * this.appearValue);
            int red = this.multAlpha(redCol, alphaTrail * this.appearValue);
            int color = this.overCol(col, red, hurtPC);
            this.drawStaticBillboard(
               matrices,
               cam,
               worldX,
               worldY,
               worldZ,
               sz * 0.11999999731779099f,
               color,
               0.0f
            );
            int glowColor = this.multAlpha(color, 0.44999998807907104f);
            this.drawStaticBillboard(matrices, cam, worldX, worldY, worldZ, sz * 0.21F, glowColor, 0.0f);
         }

         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
         RenderSystem.depthMask(true);
      }
   }
   public void addBMWGhosts(LivingEntity entity, float partialTicks, int cornersCount, int maxTime, int colorBase) {
      float xzRange = 0.699999988079071f;
      float yRange = entity.getHeight();
      int delayXZ = (int)this.bmwStrengthXZ.getValue().floatValue();
      int delayY = (int)this.bmwStrengthY.getValue().floatValue();
      long time = System.currentTimeMillis();
      float rotateProgress = (float)(time % (long)delayXZ) / (float)delayXZ;
      float xzRotate = rotateProgress * 360.0f;
      float yProgress = (float)(time % (long)delayY) / (float)delayY;
      float yLrpPC = 0.5f
         - 0.5f * MathHelper.cos(yProgress * 6.2831854820251465f);

      for (int corner = 0; corner < cornersCount; corner++) {
         float cornersPC = (float)corner / (float)cornersCount;
         double yawRad = Math.toRadians((double)MathHelper.wrapDegrees(cornersPC * 360.0f + xzRotate));
         float offsetX = -((float)Math.sin(yawRad)) * xzRange;
         float offsetY = yRange * yLrpPC;
         float offsetZ = (float)Math.cos(yawRad) * xzRange;
         this.bmwPoints.add(new ObsidianComponent_322(offsetX, offsetY, offsetZ, maxTime, colorBase));
      }
   }
   public void drawBMW3D(RenderEvent event) {
      if (!this.bmwPoints.isEmpty() && !(this.appearValue <= 0.0010000000474974513f)) {
         LivingEntity renderTarget = this.lastTarget != null ? this.lastTarget : this.lastHandledTarget;
         if (renderTarget != null || this.lastTargetPos != null) {
            float partialTicks = mc.getRenderTickCounter().getTickDelta(true);
            Vec3d basePos;
            if (renderTarget != null && renderTarget.isAlive()) {
               basePos = new Vec3d(
                  MathHelper.lerp((double)partialTicks, renderTarget.lastRenderX, renderTarget.getX()),
                  MathHelper.lerp((double)partialTicks, renderTarget.lastRenderY, renderTarget.getY()),
                  MathHelper.lerp((double)partialTicks, renderTarget.lastRenderZ, renderTarget.getZ())
               );
            } else {
               basePos = this.lastTargetPos;
            }

            if (basePos != null) {
               Vec3d cam = mc.gameRenderer.getCamera().getPos();
               float hurtPC = this.getHurtPC(renderTarget);
               float fixedScreenSize = 6.0f;
               RenderSystem.disableDepthTest();
               RenderSystem.enableBlend();
               RenderSystem.depthMask(false);
               RenderSystem.disableCull();
               RenderSystem.blendFunc(770, 1);
               RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
               RenderSystem.setShaderTexture(0, this.getBloomTexture());
               MatrixStack matrices = event.getMatrices();

               for (ObsidianComponent_322 point : this.bmwPoints) {
                  float timePC = point.getTimeProgress();
                  float trailFactor = 1.0F - timePC * 0.6000000238418579f;
                  double worldX = basePos.x + (double)point.x;
                  double worldY = basePos.y + (double)point.y;
                  double worldZ = basePos.z + (double)point.z;
                  float sz = fixedScreenSize * trailFactor;
                  int alpha = (int)(
                     255.0f
                        * this.appearValue
                        * trailFactor
                        * 0.800000011920929f
                  );
                  alpha = Math.max(0, Math.min(255, alpha));
                  int col = this.replAlpha(point.baseColor, alpha);
                  int red = this.replAlpha(ObsidianComponent_olorUtil.rgb(255, 3, 3), alpha);
                  int finalColor = this.overCol(col, red, hurtPC);
                  this.drawBillboard(matrices, cam, worldX, worldY, worldZ, sz, finalColor, 0.0f);
               }

               RenderSystem.enableCull();
               RenderSystem.enableDepthTest();
               RenderSystem.defaultBlendFunc();
               RenderSystem.disableBlend();
               RenderSystem.depthMask(true);
            }
         }
      }
   }
   public void drawCelka3D(RenderEvent event) {
      if (!(this.appearValue <= 0.0010000000474974513f) && this.lastTargetPos != null) {
         float partialTicks = mc.getRenderTickCounter().getTickDelta(true);
         LivingEntity target = this.lastTarget;
         Vec3d vec;
         if (target != null && target.isAlive()) {
            vec = new Vec3d(
               MathHelper.lerp((double)partialTicks, target.lastRenderX, target.getX()),
               MathHelper.lerp((double)partialTicks, target.lastRenderY, target.getY()),
               MathHelper.lerp((double)partialTicks, target.lastRenderZ, target.getZ())
            );
            float entityHeight = target.getHeight();
         } else {
            vec = this.lastTargetPos;
            float entityHeight = this.lastTargetHeight;
         }

         Vec3d cam = mc.gameRenderer.getCamera().getPos();
         double bx = vec.x;
         double by = vec.y;
         double bz = vec.z;
         double t = (double)System.currentTimeMillis()
            / 384.61539872299335
            * 1.2000000476837158;
         double tv = (double)System.currentTimeMillis()
            / 666.6666666666666
            * 1.2000000476837158;
         int baseCol = this.getESPColor();
         float fixedSize = 4.0f;
         RenderSystem.disableDepthTest();
         RenderSystem.enableBlend();
         RenderSystem.depthMask(false);
         RenderSystem.disableCull();
         RenderSystem.blendFunc(770, 1);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
         RenderSystem.setShaderTexture(0, this.getBloomTexture());
         MatrixStack matrices = event.getMatrices();
         float radius = 0.6499999761581421f;

         for (int k = 0; k < (4); k++) {
            for (int j = 0; j < (20); j++) {
               float kf = (float)j / 20.0F;
               float sizeFactor = 1.0f - kf * 0.550000011920929f;
               double tj = t - (double)j * 0.05;
               double tvj = tv - (double)j * 0.05;
               double cyc = (Math.sin(tvj) + 1.0)
                  * 0.5;
               double baseAngle = Math.toRadians(
                  (double)k * 90.0
                     + tj
                        * 50.0
                        % 360.0
               );
               double offX = Math.cos(baseAngle) * (double)radius;
               double offZ = Math.sin(baseAngle) * (double)radius;
               double offY = k % 2 == 0
                  ? 0.1
                     + 1.7 * cyc
                  : 1.8
                     - 1.7 * cyc;
               double worldX = bx + offX;
               double worldY = by + offY;
               double worldZ = bz + offZ;
               float sz = fixedSize * sizeFactor;
               int finalAlpha = (int)(
                  255.0f
                     * this.appearValue
                     * 0.6000000238418579f
               );
               int color = this.replAlpha(baseCol, finalAlpha);
               this.drawBillboard(matrices, cam, worldX, worldY, worldZ, sz, color, 0.0f);
               int glowColor = this.multAlpha(color, 0.44999998807907104f);
               this.drawBillboard(
                  matrices,
                  cam,
                  worldX,
                  worldY,
                  worldZ,
                  sz * 1.75f,
                  glowColor,
                  0.0f
               );
            }

            radius *= -1.0f;
         }

         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
         RenderSystem.depthMask(true);
      }
   }
   public void renderCrystals3D(MatrixStack ms, LivingEntity target, float partialTicks) {
      if (this.lastTargetPos != null && !(this.crystalAnimation <= 0.009999999776482582f)) {
         Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
         int baseColor = ObsidianComponent_olorUtil.getThemeColor();
         int color = this.multAlpha(baseColor, this.crystalAnimation);
         int glowColor = this.multAlpha(baseColor, this.crystalAnimation * 0.2800000011920929f);
         float hurtPC = this.getHurtPC(target);
         if (hurtPC > 0.0f) {
            int hurtColor = this.multAlpha(
               ObsidianComponent_olorUtil.rgb(255, 3, 3), this.crystalAnimation
            );
            color = this.overCol(color, hurtColor, hurtPC);
            glowColor = this.overCol(glowColor, this.multAlpha(hurtColor, 0.6499999761581421f), hurtPC);
         }

         float entityWidth = target != null ? target.getWidth() : this.lastTargetWidth;
         float entityHeight = target != null ? target.getHeight() : this.lastTargetHeight;
         float width = entityWidth * 1.5f;
         Vec3d renderPos;
         if (target != null && target.isAlive()) {
            renderPos = new Vec3d(
               MathHelper.lerp((double)partialTicks, target.lastRenderX, target.getX()),
               MathHelper.lerp((double)partialTicks, target.lastRenderY, target.getY()),
               MathHelper.lerp((double)partialTicks, target.lastRenderZ, target.getZ())
            );
         } else {
            renderPos = this.lastTargetPos;
         }

         RenderSystem.disableDepthTest();
         RenderSystem.enableBlend();
         RenderSystem.depthMask(false);
         RenderSystem.disableCull();
         float orbitScale = 1.2000000476837158f - 0.5f * this.crystalAnimation;
         ms.push();
         ms.translate(renderPos.x - cameraPos.x, renderPos.y - cameraPos.y, renderPos.z - cameraPos.z);
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

         for (int i = 0; i < 360; i += 20) {
            float angleRad = (float)Math.toRadians((double)((float)i + this.crystalRotationAngle));
            float sin = (float)(Math.sin((double)angleRad) * (double)width * (double)orbitScale);
            float cos = (float)(Math.cos((double)angleRad) * (double)width * (double)orbitScale);
            float crystalSize = 0.10000000149011612f;
            float yOffset = 0.10000000149011612f + entityHeight * Math.abs(MathHelper.sin((float)i));
            float targetCenterY = entityHeight / 2.0f;
            float dirX = -sin;
            float dirY = targetCenterY - yOffset;
            float dirZ = -cos;
            float length = (float)Math.sqrt((double)(dirX * dirX + dirY * dirY + dirZ * dirZ));
            if (!(length < 0.0010000000474974513f)) {
               dirX /= length;
               dirY /= length;
               dirZ /= length;
               ms.push();
               ms.translate(sin, yOffset, cos);
               Vector3f initial = new Vector3f(
                  0.0f,
                  1.0f,
                  0.0f
               );
               Vector3f dir = new Vector3f(dirX, dirY, dirZ);
               Vector3f axis = new Vector3f();
               initial.cross(dir, axis);
               float axisLen = axis.length();
               if (axisLen >= 0.0010000000474974513f) {
                  axis.div(axisLen);
                  float dot = Math.max(
                     -1.0f, Math.min(1.0f, initial.dot(dir))
                  );
                  float angle = (float)Math.acos((double)dot);
                  ms.multiply(new Quaternionf().setAngleAxis(angle, axis.x, axis.y, axis.z));
               }

               this.renderCrystalShape(buffer, ms.peek().getPositionMatrix(), crystalSize, color);
               ms.pop();
            }
         }

         BufferRenderer.drawWithGlobalProgram(buffer.end());
         ms.pop();
         float glowBaseSize = 4.5f
            + entityWidth * 3.0f;
         float outerGlowSize = glowBaseSize * 1.2799999713897705f;
         RenderSystem.blendFunc(770, 1);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
         RenderSystem.setShaderTexture(0, this.getBloomTexture());

         for (int ix = 0; ix < (360); ix += 20) {
            float angleRad = (float)Math.toRadians((double)((float)ix + this.crystalRotationAngle));
            float sin = (float)(Math.sin((double)angleRad) * (double)width * (double)orbitScale);
            float cos = (float)(Math.cos((double)angleRad) * (double)width * (double)orbitScale);
            float yOffset = 0.10000000149011612f + entityHeight * Math.abs(MathHelper.sin((float)ix));
            double worldX = renderPos.x + (double)sin;
            double worldY = renderPos.y + (double)yOffset;
            double worldZ = renderPos.z + (double)cos;
            this.drawBillboard(
               ms,
               cameraPos,
               worldX,
               worldY,
               worldZ,
               outerGlowSize,
               this.multAlpha(glowColor, 0.23999999463558197f),
               this.crystalRotationAngle + (float)ix
            );
            this.drawBillboard(
               ms,
               cameraPos,
               worldX,
               worldY,
               worldZ,
               glowBaseSize,
               glowColor,
               -(this.crystalRotationAngle + (float)ix * 0.5f)
            );
         }

         RenderSystem.enableDepthTest();
         RenderSystem.enableCull();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
         RenderSystem.depthMask(true);
      }
   }
   public void renderCrystalShape(BufferBuilder buffer, Matrix4f matrix, float size, int color) {
      int r = color >> 16 & (255);
      int g = color >> (8) & 255;
      int b = color & 255;
      int a = color >> (24) & (255);
      float w = 0.3400000035762787f * size / 0.10000000149011612f;
      float h = 1.149999976158142f * size / 0.10000000149011612f;
      w = 0.05999999865889549f;
      h = 0.20000000298023224f;
      this.tri(
         buffer,
         matrix,
         0.0f,
         h,
         0.0f,
         w,
         0.0f,
         0.0f,
         0.0f,
         0.0f,
         w,
         r,
         g,
         b,
         a
      );
      this.tri(
         buffer,
         matrix,
         0.0f,
         h,
         0.0f,
         0.0f,
         0.0f,
         w,
         -w,
         0.0f,
         0.0f,
         r,
         g,
         b,
         a
      );
      this.tri(
         buffer,
         matrix,
         0.0f,
         h,
         0.0f,
         -w,
         0.0f,
         0.0f,
         0.0f,
         0.0f,
         -w,
         r,
         g,
         b,
         a
      );
      this.tri(
         buffer,
         matrix,
         0.0f,
         h,
         0.0f,
         0.0f,
         0.0f,
         -w,
         w,
         0.0f,
         0.0f,
         r,
         g,
         b,
         a
      );
      this.tri(
         buffer,
         matrix,
         0.0f,
         -h,
         0.0f,
         w,
         0.0f,
         0.0f,
         0.0f,
         0.0f,
         w,
         r,
         g,
         b,
         a
      );
      this.tri(
         buffer,
         matrix,
         0.0f,
         -h,
         0.0f,
         0.0f,
         0.0f,
         w,
         -w,
         0.0f,
         0.0f,
         r,
         g,
         b,
         a
      );
      this.tri(
         buffer,
         matrix,
         0.0f,
         -h,
         0.0f,
         -w,
         0.0f,
         0.0f,
         0.0f,
         0.0f,
         -w,
         r,
         g,
         b,
         a
      );
      this.tri(
         buffer,
         matrix,
         0.0f,
         -h,
         0.0f,
         0.0f,
         0.0f,
         -w,
         w,
         0.0f,
         0.0f,
         r,
         g,
         b,
         a
      );
   }
   public float[] project2D(double worldX, double worldY, double worldZ) {
      return null;
   }
   public double getScale(double worldX, double worldY, double worldZ) {
      Vec3d cam = mc.gameRenderer.getCamera().getPos();
      double dx = worldX - cam.x;
      double dy = worldY - cam.y;
      double dz = worldZ - cam.z;
      double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
      return Math.max(
         0.5,
         8.0
            / Math.max(0.1, distance)
      );
   }
   public void drawTexturedRect2D(MatrixStack matrix, float x, float y, float width, float height, int color) {
      int r = color >> (16) & (255);
      int g = color >> 8 & (255);
      int b = color & 255;
      int a = color >> (24) & 255;
      if (a > 0) {
         Matrix4f mat = matrix.peek().getPositionMatrix();
         BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         buffer.vertex(mat, x, y, 0.0f)
            .texture(0.0f, 0.0f)
            .color(r, g, b, a);
         buffer.vertex(mat, x, y + height, 0.0f)
            .texture(0.0f, 1.0f)
            .color(r, g, b, a);
         buffer.vertex(mat, x + width, y + height, 0.0f)
            .texture(1.0f, 1.0f)
            .color(r, g, b, a);
         buffer.vertex(mat, x + width, y, 0.0f)
            .texture(1.0f, 0.0f)
            .color(r, g, b, a);
         BufferRenderer.drawWithGlobalProgram(buffer.end());
      }
   }
   public void drawCrystalGlow2D(MatrixStack matrix, LivingEntity target) {
   }
   public void tri(
      BufferBuilder buffer,
      Matrix4f matrix,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      int r,
      int g,
      int b,
      int a
   ) {
      buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a);
      buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a);
      buffer.vertex(matrix, x3, y3, z3).color(r, g, b, a);
   }

   static {
      byte[][] var10000 = new byte[12][];
      int var10002 = 0;
      byte[] var10003 = new byte[6];
      var10003[0] = (byte)(-1);
      var10003[1] = (byte)(-1);
      var10003[2] = (byte)(-1);
      var10003[3] = (byte)(1);
      var10003[4] = (byte)(-1);
      var10003[5] = (byte)(-1);
      var10000[var10002] = var10003;
      var10003 = new byte[6];
      var10003[0] = (byte)(1);
      var10003[1] = (byte)(-1);
      var10003[2] = (byte)(-1);
      var10003[3] = (byte)(1);
      var10003[4] = (byte)(-1);
      var10003[5] = (byte)(1);
      var10000[1] = var10003;
      var10002 = 2;
      var10003 = new byte[6];
      var10003[0] = (byte)(1);
      var10003[1] = (byte)(-1);
      var10003[2] = (byte)(1);
      var10003[3] = (byte)(-1);
      var10003[4] = (byte)(-1);
      var10003[5] = (byte)(1);
      var10000[var10002] = var10003;
      var10002 = 3;
      var10003 = new byte[6];
      var10003[0] = (byte)(-1);
      var10003[1] = (byte)(-1);
      var10003[2] = (byte)(1);
      var10003[3] = (byte)(-1);
      var10003[4] = (byte)(-1);
      var10003[5] = (byte)(-1);
      var10000[var10002] = var10003;
      var10002 = 4;
      var10003 = new byte[6];
      var10003[0] = (byte)(-1);
      var10003[1] = (byte)(1);
      var10003[2] = (byte)(-1);
      var10003[3] = (byte)(1);
      var10003[4] = (byte)(1);
      var10003[5] = (byte)(-1);
      var10000[var10002] = var10003;
      var10002 = 5;
      var10003 = new byte[6];
      var10003[0] = (byte)(1);
      var10003[1] = (byte)(1);
      var10003[2] = (byte)(-1);
      var10003[3] = (byte)(1);
      var10003[4] = (byte)(1);
      var10003[5] = (byte)(1);
      var10000[var10002] = var10003;
      var10002 = 6;
      var10003 = new byte[6];
      var10003[0] = (byte)(1);
      var10003[1] = (byte)(1);
      var10003[2] = (byte)(1);
      var10003[3] = (byte)(-1);
      var10003[4] = (byte)(1);
      var10003[5] = (byte)(1);
      var10000[var10002] = var10003;
      var10002 = 7;
      var10003 = new byte[6];
      var10003[0] = (byte)(-1);
      var10003[1] = (byte)(1);
      var10003[2] = (byte)(1);
      var10003[3] = (byte)(-1);
      var10003[4] = (byte)(1);
      var10003[5] = (byte)(-1);
      var10000[var10002] = var10003;
      var10002 = 8;
      var10003 = new byte[6];
      var10003[0] = (byte)(-1);
      var10003[1] = (byte)(-1);
      var10003[2] = (byte)(-1);
      var10003[3] = (byte)(-1);
      var10003[4] = (byte)(1);
      var10003[5] = (byte)(-1);
      var10000[var10002] = var10003;
      var10002 = 9;
      var10003 = new byte[6];
      var10003[0] = 1;
      var10003[1] = (byte)(-1);
      var10003[2] = (byte)(-1);
      var10003[3] = (byte)(1);
      var10003[4] = (byte)(1);
      var10003[5] = (byte)(-1);
      var10000[var10002] = var10003;
      var10002 = 10;
      var10003 = new byte[6];
      var10003[0] = (byte)(1);
      var10003[1] = (byte)(-1);
      var10003[2] = (byte)(1);
      var10003[3] = (byte)(1);
      var10003[4] = (byte)(1);
      var10003[5] = (byte)(1);
      var10000[var10002] = var10003;
      var10002 = 11;
      var10003 = new byte[6];
      var10003[0] = (byte)(-1);
      var10003[1] = (byte)(-1);
      var10003[2] = (byte)(1);
      var10003[3] = (byte)(-1);
      var10003[4] = (byte)(1);
      var10003[5] = (byte)(1);
      var10000[var10002] = var10003;
      CUBE_EDGES = var10000;
   }
   public static TargetESPModule INSTANCE = new TargetESPModule();

}
