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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class TotemPopModule extends Module {
   public static TotemPopModule INSTANCE = new TotemPopModule();
   public final ModeSetting mode;
   public final BooleanSetting visuals;
   public final BooleanSetting chatInfo;
   public final SliderSetting riseHeight;
   public final SliderSetting duration;
   public final MultiBooleanSetting renderModes;
   public static final float WING_SCALE = 1.0F;
   public static final float FLAP_SPEED = 1.6F;
   public static final float FLAP_AMPLITUDE = 25.0F;
   public static final float GLOW_INTENSITY = 0.1F;
   public static final float HALO_SIZE = 0.4F;
   private static final Identifier SPARKLE_TEXTURE = Identifier.of("obsidian", "textures/particle/sparkle.png");
   public static final int GREEN_COLOR = -13238485;
   public static final int YELLOW_COLOR = -3797;
   public final List<ObsidianComponent_229> ghosts;
   public final List<ObsidianComponent_66> sphereEffects;
   public final Map<Integer, Long> recentSphereSpawns;

   public TotemPopModule() {
      super("TotemPop", "Отображает эффект и пишет в чат при срабатывании тотема", Category.RENDER);
      String[] var10005 = new String[1];
      var10005[0] = "Angel";
      this.mode = new ModeSetting("Режим", "Angel", var10005);
      this.visuals = new BooleanSetting("Визуал", true);
      this.chatInfo = new BooleanSetting("Чат инфо", true);
      this.riseHeight = new SliderSetting(
         "Высота",
         4.0f,
         0.20000000298023224f,
         10.0f,
         0.10000000149011612f
      );
      this.duration = new SliderSetting(
         "Длительность",
         3.0f,
         0.20000000298023224f,
         6.0f,
         0.10000000149011612f
      );
      BooleanSetting[] var10004 = new BooleanSetting[1];
      var10004[0] = new BooleanSetting("Ангел", true);
      this.renderModes = new MultiBooleanSetting("Режим", var10004);
      this.ghosts = new CopyOnWriteArrayList<>();
      this.sphereEffects = new CopyOnWriteArrayList<>();
      this.recentSphereSpawns = new ConcurrentHashMap<>();
      Setting[] var10001 = new Setting[6];
      var10001[0] = this.renderModes;
      var10001[1] = this.mode.visible(() -> Boolean.valueOf(false));
      var10001[2] = this.visuals.visible(() -> Boolean.valueOf(false));
      var10001[3] = this.chatInfo;
      var10001[4] = this.riseHeight;
      var10001[5] = this.duration;
      this.addSettings(var10001);
   }

   @Override
   public void onDisable() {
      this.ghosts.clear();
      super.onDisable();
   }
   public Identifier getGlowTexture() {
      return Identifier.of("obsidian", "textures/targetesp/bloom.png");
   }
   public Identifier getSkinTexture() {
      return Identifier.of("obsidian", "textures/skin/skin.png");
   }

   @EventTarget
   public void onPacket(PacketEvent event) {
      if (mc.world != null && mc.player != null && event.getType() == PacketType.RECEIVE) {
         if (event.getPacket() instanceof EntityStatusS2CPacket packet && packet.getStatus() == (35)) {
            mc.execute(() -> this.handleTotemPopPacket(packet));
         }
      }
   }
   public void handleTotemPopPacket(EntityStatusS2CPacket packet) {
      if (mc.world != null && mc.player != null) {
         if (packet.getEntity(mc.world) instanceof AbstractClientPlayerEntity player) {
            if (this.renderModes.is("Ангел")) {
               this.addGhost(player);
            }

            if (this.chatInfo.isState() && player != mc.player) {
               String name = player.getName().getString();
               ObsidianComponent_hatUtil.sendMessage(name + " §7снёс тотем!");
            }
         }
      }
   }

   @EventTarget
   public void onRender3D(RenderEvent event) {
      if (mc.world != null && mc.player != null) {
         if (this.renderModes.is("Ангел") && !this.ghosts.isEmpty()) {
            this.renderGhosts(event.getMatrices(), event.getTickDelta());
         }
      }
   }
   public void addGhost(AbstractClientPlayerEntity player) {
      float partialTicks = mc.getRenderTickCounter().getTickDelta(true);
      double x = MathHelper.lerp((double)partialTicks, player.lastRenderX, player.getX());
      double y = MathHelper.lerp((double)partialTicks, player.lastRenderY, player.getY());
      double z = MathHelper.lerp((double)partialTicks, player.lastRenderZ, player.getZ());
      float bodyYaw = MathHelper.lerp(partialTicks, player.prevBodyYaw, player.bodyYaw);
      float headYaw = MathHelper.lerp(partialTicks, player.prevHeadYaw, player.headYaw);
      float headPitch = MathHelper.lerp(partialTicks, player.prevPitch, player.getPitch());
      float limbSwing = player.limbAnimator.getPos(partialTicks);
      float limbSwingAmount = player.limbAnimator.getSpeed(partialTicks);
      boolean sneaking = player.isSneaking();
      float height = player.getHeight();
      this.ghosts
         .add(new ObsidianComponent_229(new Vec3d(x, y, z), bodyYaw, headYaw - bodyYaw, headPitch, limbSwing, limbSwingAmount, sneaking, height, System.currentTimeMillis()));
   }
   public void addSphereEffect(AbstractClientPlayerEntity player) {
      if (player != null && player != mc.player) {
         long now = System.currentTimeMillis();
         this.recentSphereSpawns
            .entrySet()
            .removeIf(
               entry -> (now - entry.getValue() > (1000L))
            );
         Long lastSpawn = this.recentSphereSpawns.get(player.getId());
         if (lastSpawn == null || now - lastSpawn >= 120L) {
            this.recentSphereSpawns.put(player.getId(), now);
            double centerY = player.getY()
               + (double)player.getHeight() * 0.62;
            List<ObsidianComponent_281> particles = new ArrayList<>(64);
            ThreadLocalRandom random = ThreadLocalRandom.current();

            for (int i = 0; i < (64); i++) {
               double yaw = random.nextDouble(
                  0.0,
                  6.283185307179586
               );
               double pitch = random.nextDouble(
                  Double.longBitsToDouble(-4617991057905706598L),
                  0.8
               );
               Vec3d direction = new Vec3d(
                     Math.cos(yaw) * Math.cos(pitch),
                     Math.sin(pitch) * 0.62
                        + random.nextDouble(
                           Double.longBitsToDouble(-4630060704907059528L),
                           0.24
                        ),
                     Math.sin(yaw) * Math.cos(pitch)
                  )
                  .normalize();
               particles.add(
                  new ObsidianComponent_281(
                     direction,
                     random.nextFloat(0.2800000011920929f, 1.0800000429153442f),
                     random.nextFloat(0.85F, 1.5499999523162842f),
                     random.nextFloat(1.0499999523162842f, 1.850000023841858f),
                     random.nextFloat(0.949999988079071f, 1.4500000476837158f),
                     random.nextFloat(0.0f, 1.0f),
                     random.nextBoolean() ? -13238485 : -3797
                  )
               );
            }

            this.sphereEffects
               .add(
                  new ObsidianComponent_66(
                     new Vec3d(player.getX(), centerY, player.getZ()),
                     now,
                     random.nextFloat(0.0f, 360.0f),
                     particles,
                     this.createSphereOrbitLines()
                  )
               );
         }
      }
   }
   public void renderGhosts(MatrixStack matrices, float tickDelta) {
      Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
      long now = System.currentTimeMillis();
      List<ObsidianComponent_229> toRemove = new ArrayList<>();
      int themeColor = ObsidianComponent_olorUtil.getThemeColor();
      float r = ObsidianComponent_olorUtil.redf(themeColor);
      float g = ObsidianComponent_olorUtil.greenf(themeColor);
      float b = ObsidianComponent_olorUtil.bluef(themeColor);

      for (ObsidianComponent_229 ghost : this.ghosts) {
         float progress = (float)(now - ghost.startTime) / (this.duration.get() * 1000.0f);
         if (progress >= 1.0f) {
            toRemove.add(ghost);
         } else {
            double motionY = (double)(this.riseHeight.get() * this.easeOutCubic(progress));
            float alpha = (1.0f - this.easeInCubic(progress))
               * 0.8500000238418579f;
            double renderX = ghost.position.x - cameraPos.x;
            double renderY = ghost.position.y - cameraPos.y + motionY;
            double renderZ = ghost.position.z - cameraPos.z;
            matrices.push();
            matrices.translate(renderX, renderY, renderZ);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-ghost.bodyYaw));
            this.renderGlowingPlayerModel(matrices, r, g, b, alpha, ghost);
            this.renderWings(matrices, ghost, progress, tickDelta, themeColor, alpha);
            this.renderHalo(matrices, ghost, themeColor, alpha);
            matrices.pop();
         }
      }

      if (!toRemove.isEmpty()) {
         this.ghosts.removeAll(toRemove);
      }
   }
   public void renderSphereEffects(MatrixStack matrices, Vec3d cameraPos) {
      long now = System.currentTimeMillis();
      float sphereDurationMs = this.duration.get() * 1000.0f;
      this.sphereEffects
         .removeIf(effect -> ((float)(now - effect.startTime) >= sphereDurationMs));
      if (!this.sphereEffects.isEmpty()) {
         RenderSystem.enableBlend();
         RenderSystem.disableCull();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         this.renderSphereParticles(matrices, cameraPos, now, sphereDurationMs);
         this.renderSphereArcs(matrices, cameraPos, now, sphereDurationMs);
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(true);
         RenderSystem.enableCull();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
      }
   }
   public void renderSphereParticles(MatrixStack matrices, Vec3d cameraPos, long now, float durationMs) {
      RenderSystem.blendFunc(770, 1);
      RenderSystem.setShaderTexture(0, SPARKLE_TEXTURE);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
      float cameraYaw = mc.gameRenderer.getCamera().getYaw();
      float cameraPitch = mc.gameRenderer.getCamera().getPitch();
      float baseRadius = 1.1799999475479126f;
      float baseSize = 0.2800000011920929f;

      for (ObsidianComponent_66 effect : this.sphereEffects) {
         float age = (float)(now - effect.startTime) / durationMs;
         float appear = MathHelper.clamp(
            1.0f - age,
            0.0f,
            1.0f
         );
         float burstProgress = this.easeOutQuad(
            Math.min(1.0f, age * 1.1200000047683716f)
         );

         for (ObsidianComponent_281 particle : effect.particles) {
            float localProgress = MathHelper.clamp(
               age * particle.timeScale + particle.progressOffset * 0.10000000149011612f,
               0.0F,
               1.0f
            );
            float launchProgress = this.easeOutQuad(localProgress);
            float radial = (
                  0.3400000035762787f
                     + launchProgress
                        * (1.2000000476837158f + particle.spread * 1.0499999523162842f)
                     + burstProgress * 0.3199999928474426f
               )
               * baseRadius;
            float orbit = (float)now * 0.0012000000569969416f * particle.rotationScale
               + particle.progressOffset * 5.400000095367432f;
            double swirlScale = (double)(
               (1.0f - localProgress) * 0.18000000715255737f
            );
            double swirlX = Math.cos((double)orbit) * swirlScale * (double)particle.swirlAmount;
            double swirlY = Math.sin((double)(orbit * 1.2999999523162842f))
                  * swirlScale
                  * 0.75
                  * (double)particle.swirlAmount
               + (double)(localProgress * 0.07999999821186066f);
            double swirlZ = Math.sin((double)orbit) * swirlScale * (double)particle.swirlAmount;
            double dragY = (double)(localProgress * localProgress * 0.14000000059604645f);
            Vec3d worldPos = effect.origin.add(particle.direction.multiply((double)radial)).add(swirlX, swirlY - dragY, swirlZ);
            double x = worldPos.x - cameraPos.x;
            double y = worldPos.y - cameraPos.y;
            double z = worldPos.z - cameraPos.z;
            int color = this.setAlpha(
               particle.color,
               (int)(
                  255.0f
                     * appear
                     * (
                        0.5f
                           + 0.5f
                              * (1.0f - localProgress)
                     )
               )
            );
            float drawSize = baseSize
               * (
                  0.6800000071525574f
                     + particle.spread * 0.3400000035762787f
               )
               * (0.699999988079071f + appear * 0.5199999809265137f);
            matrices.push();
            matrices.translate(x, y, z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-cameraYaw));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cameraPitch));
            this.drawSphereBillboard(matrices.peek().getPositionMatrix(), drawSize, color);
            matrices.pop();
         }
      }
   }
   public void renderSphereArcs(MatrixStack matrices, Vec3d cameraPos, long now, float durationMs) {
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      RenderSystem.lineWidth(1.0499999523162842f);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);

      for (ObsidianComponent_66 effect : this.sphereEffects) {
         float age = (float)(now - effect.startTime) / durationMs;
         float appear = MathHelper.clamp(
            1.0f - age, 0.0f, 1.0F
         );
         float grow = this.easeOutQuad(
            Math.min(1.0f, age * 1.25f)
         );
         float elapsedSec = (float)(now - effect.startTime) / 1000.0f;
         float scale = 1.1799999475479126f
            * (0.7799999713897705f + grow * 0.10000000149011612f);
         double x = effect.origin.x - cameraPos.x;
         double y = effect.origin.y - cameraPos.y;
         double z = effect.origin.z - cameraPos.z;

         for (ObsidianComponent_124 line : effect.orbitLines) {
            matrices.push();
            matrices.translate(x, y, z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(effect.baseRotation + line.baseYaw + elapsedSec * line.speedDeg));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(line.tiltX));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(line.tiltZ));
            this.drawSphereOrbitArc(
               matrices,
               line.radiusX * scale,
               line.radiusZ * scale,
               line.yOffset,
               line.startDeg,
               line.arcDeg,
               appear * line.alphaMul,
               line.startColor,
               line.endColor
            );
            matrices.pop();
         }
      }

      GL11.glDisable(2848);
   }
   public void drawSphereOrbitArc(
      MatrixStack matrices, float radiusX, float radiusZ, float y, float startDeg, float arcDeg, float alphaMul, int startColor, int endColor
   ) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      int segments = 28;
      float from = (float)Math.toRadians((double)startDeg);
      float to = (float)Math.toRadians((double)(startDeg + arcDeg));
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

      for (int i = 0; i <= segments; i++) {
         float progress = (float)i / (float)segments;
         float angle = MathHelper.lerp(progress, from, to);
         float px = MathHelper.cos(angle) * radiusX;
         float pz = MathHelper.sin(angle) * radiusZ;
         float localY = y
            + MathHelper.sin(angle * 1.350000023841858f)
               * 0.009999999776482582f;
         float edgeFade = MathHelper.clamp(
            1.0f
               - Math.abs(progress - 0.5f)
                  * 2.0f,
            0.0f,
            1.0f
         );
         int color = this.fadeLerp(
            startColor,
            endColor,
            progress,
            alphaMul * (0.2199999988079071f + 0.7799999713897705f * edgeFade)
         );
         buffer.vertex(matrix, px, localY, pz).color(color);
      }

      BufferRenderer.drawWithGlobalProgram(buffer.end());
      BufferBuilder echo = Tessellator.getInstance().begin(DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

      for (int i = 0; i <= segments; i++) {
         float progress = (float)i / (float)segments;
         float angle = MathHelper.lerp(
            progress, from + 0.014999999664723873f, to - 0.012000000104308128f
         );
         float px = MathHelper.cos(angle) * (radiusX + 0.012000000104308128f);
         float pz = MathHelper.sin(angle) * (radiusZ + 0.012000000104308128f);
         float localY = y
            + 0.004000000189989805f
            + MathHelper.sin(angle * 1.350000023841858f + 0.8999999761581421f)
               * 0.00800000037997961f;
         float edgeFade = MathHelper.clamp(
            1.0f
               - Math.abs(progress - 0.5f) * 2.0f,
            0.0f,
            1.0f
         );
         int color = this.fadeLerp(startColor, endColor, progress, alphaMul * 0.14000000059604645f * edgeFade);
         echo.vertex(matrix, px, localY, pz).color(color);
      }

      BufferRenderer.drawWithGlobalProgram(echo.end());
   }
   public List<ObsidianComponent_124> createSphereOrbitLines() {
      List<ObsidianComponent_124> lines = new ArrayList<>(5);
      lines.add(
         new ObsidianComponent_124(
            1.0199999809265137f,
            0.6600000262260437f,
            0.20000000298023224f,
            196.0f,
            156.0f,
            14.0f,
            -12.0f,
            54.0f,
            0.46000000834465027f,
            -13238485,
            -13238485
         )
      );
      lines.add(
         new ObsidianComponent_124(
            0.9200000166893005f,
            0.6000000238418579f,
            0.1599999964237213f,
            188.0f,
            148.0f,
            14.0f,
            -12.0f,
            54.0f,
            0.2199999988079071f,
            -13238485,
            -13238485
         )
      );
      lines.add(
         new ObsidianComponent_124(
            0.8600000143051147f,
            0.5400000214576721f,
            -0.11999999731779099f,
            122.0f,
            112.0f,
            78.0f,
            4.0f,
            -68.0f,
            0.6499999761581421f,
            -3797,
            -3797
         )
      );
      lines.add(
         new ObsidianComponent_124(
            0.7400000095367432f,
            0.46000000834465027f,
            -0.019999999552965164f,
            314.0f,
            88.0f,
            62.0f,
            -18.0f,
            76.0f,
            0.5799999833106995f,
            -13238485,
            -3797
         )
      );
      lines.add(
         new ObsidianComponent_124(
            0.6800000071525574f,
            0.3400000035762787f,
            0.0f,
            202.0f,
            44.0f,
            8.0f,
            52.0f,
            -44.0f,
            0.18000000715255737f,
            -13238485,
            -13238485
         )
      );
      return lines;
   }
   public void drawSphereBillboard(Matrix4f matrix, float size, int color) {
      float half = size * 0.5f;
      int r = color >> (16) & 255;
      int g = color >> 8 & (255);
      int b = color & 255;
      int a = color >> (24) & (255);
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
   }
   public int fadeLerp(int start, int end, float progress, float alphaMul) {
      int sr = start >> (16) & (255);
      int sg = start >> 8 & (255);
      int sb = start & 255;
      int er = end >> 16 & (255);
      int eg = end >> (8) & 255;
      int eb = end & 255;
      int r = MathHelper.lerp(progress, sr, er);
      int g = MathHelper.lerp(progress, sg, eg);
      int b = MathHelper.lerp(progress, sb, eb);
      int a = MathHelper.clamp(
         (int)(255.0f * alphaMul), 0, 255
      );
      return a << (24) | r << 16 | g << 8 | b;
   }
   public int setAlpha(int color, int alpha) {
      return MathHelper.clamp(alpha, 0, 255) << (24)
         | color & (16777215);
   }
   public float easeOutQuad(float value) {
      float inv = 1.0f - value;
      return 1.0f - inv * inv;
   }
   public void renderSkinPlayerModel(MatrixStack matrices, float alpha, ObsidianComponent_229 ghost) {
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(770, 771);
      RenderSystem.enableDepthTest();
      RenderSystem.depthMask(true);
      RenderSystem.setShaderTexture(0, this.getSkinTexture());
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
      float unit = 0.0625f;
      float sneakOffset = ghost.sneaking
         ? 0.25f
         : 0.0f;
      float limbSwing = ghost.limbSwing;
      float limbSwingAmount = Math.min(1.0f, ghost.limbSwingAmount);
      float legSwing = MathHelper.cos(limbSwing * 0.6661999821662903f)
         * 1.399999976158142f
         * limbSwingAmount;
      float armSwing = MathHelper.cos(
            limbSwing * 0.6661999821662903f + 3.1415927410125732f
         )
         * 2.0f
         * limbSwingAmount;
      int alphaInt = (int)(alpha * 255.0f);
      matrices.push();
      matrices.translate(
         0.0f,
         24.0f * unit - sneakOffset,
         0.0f
      );
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(ghost.netHeadYaw));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(ghost.headPitch));
      this.renderSkinBox(
         matrices,
         -4.0f * unit,
         -8.0f * unit,
         -4.0f * unit,
         8.0f * unit,
         8.0f * unit,
         8.0f * unit,
         8,
         8,
         16,
         16,
         64,
         64,
         alphaInt
      );
      matrices.pop();
      matrices.push();
      if (ghost.sneaking) {
         matrices.translate(
            0.0f,
            12.0f * unit,
            0.0f
         );
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(28.0f));
         matrices.translate(
            0.0f,
            -12.0f * unit,
            0.0f
         );
      }

      this.renderSkinBox(
         matrices,
         -4.0f * unit,
         12.0f * unit - sneakOffset,
         -2.0f * unit,
         8.0f * unit,
         12.0f * unit,
         4.0f * unit,
         20,
         20,
         28,
         32,
         64,
         64,
         alphaInt
      );
      matrices.pop();
      matrices.push();
      matrices.translate(
         -5.0f * unit,
         22.0f * unit - sneakOffset,
         0.0f
      );
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(armSwing * 57.2957763671875f));
      this.renderSkinBox(
         matrices,
         -2.0f * unit,
         -12.0f * unit,
         -2.0f * unit,
         4.0f * unit,
         12.0f * unit,
         4.0f * unit,
         44,
         20,
         48,
         32,
         64,
         64,
         alphaInt
      );
      matrices.pop();
      matrices.push();
      matrices.translate(
         5.0f * unit,
         22.0f * unit - sneakOffset,
         0.0f
      );
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-armSwing * 57.2957763671875f));
      this.renderSkinBox(
         matrices,
         -2.0f * unit,
         -12.0f * unit,
         -2.0f * unit,
         4.0f * unit,
         12.0f * unit,
         4.0f * unit,
         36,
         52,
         40,
         64,
         64,
         64,
         alphaInt
      );
      matrices.pop();
      matrices.push();
      matrices.translate(
         -2.0f * unit,
         12.0f * unit - sneakOffset,
         0.0f
      );
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(legSwing * 57.2957763671875f));
      this.renderSkinBox(
         matrices,
         -2.0f * unit,
         -12.0f * unit,
         -2.0f * unit,
         4.0F * unit,
         12.0f * unit,
         4.0f * unit,
         4,
         20,
         8,
         32,
         64,
         64,
         alphaInt
      );
      matrices.pop();
      matrices.push();
      matrices.translate(
         2.0F * unit, 12.0f * unit - sneakOffset, 0.0f
      );
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-legSwing * 57.2957763671875f));
      this.renderSkinBox(
         matrices,
         -2.0f * unit,
         -12.0f * unit,
         -2.0f * unit,
         4.0f * unit,
         12.0f * unit,
         4.0f * unit,
         20,
         52,
         24,
         64,
         64,
         64,
         alphaInt
      );
      matrices.pop();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
   }
   public void renderSkinBox(
      MatrixStack matrices,
      float x,
      float y,
      float z,
      float width,
      float height,
      float depth,
      int u,
      int v,
      int u2,
      int v2,
      int texWidth,
      int texHeight,
      int alpha
   ) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      float x2 = x + width;
      float y2 = y + height;
      float z2 = z + depth;
      float w = width * 16.0f;
      float h = height * 16.0f;
      float d = depth * 16.0f;
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      float uMin = (float)u / (float)texWidth;
      float vMin = (float)v / (float)texHeight;
      float uMax = (float)u2 / (float)texWidth;
      float vMax = (float)v2 / (float)texHeight;
      float frontU1 = ((float)u + d) / (float)texWidth;
      float frontU2 = ((float)u + d + w) / (float)texWidth;
      float frontV1 = ((float)v + d) / (float)texHeight;
      float frontV2 = ((float)v + d + h) / (float)texHeight;
      buffer.vertex(matrix, x, y, z2)
         .texture(frontU1, frontV2)
         .color(255, 255, 255, alpha);
      buffer.vertex(matrix, x2, y, z2).texture(frontU2, frontV2).color(255, 255, 255, alpha);
      buffer.vertex(matrix, x2, y2, z2)
         .texture(frontU2, frontV1)
         .color(255, 255, 255, alpha);
      buffer.vertex(matrix, x, y2, z2)
         .texture(frontU1, frontV1)
         .color(255, 255, 255, alpha);
      float backU1 = ((float)u + d + w + d) / (float)texWidth;
      float backU2 = ((float)u + d + w + d + w) / (float)texWidth;
      float backV1 = ((float)v + d) / (float)texHeight;
      float backV2 = ((float)v + d + h) / (float)texHeight;
      buffer.vertex(matrix, x2, y, z)
         .texture(backU1, backV2)
         .color(255, 255, 255, alpha);
      buffer.vertex(matrix, x, y, z)
         .texture(backU2, backV2)
         .color(255, 255, 255, alpha);
      buffer.vertex(matrix, x, y2, z).texture(backU2, backV1).color(255, 255, 255, alpha);
      buffer.vertex(matrix, x2, y2, z)
         .texture(backU1, backV1)
         .color(255, 255, 255, alpha);
      float topU1 = ((float)u + d) / (float)texWidth;
      float topU2 = ((float)u + d + w) / (float)texWidth;
      float topV1 = (float)v / (float)texHeight;
      float topV2 = ((float)v + d) / (float)texHeight;
      buffer.vertex(matrix, x, y2, z)
         .texture(topU1, topV1)
         .color(255, 255, 255, alpha);
      buffer.vertex(matrix, x, y2, z2)
         .texture(topU1, topV2)
         .color(255, 255, 255, alpha);
      buffer.vertex(matrix, x2, y2, z2)
         .texture(topU2, topV2)
         .color(255, 255, 255, alpha);
      buffer.vertex(matrix, x2, y2, z)
         .texture(topU2, topV1)
         .color(255, 255, 255, alpha);
      float bottomU1 = ((float)u + d + w) / (float)texWidth;
      float bottomU2 = ((float)u + d + w + w) / (float)texWidth;
      float bottomV1 = (float)v / (float)texHeight;
      float bottomV2 = ((float)v + d) / (float)texHeight;
      buffer.vertex(matrix, x, y, z2)
         .texture(bottomU1, bottomV1)
         .color(255, 255, 255, alpha);
      buffer.vertex(matrix, x, y, z)
         .texture(bottomU1, bottomV2)
         .color(255, 255, 255, alpha);
      buffer.vertex(matrix, x2, y, z)
         .texture(bottomU2, bottomV2)
         .color(255, 255, 255, alpha);
      buffer.vertex(matrix, x2, y, z2)
         .texture(bottomU2, bottomV1)
         .color(255, 255, 255, alpha);
      float rightU1 = (float)u / (float)texWidth;
      float rightU2 = ((float)u + d) / (float)texWidth;
      float rightV1 = ((float)v + d) / (float)texHeight;
      float rightV2 = ((float)v + d + h) / (float)texHeight;
      buffer.vertex(matrix, x, y, z)
         .texture(rightU1, rightV2)
         .color(255, 255, 255, alpha);
      buffer.vertex(matrix, x, y, z2).texture(rightU2, rightV2).color(255, 255, 255, alpha);
      buffer.vertex(matrix, x, y2, z2)
         .texture(rightU2, rightV1)
         .color(255, 255, 255, alpha);
      buffer.vertex(matrix, x, y2, z)
         .texture(rightU1, rightV1)
         .color(255, 255, 255, alpha);
      float leftU1 = ((float)u + d + w) / (float)texWidth;
      float leftU2 = ((float)u + d + w + d) / (float)texWidth;
      float leftV1 = ((float)v + d) / (float)texHeight;
      float leftV2 = ((float)v + d + h) / (float)texHeight;
      buffer.vertex(matrix, x2, y, z2)
         .texture(leftU1, leftV2)
         .color(255, 255, 255, alpha);
      buffer.vertex(matrix, x2, y, z)
         .texture(leftU2, leftV2)
         .color(255, 255, 255, alpha);
      buffer.vertex(matrix, x2, y2, z)
         .texture(leftU2, leftV1)
         .color(255, 255, 255, alpha);
      buffer.vertex(matrix, x2, y2, z2)
         .texture(leftU1, leftV1)
         .color(255, 255, 255, alpha);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
   }
   public void renderWings(MatrixStack matrices, ObsidianComponent_229 ghost, float progress, float tickDelta, int themeColor, float alpha) {
      float anim = (float)System.currentTimeMillis()
            / 50.0f
            * 0.2199999988079071f
            * 1.600000023841858f
         + progress * 2.0f;
      float sin = MathHelper.sin(anim);
      float cos = MathHelper.cos(anim);
      float spreadAngle = 18.0f + progress * 15.0f;
      float pitchAngle = 13.0f + cos * 4.0f;
      float rollAngle = sin * 25.0f;
      if (ghost.sneaking) {
         spreadAngle -= 3.0f;
         pitchAngle += 8.0f;
      }

      int topColor = ObsidianComponent_olorUtil.setAlphaColor(themeColor, (int)(132.0f * alpha));
      int bottomColor = ObsidianComponent_olorUtil.setAlphaColor(
         ObsidianComponent_olorUtil.darken(themeColor, 0.8500000238418579f),
         (int)(102.0f * alpha)
      );
      int edgeColor = ObsidianComponent_olorUtil.setAlphaColor(
         ObsidianComponent_olorUtil.darken(themeColor, 0.699999988079071f), (int)(190.0f * alpha)
      );
      int boneColorA = ObsidianComponent_olorUtil.setAlphaColor(
         ObsidianComponent_olorUtil.darken(themeColor, 0.5199999809265137f),
         (int)(175.0f * alpha)
      );
      int boneColorB = ObsidianComponent_olorUtil.setAlphaColor(
         ObsidianComponent_olorUtil.darken(themeColor, 0.5799999833106995f), (int)(150.0f * alpha)
      );
      RenderSystem.enableBlend();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(true);
      RenderSystem.blendFunc(770, 771);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      matrices.push();
      float sneakOffset = ghost.sneaking
         ? 0.25f
         : 0.0f;
      matrices.translate(
         0.0f,
         1.2999999523162842f - sneakOffset,
         -0.07999999821186066f
      );
      if (ghost.sneaking) {
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(24.0f));
      }

      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      this.renderButterflyWing(
         buffer,
         matrices,
         1.0f,
         spreadAngle,
         pitchAngle,
         rollAngle,
         1.0f,
         topColor,
         bottomColor,
         edgeColor,
         boneColorA,
         boneColorB
      );
      this.renderButterflyWing(
         buffer,
         matrices,
         -1.0f,
         spreadAngle,
         pitchAngle,
         rollAngle,
         1.0f,
         topColor,
         bottomColor,
         edgeColor,
         boneColorA,
         boneColorB
      );
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.blendFunc(770, 1);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      int glowA = ObsidianComponent_olorUtil.setAlphaColor(themeColor, (int)(72.0f * alpha));
      int glowB = ObsidianComponent_olorUtil.setAlphaColor(
         ObsidianComponent_olorUtil.darken(themeColor, 0.8199999928474426f),
         (int)(66.0f * alpha)
      );
      BufferBuilder glowBuffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      this.renderButterflyGlow(
         glowBuffer,
         matrices,
         1.0f,
         spreadAngle,
         pitchAngle,
         rollAngle,
         1.0f,
         glowA,
         glowB
      );
      this.renderButterflyGlow(
         glowBuffer,
         matrices,
         -1.0f,
         spreadAngle,
         pitchAngle,
         rollAngle,
         1.0f,
         glowA,
         glowB
      );
      BufferRenderer.drawWithGlobalProgram(glowBuffer.end());
      matrices.pop();
      RenderSystem.enableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
      RenderSystem.depthMask(true);
   }
   public void renderButterflyWing(
      BufferBuilder buffer,
      MatrixStack matrices,
      float side,
      float spread,
      float pitch,
      float roll,
      float scale,
      int topColor,
      int bottomColor,
      int edgeColor,
      int boneColorA,
      int boneColorB
   ) {
      float root = 0.11999999731779099f * scale;
      float topW = 1.5f * scale;
      float topH = 0.6100000143051147f * scale;
      float lowW = 1.100000023841858f * scale;
      float lowH = 0.3499999940395355f * scale;
      matrices.push();
      matrices.translate(
         0.15000000596046448f * side,
         0.0f,
         -0.17000000178813934f
      );
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * spread));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side * roll));
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      this.addDoubleSidedGradientQuad(
         buffer,
         matrix,
         side * root,
         0.019999999552965164f,
         0.0f,
         side * (root + topW * 0.18000000715255737f),
         topH * 0.949999988079071f,
         -0.05999999865889549f,
         side * (root + topW),
         topH * 0.30000001192092896f,
         -0.12999999523162842f,
         side * (root + topW * 0.20000000298023224f),
         0.05999999865889549f,
         -0.029999999329447746f,
         topColor,
         bottomColor
      );
      this.addDoubleSidedGradientQuad(
         buffer,
         matrix,
         side * root,
         -0.009999999776482582f,
         -0.019999999552965164f,
         side * (root + lowW * 0.18000000715255737f),
         -lowH * 0.9399999976158142f,
         -0.10000000149011612f,
         side * (root + lowW),
         -lowH * 0.36000001430511475f,
         -0.17000000178813934f,
         side * (root + lowW * 0.6000000238418579f),
         -0.10000000149011612f,
         -0.07000000029802322f,
         bottomColor,
         topColor
      );
      this.addDoubleSidedQuad(
         buffer,
         matrix,
         side * root,
         0.012000000104308128f,
         0.009999999776482582f,
         side * root,
         -0.03200000151991844f,
         -0.009999999776482582f,
         side * (root + topW * 0.5600000023841858f),
         -0.00800000037997961f,
         -0.07999999821186066f,
         side * (root + topW * 0.5600000023841858f),
         0.00800000037997961f,
         -0.03999999910593033f,
         edgeColor >> (16) & 0xFF,
         edgeColor >> 8 & 255,
         edgeColor & 255,
         edgeColor >> 24 & 255
      );
      this.renderWingBoneLine(
         buffer,
         matrix,
         side * root,
         0.0f,
         -0.019999999552965164f,
         side * (root + topW * 0.2199999988079071f),
         topH * 0.8199999928474426f,
         -0.07000000029802322f,
         side * (root + topW),
         topH * 0.30000001192092896f,
         -0.12999999523162842f,
         0.01600000075995922f * scale,
         boneColorB,
         boneColorB
      );
      this.renderWingBoneLine(
         buffer,
         matrix,
         side * root,
         0.012000000104308128f,
         -0.008F,
         side * (root + topW * 0.36000001430511475f),
         topH * 0.5600000023841858f,
         -0.06499999761581421f,
         side * (root + topW * 0.8600000143051147f),
         topH * 0.25999999046325684f,
         -0.11500000208616257f,
         0.012000000104308128f * scale,
         boneColorA,
         boneColorB
      );
      this.renderWingBoneLine(
         buffer,
         matrix,
         side * root,
         -0.019999999552965164f,
         -0.03999999910593033f,
         side * (root + lowW * 0.2199999988079071f),
         -lowH * 0.8399999737739563f,
         -0.10999999940395355f,
         side * (root + lowW),
         -lowH * 0.3400000035762787f,
         -0.18000000715255737f,
         0.008999999612569809f * scale,
         boneColorB,
         boneColorB
      );
      this.renderWingBoneLine(
         buffer,
         matrix,
         side * root,
         -0.004000000189989805f,
         -0.017999999225139618f,
         side * (root + lowW * 0.3400000035762787f),
         -lowH * 0.5199999809265137f,
         -0.08500000089406967f,
         side * (root + lowW * 0.8799999952316284f),
         -lowH * 0.30000001192092896f,
         -0.14499999582767487f,
         0.009999999776482582f * scale,
         boneColorB,
         boneColorA
      );
      matrices.pop();
   }
   public void renderButterflyGlow(
      BufferBuilder buffer, MatrixStack matrices, float side, float spread, float pitch, float roll, float scale, int glowA, int glowB
   ) {
      float root = 0.11999999731779099f * scale;
      float topW = 1.5f * scale;
      float topH = 0.6100000143051147f * scale;
      float lowW = 1.100000023841858f * scale;
      float lowH = 0.3499999940395355f * scale;
      matrices.push();
      matrices.translate(
         0.15000000596046448f * side,
         0.0f,
         -0.17000000178813934f
      );
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * spread));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side * roll));
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      this.renderWingBoneLine(
         buffer,
         matrix,
         side * root,
         0.0f,
         -0.019999999552965164f,
         side * (root + topW * 0.20000000298023224f),
         topH * 0.8600000143051147f,
         -0.07999999821186066f,
         side * (root + topW),
         topH * 0.30000001192092896f,
         -0.1599999964237213f,
         0.019999999552965164f * scale,
         glowA,
         glowB
      );
      this.renderWingBoneLine(
         buffer,
         matrix,
         side * root,
         -0.019999999552965164f,
         -0.05000000074505806f,
         side * (root + lowW * 0.20000000298023224f),
         -lowH * 0.8600000143051147f,
         -0.12999999523162842f,
         side * (root + lowW),
         -lowH * 0.3199999928474426f,
         -0.20000000298023224f,
         0.017999999225139618f * scale,
         glowB,
         glowA
      );
      this.renderWingBoneLine(
         buffer,
         matrix,
         side * root,
         0.012000000104308128f,
         -0.00800000037997961f,
         side * (root + topW * 0.36000001430511475f),
         topH * 0.5600000023841858f,
         -0.07000000029802322f,
         side * (root + topW * 0.8399999737739563f),
         topH * 0.25f,
         -0.125f,
         0.01600000075995922f * scale,
         glowA,
         glowB
      );
      matrices.pop();
   }
   public void renderHalo(MatrixStack matrices, ObsidianComponent_229 ghost, int themeColor, float alpha) {
      float sneakOffset = ghost.sneaking ? 0.25f : 0.0f;
      float rotation = (float)System.currentTimeMillis() / 30.0F % 360.0f;
      matrices.push();
      matrices.translate(
         0.0f,
         1.899999976158142f - sneakOffset,
         0.0f
      );
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(15.0f));
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
      RenderSystem.enableBlend();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.blendFunc(770, 1);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      int haloColor = ObsidianComponent_olorUtil.setAlphaColor(themeColor, (int)(200.0f * alpha));
      int haloGlow = ObsidianComponent_olorUtil.setAlphaColor(themeColor, (int)(100.0f * alpha));
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      this.renderHaloRing(matrix, 0.4000000059604645f, 0.029999999329447746f, haloColor);
      this.renderHaloRing(
         matrix, 0.42000001668930054f, 0.05000000074505806f, haloGlow
      );
      this.renderHaloRing(
         matrix, 0.3799999952316284f, 0.019999999552965164f, haloGlow
      );
      RenderSystem.enableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
      matrices.pop();
   }
   public void renderHaloRing(Matrix4f matrix, float radius, float thickness, int color) {
      int segments = 36;
      float angleStep = (float)(6.283185307179586 / (double)segments);
      int r = color >> 16 & (255);
      int g = color >> 8 & 255;
      int b = color & 255;
      int a = color >> (24) & 255;
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);

      for (int i = 0; i < segments; i++) {
         float angle1 = (float)i * angleStep;
         float angle2 = (float)(i + 1) * angleStep;
         float x1Inner = MathHelper.cos(angle1) * (radius - thickness / 2.0f);
         float z1Inner = MathHelper.sin(angle1) * (radius - thickness / 2.0f);
         float x1Outer = MathHelper.cos(angle1) * (radius + thickness / 2.0f);
         float z1Outer = MathHelper.sin(angle1) * (radius + thickness / 2.0f);
         float x2Inner = MathHelper.cos(angle2) * (radius - thickness / 2.0f);
         float z2Inner = MathHelper.sin(angle2) * (radius - thickness / 2.0f);
         float x2Outer = MathHelper.cos(angle2) * (radius + thickness / 2.0f);
         float z2Outer = MathHelper.sin(angle2) * (radius + thickness / 2.0f);
         buffer.vertex(matrix, x1Inner, 0.009999999776482582f, z1Inner).color(r, g, b, a);
         buffer.vertex(matrix, x1Outer, 0.009999999776482582f, z1Outer).color(r, g, b, a);
         buffer.vertex(matrix, x2Outer, 0.009999999776482582f, z2Outer).color(r, g, b, a);
         buffer.vertex(matrix, x2Inner, 0.009999999776482582f, z2Inner).color(r, g, b, a);
         buffer.vertex(matrix, x1Inner, -0.009999999776482582f, z1Inner).color(r, g, b, a);
         buffer.vertex(matrix, x2Inner, -0.009999999776482582f, z2Inner).color(r, g, b, a);
         buffer.vertex(matrix, x2Outer, -0.01F, z2Outer).color(r, g, b, a);
         buffer.vertex(matrix, x1Outer, -0.009999999776482582f, z1Outer).color(r, g, b, a);
         buffer.vertex(matrix, x1Outer, -0.009999999776482582f, z1Outer).color(r, g, b, a);
         buffer.vertex(matrix, x2Outer, -0.009999999776482582f, z2Outer).color(r, g, b, a);
         buffer.vertex(matrix, x2Outer, 0.009999999776482582f, z2Outer).color(r, g, b, a);
         buffer.vertex(matrix, x1Outer, 0.009999999776482582f, z1Outer).color(r, g, b, a);
         buffer.vertex(matrix, x1Inner, 0.009999999776482582f, z1Inner).color(r, g, b, a);
         buffer.vertex(matrix, x2Inner, 0.009999999776482582f, z2Inner).color(r, g, b, a);
         buffer.vertex(matrix, x2Inner, -0.009999999776482582f, z2Inner).color(r, g, b, a);
         buffer.vertex(matrix, x1Inner, -0.009999999776482582f, z1Inner).color(r, g, b, a);
      }

      BufferRenderer.drawWithGlobalProgram(buffer.end());
   }
   public void addDoubleSidedQuad(
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
      float x4,
      float y4,
      float z4,
      int r,
      int g,
      int b,
      int a
   ) {
      this.addQuad(buffer, matrix, x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, r, g, b, a);
      this.addQuad(buffer, matrix, x4, y4, z4, x3, y3, z3, x2, y2, z2, x1, y1, z1, r, g, b, a);
   }
   public void addDoubleSidedGradientQuad(
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
      float x4,
      float y4,
      float z4,
      int nearColor,
      int farColor
   ) {
      int nr = nearColor >> 16 & 255;
      int ng = nearColor >> 8 & 255;
      int nb = nearColor & (255);
      int na = nearColor >> (24) & (255);
      int fr = farColor >> 16 & (255);
      int fg = farColor >> (8) & 255;
      int fb = farColor & (255);
      int fa = farColor >> 24 & 255;
      buffer.vertex(matrix, x1, y1, z1).color(nr, ng, nb, na);
      buffer.vertex(matrix, x2, y2, z2).color(fr, fg, fb, fa);
      buffer.vertex(matrix, x3, y3, z3).color(fr, fg, fb, fa);
      buffer.vertex(matrix, x4, y4, z4).color(nr, ng, nb, na);
      buffer.vertex(matrix, x4, y4, z4).color(nr, ng, nb, na);
      buffer.vertex(matrix, x3, y3, z3).color(fr, fg, fb, fa);
      buffer.vertex(matrix, x2, y2, z2).color(fr, fg, fb, fa);
      buffer.vertex(matrix, x1, y1, z1).color(nr, ng, nb, na);
   }
   public void renderWingBoneLine(
      BufferBuilder buffer,
      Matrix4f matrix,
      float x0,
      float y0,
      float z0,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float thickness,
      int colorA,
      int colorB
   ) {
      float vx1 = x1 - x0;
      float vy1 = y1 - y0;
      float len1 = Math.max(9.999999747378752e-05f, (float)Math.sqrt((double)(vx1 * vx1 + vy1 * vy1)));
      float nx1 = -vy1 / len1 * thickness;
      float ny1 = vx1 / len1 * thickness;
      int aR = colorA >> (16) & 255;
      int aG = colorA >> (8) & (255);
      int aB = colorA & (255);
      int aA = colorA >> 24 & (255);
      int bR = colorB >> (16) & (255);
      int bG = colorB >> 8 & (255);
      int bB = colorB & 255;
      int bA = colorB >> (24) & 255;
      this.addDoubleSidedQuad(buffer, matrix, x0 + nx1, y0 + ny1, z0, x0 - nx1, y0 - ny1, z0, x1 - nx1, y1 - ny1, z1, x1 + nx1, y1 + ny1, z1, aR, aG, aB, aA);
      float vx2 = x2 - x1;
      float vy2 = y2 - y1;
      float len2 = Math.max(9.999999747378752e-05f, (float)Math.sqrt((double)(vx2 * vx2 + vy2 * vy2)));
      float nx2 = -vy2 / len2 * thickness;
      float ny2 = vx2 / len2 * thickness;
      this.addDoubleSidedQuad(buffer, matrix, x1 + nx2, y1 + ny2, z1, x1 - nx2, y1 - ny2, z1, x2 - nx2, y2 - ny2, z2, x2 + nx2, y2 + ny2, z2, bR, bG, bB, bA);
   }
   public void addQuad(
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
      float x4,
      float y4,
      float z4,
      int r,
      int g,
      int b,
      int a
   ) {
      buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a);
      buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a);
      buffer.vertex(matrix, x3, y3, z3).color(r, g, b, a);
      buffer.vertex(matrix, x4, y4, z4).color(r, g, b, a);
   }
   public void renderGlowingPlayerModel(MatrixStack matrices, float r, float g, float b, float alpha, ObsidianComponent_229 ghost) {
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(770, 1);
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.disableCull();
      RenderSystem.setShaderTexture(0, this.getGlowTexture());
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
      float unit = 0.0625f;
      float sneakOffset = ghost.sneaking ? 0.25f : 0.0f;
      float limbSwing = ghost.limbSwing;
      float limbSwingAmount = Math.min(1.0f, ghost.limbSwingAmount);
      float legSwing = MathHelper.cos(limbSwing * 0.6661999821662903f)
         * 1.399999976158142f
         * limbSwingAmount;
      float armSwing = MathHelper.cos(
            limbSwing * 0.6661999821662903f + 3.1415927410125732f
         )
         * 1.399999976158142f
         * limbSwingAmount;
      matrices.push();
      matrices.translate(
         0.0f,
         24.0f * unit - sneakOffset,
         0.0f
      );
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(ghost.netHeadYaw));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(ghost.headPitch));
      this.renderGlowBox(
         matrices,
         -4.0f * unit,
         -8.0f * unit,
         -4.0f * unit,
         8.0f * unit,
         8.0f * unit,
         8.0f * unit,
         r,
         g,
         b,
         alpha * 0.10000000149011612f
      );
      matrices.pop();
      matrices.push();
      if (ghost.sneaking) {
         matrices.translate(
            0.0f,
            12.0f * unit,
            0.0f
         );
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(25.0F));
         matrices.translate(
            0.0f,
            -12.0f * unit,
            0.0f
         );
      }

      this.renderGlowBox(
         matrices,
         -4.0f * unit,
         12.0f * unit - sneakOffset,
         -2.0f * unit,
         8.0f * unit,
         12.0f * unit,
         4.0f * unit,
         r,
         g,
         b,
         alpha * 0.10000000149011612f
      );
      matrices.pop();
      float armWidth = 3.0f * unit;
      matrices.push();
      matrices.translate(
         -4.0f * unit - armWidth / 2.0f,
         22.0f * unit - sneakOffset,
         0.0f
      );
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(armSwing * 57.2957763671875f));
      this.renderGlowBox(
         matrices,
         -armWidth / 2.0f,
         -10.0f * unit,
         -2.0f * unit,
         armWidth,
         12.0f * unit,
         4.0f * unit,
         r,
         g,
         b,
         alpha * 0.10000000149011612f
      );
      matrices.pop();
      matrices.push();
      matrices.translate(
         4.0f * unit + armWidth / 2.0f,
         22.0f * unit - sneakOffset,
         0.0f
      );
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-armSwing * 57.2957763671875f));
      this.renderGlowBox(
         matrices,
         -armWidth / 2.0f,
         -10.0f * unit,
         -2.0f * unit,
         armWidth,
         12.0f * unit,
         4.0f * unit,
         r,
         g,
         b,
         alpha * 0.10000000149011612f
      );
      matrices.pop();
      matrices.push();
      matrices.translate(
         -2.0f * unit,
         12.0f * unit,
         0.0f
      );
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(legSwing * 57.2957763671875f));
      this.renderGlowBox(
         matrices,
         -2.0f * unit,
         -12.0f * unit,
         -2.0f * unit,
         4.0f * unit,
         12.0F * unit,
         4.0f * unit,
         r,
         g,
         b,
         alpha * 0.10000000149011612f
      );
      matrices.pop();
      matrices.push();
      matrices.translate(
         2.0f * unit, 12.0f * unit, 0.0F
      );
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-legSwing * 57.2957763671875f));
      this.renderGlowBox(
         matrices,
         -2.0f * unit,
         -12.0f * unit,
         -2.0f * unit,
         4.0f * unit,
         12.0F * unit,
         4.0f * unit,
         r,
         g,
         b,
         alpha * 0.10000000149011612f
      );
      matrices.pop();
      RenderSystem.enableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.depthMask(true);
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
   }
   public void renderGlowBox(MatrixStack matrices, float x, float y, float z, float width, float height, float depth, float r, float g, float b, float alpha) {
      float centerX = x + width / 2.0f;
      float centerY = y + height / 2.0f;
      float centerZ = z + depth / 2.0f;
      float glowSize = Math.max(width, Math.max(height, depth)) * 1.7999999523162842f;
      this.renderGlowSprite(
         matrices,
         centerX,
         centerY,
         centerZ
            + depth / 2.0f
            + 0.009999999776482582f,
         glowSize,
         width,
         height,
         r,
         g,
         b,
         alpha
      );
      this.renderGlowSprite(
         matrices,
         centerX,
         centerY,
         centerZ - depth / 2.0f - 0.009999999776482582f,
         glowSize,
         width,
         height,
         r,
         g,
         b,
         alpha
      );
      matrices.push();
      matrices.translate(centerX, centerY, centerZ);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0f));
      this.renderGlowSpriteRotated(
         matrices,
         0.0f,
         0.0f,
         depth / 2.0f + 0.009999999776482582f,
         glowSize,
         depth,
         height,
         r,
         g,
         b,
         alpha
      );
      this.renderGlowSpriteRotated(
         matrices,
         0.0f,
         0.0f,
         -depth / 2.0f - 0.009999999776482582f,
         glowSize,
         depth,
         height,
         r,
         g,
         b,
         alpha
      );
      matrices.pop();
      matrices.push();
      matrices.translate(centerX, centerY, centerZ);
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
      this.renderGlowSpriteRotated(
         matrices,
         0.0f,
         0.0F,
         height / 2.0F + 0.009999999776482582f,
         glowSize,
         width,
         depth,
         r,
         g,
         b,
         alpha
      );
      this.renderGlowSpriteRotated(
         matrices,
         0.0f,
         0.0f,
         -height / 2.0f - 0.009999999776482582f,
         glowSize,
         width,
         depth,
         r,
         g,
         b,
         alpha
      );
      matrices.pop();
      float innerAlpha = alpha * 0.4000000059604645f;
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      this.renderSolidBox(matrices, x, y, z, width, height, depth, r, g, b, innerAlpha);
      RenderSystem.setShaderTexture(0, this.getGlowTexture());
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
   }
   public void renderGlowSprite(
      MatrixStack matrices, float x, float y, float z, float glowSize, float boxWidth, float boxHeight, float r, float g, float b, float alpha
   ) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      int rInt = (int)(r * 255.0f);
      int gInt = (int)(g * 255.0f);
      int bInt = (int)(b * 255.0f);
      int aInt = (int)(
         MathHelper.clamp(
               alpha, 0.0f, 1.0f
            )
            * 255.0f
      );
      float halfW = glowSize / 2.0f;
      float halfH = glowSize / 2.0f;
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      buffer.vertex(matrix, x - halfW, y - halfH, z)
         .texture(0.0f, 0.0f)
         .color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x - halfW, y + halfH, z)
         .texture(0.0f, 1.0f)
         .color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x + halfW, y + halfH, z)
         .texture(1.0f, 1.0f)
         .color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x + halfW, y - halfH, z)
         .texture(1.0f, 0.0f)
         .color(rInt, gInt, bInt, aInt);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
   }
   public void renderGlowSpriteRotated(
      MatrixStack matrices, float x, float y, float z, float glowSize, float boxWidth, float boxHeight, float r, float g, float b, float alpha
   ) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      int rInt = (int)(r * 255.0f);
      int gInt = (int)(g * 255.0f);
      int bInt = (int)(b * 255.0f);
      int aInt = (int)(
         MathHelper.clamp(alpha, 0.0f, 1.0f)
            * 255.0f
      );
      float halfW = glowSize / 2.0f;
      float halfH = glowSize / 2.0f;
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      buffer.vertex(matrix, x - halfW, y - halfH, z)
         .texture(0.0f, 0.0f)
         .color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x - halfW, y + halfH, z)
         .texture(0.0f, 1.0f)
         .color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x + halfW, y + halfH, z)
         .texture(1.0f, 1.0f)
         .color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x + halfW, y - halfH, z)
         .texture(1.0f, 0.0f)
         .color(rInt, gInt, bInt, aInt);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
   }
   public void renderSolidBox(MatrixStack matrices, float x, float y, float z, float width, float height, float depth, float r, float g, float b, float alpha) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      float x2 = x + width;
      float y2 = y + height;
      float z2 = z + depth;
      int rInt = (int)(r * 255.0f);
      int gInt = (int)(g * 255.0f);
      int bInt = (int)(b * 255.0f);
      int aInt = (int)(
         MathHelper.clamp(alpha, 0.0f, 1.0f)
            * 255.0f
      );
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      buffer.vertex(matrix, x, y, z2).color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x2, y, z2).color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x2, y2, z2).color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x, y2, z2).color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x2, y, z).color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x, y, z).color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x, y2, z).color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x2, y2, z).color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x, y, z).color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x, y, z2).color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x, y2, z2).color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x, y2, z).color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x2, y, z2).color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x2, y, z).color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x2, y2, z).color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x2, y2, z2).color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x, y2, z).color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x, y2, z2).color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x2, y2, z2).color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x2, y2, z).color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x, y, z2).color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x, y, z).color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x2, y, z).color(rInt, gInt, bInt, aInt);
      buffer.vertex(matrix, x2, y, z2).color(rInt, gInt, bInt, aInt);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
   }
   public float easeOutCubic(float t) {
      return 1.0f
         - (float)Math.pow(
            1.0 - (double)t,
            3.0
         );
   }
   public float easeInCubic(float t) {
      return t * t * t;
   }
}
