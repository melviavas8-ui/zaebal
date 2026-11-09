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
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class CosmeticsModule extends Module {
   
   public static final float PI_STEP = 0.06981317F;
   public static final float WING_SCALE = 1.0F;
   public static final float FLAP_SPEED = 1.6F;
   public static final float FLAP_AMPLITUDE = 25.0F;
   public static final int NIMBUS_ARMS = 2;
   public static final int NIMBUS_SEGMENTS = 17;
   public static final float NIMBUS_RADIUS = 0.45F;
   public static final float NIMBUS_BASE_SIZE = 0.23F;
   public static final double NIMBUS_STEP_RADIANS = 0.11;
   public static final int NIMBUS_MAX_ALPHA = 255;
   public static final int NIMBUS_ALPHA_FALLOFF = 9;
   public static final float NIMBUS_SPEED = 170.0F;
   public static final float CLASSIC_WING_DEFAULT_SPREAD = 8.0F;
   public static final int CLASSIC_WING_DEFAULT_ALPHA = 220;
   public static final ObsidianComponent_433[] CLASSIC_WING_SHAPE;
   public final MultiBooleanSetting cosmetics;
   public final BooleanSetting butterflyWingAnimation;
   public final SliderSetting butterflyWingSize;
   public final BooleanSetting classicWingAnimation;
   public final SliderSetting classicWingSize;
   public float selfClassicBodyYaw;
   public boolean selfClassicBodyYawInitialized;
   public boolean lastButterflySelected;
   public boolean lastClassicSelected;

   public CosmeticsModule() {
      super("Cosmetics", "Визуальные украшения", Category.RENDER);
      BooleanSetting[] var10004 = new BooleanSetting[4];
      var10004[0] = new BooleanSetting("Нимб", true);
      var10004[1] = new BooleanSetting("Крылья", true);
      var10004[2] = new BooleanSetting("Крылья 2", false);
      var10004[3] = new BooleanSetting("Китайская шляпа", true);
      this.cosmetics = new MultiBooleanSetting("Косметика", var10004);
      this.butterflyWingAnimation = new BooleanSetting("Анимация крыльев", true).visible(() -> this.cosmetics.is("Крылья"));
      this.butterflyWingSize = new SliderSetting(
            "Размер",
            1.0f,
            0.6499999761581421f,
            1.7999999523162842f,
            0.05000000074505806f
         )
         .visible(() -> this.cosmetics.is("Крылья"));
      this.classicWingAnimation = new BooleanSetting("Анимация крыльев", true)
         .visible(() -> this.cosmetics.is("Крылья 2"));
      this.classicWingSize = new SliderSetting(
            "Размер",
            1.0f,
            0.6499999761581421f,
            1.7999999523162842f,
            0.05000000074505806f
         )
         .visible(() -> this.cosmetics.is("Крылья 2"));
      Setting[] var10001 = new Setting[5];
      var10001[0] = this.cosmetics;
      var10001[1] = this.butterflyWingAnimation;
      var10001[2] = this.butterflyWingSize;
      var10001[3] = this.classicWingAnimation;
      var10001[4] = this.classicWingSize;
      this.addSettings(var10001);
   }

   @Override
   public void onDisable() {
      this.selfClassicBodyYawInitialized = false;
      this.lastButterflySelected = false;
      this.lastClassicSelected = false;
      super.onDisable();
   }

   @EventTarget
   public void onRender3D(RenderEvent event) {
      if (mc.player != null && mc.world != null) {
         this.syncWingSelectionState();
         if (this.cosmetics.is("Нимб")) {
            this.renderNimbus(event);
         }

         boolean renderButterfly = this.cosmetics.is("Крылья");
         boolean renderClassic = this.cosmetics.is("Крылья 2");
         if (renderButterfly || renderClassic) {
            float tickDelta = event.getTickDelta();
            MatrixStack matrices = event.getMatrices();
            Vec3d cameraPos = event.getCamera().getPos();

            for (PlayerEntity player : mc.world.getPlayers()) {
               if (this.shouldRenderCosmeticForPlayer(player) && (player != mc.player || !mc.options.getPerspective().isFirstPerson())) {
                  if (renderButterfly) {
                     this.renderButterflyWings(player, tickDelta, matrices, cameraPos);
                  }

                  if (renderClassic) {
                     this.renderClassicWings(player, tickDelta, matrices, cameraPos);
                  }
               }
            }
         }
      }
   }
   public void renderButterflyWings(PlayerEntity player, float tickDelta, MatrixStack matrices, Vec3d cameraPos) {
      if (!player.isGliding() && player.getPose() != EntityPose.SWIMMING && !player.isInSwimmingPose()) {
         Vec3d velocity = player.getVelocity();
         float bodyYaw = MathHelper.lerp(tickDelta, player.prevBodyYaw, player.bodyYaw);
         float yawRad = bodyYaw * 0.01745329238474369f;
         Vec3d forward = new Vec3d(
            (double)(-MathHelper.sin(yawRad)),
            0.0,
            (double)MathHelper.cos(yawRad)
         );
         Vec3d sideways = new Vec3d(forward.z, 0.0, -forward.x);
         float forwardMove = (float)(velocity.x * forward.x + velocity.z * forward.z);
         float strafeMove = (float)(velocity.x * sideways.x + velocity.z * sideways.z);
         float verticalMove = (float)velocity.y;
         boolean animated = this.butterflyWingAnimation.isState();
         float smoothLean = animated
            ? MathHelper.clamp(
               -forwardMove * 140.0f
                  - verticalMove * 48.0f,
               -24.0f,
               26.0f
            )
            : 0.0f;
         float smoothStrafe = animated
            ? MathHelper.clamp(
               strafeMove * 90.0f,
               -10.0f,
               10.0f
            )
            : 0.0f;
         float wingSpring = animated
            ? MathHelper.clamp(
               Math.abs(forwardMove) * 0.949999988079071f
                  + Math.abs(strafeMove) * 0.6499999761581421f
                  + Math.abs(verticalMove) * 0.75f,
               0.0f,
               1.7000000476837158f
            )
            : 0.0f;
         float anim = ((float)player.age + tickDelta)
               * 0.2199999988079071f
               * 1.600000023841858f
            + wingSpring * 0.4000000059604645f;
         float sin = animated ? MathHelper.sin(anim) : 0.0f;
         float cos = animated ? MathHelper.cos(anim) : 0.0f;
         float spreadAngle = 18.0f + wingSpring * 5.0f;
         float pitchAngle = 13.0f
            + smoothLean * 0.30000001192092896f
            + cos * 4.0f;
         float rollAngle = sin * 25.0f
            + smoothStrafe * 0.75f;
         EntityPose pose = player.getPose();
         boolean fallFlying = player.isGliding();
         boolean horizontalPose = (!(pose != EntityPose.SWIMMING && !fallFlying));
         if (horizontalPose) {
            spreadAngle -= 4.0f;
            pitchAngle -= 6.0f;
            rollAngle *= 0.7200000286102295f;
         }

         if (player.isSneaking()) {
            spreadAngle -= 3.0f;
            pitchAngle += 8.0f;
         }

         double px = MathHelper.lerp((double)tickDelta, player.prevX, player.getX()) - cameraPos.x;
         double py = MathHelper.lerp((double)tickDelta, player.prevY, player.getY()) - cameraPos.y;
         double pz = MathHelper.lerp((double)tickDelta, player.prevZ, player.getZ()) - cameraPos.z;
         matrices.push();
         matrices.translate(px, py, pz);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-bodyYaw));
         this.applyBackPoseTransform(matrices, player, tickDelta, pose, fallFlying);
         int theme = this.resolveCosmeticThemeColor();
         int topColor = ObsidianComponent_olorUtil.setAlphaColor(theme, 132);
         int bottomColor = ObsidianComponent_olorUtil.setAlphaColor(ObsidianComponent_olorUtil.darken(theme, 0.8500000238418579f), 102);
         int outlineColor = ObsidianComponent_olorUtil.setAlphaColor(
            ObsidianComponent_olorUtil.darken(theme, 0.5799999833106995f), 214
         );
         RenderSystem.enableBlend();
         RenderSystem.disableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.blendFunc(770, 771);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         float butterflyScale = 1.0f * this.butterflyWingSize.get();
         BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
         this.renderButterflyWing(buffer, matrices, 1.0F, spreadAngle, pitchAngle, rollAngle, butterflyScale, topColor, bottomColor);
         this.renderButterflyWing(
            buffer, matrices, -1.0f, spreadAngle, pitchAngle, rollAngle, butterflyScale, topColor, bottomColor
         );
         BufferRenderer.drawWithGlobalProgram(buffer.end());
         RenderSystem.lineWidth(1.899999976158142f);
         BufferBuilder outlineBuffer = Tessellator.getInstance().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
         this.renderButterflyWingOutline(
            outlineBuffer,
            matrices,
            1.0f,
            spreadAngle,
            pitchAngle,
            rollAngle,
            butterflyScale,
            outlineColor
         );
         this.renderButterflyWingOutline(
            outlineBuffer, matrices, -1.0f, spreadAngle, pitchAngle, rollAngle, butterflyScale, outlineColor
         );
         BufferRenderer.drawWithGlobalProgram(outlineBuffer.end());
         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
         RenderSystem.depthMask(true);
         matrices.pop();
      }
   }
   public void renderClassicWings(PlayerEntity player, float tickDelta, MatrixStack matrices, Vec3d cameraPos) {
      if (player.isAlive() && !player.isInvisible()) {
         if (!player.isGliding() && player.getPose() != EntityPose.SWIMMING && !player.isInSwimmingPose()) {
            double px = MathHelper.lerp((double)tickDelta, player.prevX, player.getX()) - cameraPos.x;
            double py = MathHelper.lerp((double)tickDelta, player.prevY, player.getY()) - cameraPos.y;
            double pz = MathHelper.lerp((double)tickDelta, player.prevZ, player.getZ()) - cameraPos.z;
            float bodyYaw = this.resolveClassicBodyYaw(player, tickDelta);
            Vec3d velocity = player.getVelocity();
            float yawRad = bodyYaw * 0.01745329238474369f;
            Vec3d forward = new Vec3d(
               (double)(-MathHelper.sin(yawRad)),
               0.0,
               (double)MathHelper.cos(yawRad)
            );
            Vec3d sideways = new Vec3d(forward.z, 0.0, -forward.x);
            float forwardMove = (float)(velocity.x * forward.x + velocity.z * forward.z);
            float strafeMove = (float)(velocity.x * sideways.x + velocity.z * sideways.z);
            float verticalMove = (float)velocity.y;
            boolean animated = this.classicWingAnimation.isState();
            float smoothLean = animated
               ? MathHelper.clamp(
                  -forwardMove * 140.0f
                     - verticalMove * 48.0f,
                  -24.0f,
                  26.0f
               )
               : 0.0f;
            float smoothStrafe = animated
               ? MathHelper.clamp(strafeMove * 90.0F, -10.0F, 10.0f)
               : 0.0f;
            float wingSpring = animated
               ? MathHelper.clamp(
                  Math.abs(forwardMove) * 0.949999988079071f
                     + Math.abs(strafeMove) * 0.6499999761581421f
                     + Math.abs(verticalMove) * 0.75f,
                  0.0f,
                  1.7000000476837158f
               )
               : 0.0f;
            float anim = ((float)player.age + tickDelta)
                  * 0.2199999988079071f
                  * 1.600000023841858f
               + wingSpring * 0.4F;
            float sin = animated ? MathHelper.sin(anim) : 0.0f;
            float cos = animated ? MathHelper.cos(anim) : 0.0f;
            float spreadAngle = 18.0f
               + wingSpring * 5.0f;
            float pitchAngle = 13.0f
               + smoothLean * 0.30000001192092896f
               + cos * 4.0f;
            float rollAngle = sin * 25.0f
               + smoothStrafe * 0.75f;
            EntityPose pose = player.getPose();
            boolean fallFlying = player.isGliding();
            boolean horizontalPose = (!(pose != EntityPose.SWIMMING && !fallFlying));
            if (horizontalPose) {
               spreadAngle -= 4.0f;
               pitchAngle -= 6.0f;
               rollAngle *= 0.7200000286102295f;
            }

            if (player.isSneaking()) {
               spreadAngle -= 3.0f;
               pitchAngle += 8.0f;
            }

            ObsidianComponent_194 wingPose = this.resolveClassicWingPose(player, tickDelta, pose);
            float open = spreadAngle * wingPose.openMultiplier;
            float scale = wingPose.scaleMultiplier * this.classicWingSize.get();
            float animatedSidePitch = wingPose.sidePitch + pitchAngle * 0.18000000715255737f;
            float animatedSideRoll = wingPose.sideRoll + rollAngle * 0.20000000298023224f;
            int theme = this.resolveCosmeticThemeColor();
            int baseColor = ObsidianComponent_olorUtil.setAlphaColor(theme, 220);
            int glowColor = ObsidianComponent_olorUtil.setAlphaColor(
               ObsidianComponent_olorUtil.interpolate(theme, -1, 0.2800000011920929f),
               Math.round(48.400001525878906f)
            );
            int coreColor = ObsidianComponent_olorUtil.setAlphaColor(
               ObsidianComponent_olorUtil.interpolate(theme, -1, 0.550000011920929f),
               Math.round(57.19999694824219f)
            );
            int outlineColor = ObsidianComponent_olorUtil.setAlphaColor(
               ObsidianComponent_olorUtil.darken(theme, 0.6200000047683716f), Math.round(136.39999389648438f)
            );
            int ribsColor = ObsidianComponent_olorUtil.setAlphaColor(
               ObsidianComponent_olorUtil.interpolate(theme, -1, 0.2800000011920929f),
               Math.round(44.0f)
            );
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.blendFunc(770, 771);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            matrices.push();
            matrices.translate(px, py, pz);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - bodyYaw));
            if (wingPose.preTranslateY != 0.0f
               || wingPose.preTranslateZ != 0.0f) {
               matrices.translate(0.0f, wingPose.preTranslateY, wingPose.preTranslateZ);
            }

            if (wingPose.pitchRotation != 0.0f) {
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(wingPose.pitchRotation));
            }

            if (wingPose.rollRotation != 0.0f) {
               matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(wingPose.rollRotation));
            }

            matrices.translate(0.0f, wingPose.anchorY, wingPose.anchorZ);
            matrices.scale(scale, scale, scale);
            this.renderClassicWingSide(
               matrices,
               -1.0f,
               open,
               animatedSidePitch,
               animatedSideRoll,
               baseColor,
               glowColor,
               coreColor,
               outlineColor,
               ribsColor,
               wingPose
            );
            this.renderClassicWingSide(
               matrices,
               1.0f,
               open,
               animatedSidePitch,
               animatedSideRoll,
               baseColor,
               glowColor,
               coreColor,
               outlineColor,
               ribsColor,
               wingPose
            );
            matrices.pop();
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
         }
      }
   }
   public void renderNimbus(RenderEvent event) {
      if (mc.player != null && mc.world != null && !mc.options.getPerspective().isFirstPerson()) {
         float tickDelta = event.getTickDelta();
         Vec3d camera = event.getCamera().getPos();
         double x = MathHelper.lerp((double)tickDelta, mc.player.prevX, mc.player.getX());
         double y = MathHelper.lerp((double)tickDelta, mc.player.prevY, mc.player.getY())
            + (double)mc.player.getHeight()
            + 0.1;
         double z = MathHelper.lerp((double)tickDelta, mc.player.prevZ, mc.player.getZ());
         int baseColor = this.resolveCosmeticThemeColor();
         long nowMs = System.currentTimeMillis();
         double radiansPerMillisecond = 0.0029670597283903604;
         RenderSystem.enableBlend();
         RenderSystem.disableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.blendFunc(770, 1);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
         RenderSystem.setShaderTexture(0, this.getNimbusTexture());
         MatrixStack matrices = event.getMatrices();

         for (int arm = 0; arm < (2); arm++) {
            double baseAngle = radiansPerMillisecond * (double)nowMs
               + (double)arm * 3.141592653589793;

            for (int segment = 0; segment < 17; segment++) {
               double segmentAngle = baseAngle - (double)segment * 0.11;
               double offsetX = Math.cos(segmentAngle) * 0.44999998807907104;
               double offsetZ = Math.sin(segmentAngle) * 0.44999998807907104;
               float progress = (float)segment / (float)Math.max(1, 16);
               float size = 0.23000000417232513f
                  * (1.0f - progress * 0.699999988079071f);
               int alpha = MathHelper.clamp(
                  255 - segment * (9),
                  0,
                  255
               );
               int segmentColor = ObsidianComponent_olorUtil.setAlphaColor(baseColor, alpha);
               this.renderNimbusBillboard(
                  matrices,
                  event.getCamera().getYaw(),
                  event.getCamera().getPitch(),
                  x - camera.x + offsetX,
                  y - camera.y,
                  z - camera.z + offsetZ,
                  size,
                  segmentColor
               );
            }
         }

         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(true);
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
      }
   }

   public void renderChinaHat(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, PlayerEntity player, ModelWithHead model) {
      if (this.isEnable() && this.cosmetics.is("Китайская шляпа")) {
         if (mc.player != null && mc.world != null) {
            if (this.shouldRenderCosmeticForPlayer(player)) {
               if (player != mc.player || !mc.options.getPerspective().isFirstPerson()) {
                  double radius = player.getBoundingBox().maxX - player.getBoundingBox().minX;
                  float offset = player.getEquippedStack(EquipmentSlot.HEAD).isEmpty()
                     ? 0.41499999165534973f
                     : 0.47999998927116394f;
                  matrixStack.push();
                  model.getHead().rotate(matrixStack);
                  RenderSystem.enableBlend();
                  RenderSystem.enableDepthTest();
                  RenderSystem.disableCull();
                  RenderSystem.defaultBlendFunc();
                  RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
                  RenderSystem.setShaderColor(
                     1.0f,
                     1.0f,
                     1.0f,
                     1.0f
                  );
                  RenderSystem.lineWidth(2.0f);
                  GL11.glEnable(2848);
                  GL11.glHint(3154, 4354);
                  matrixStack.translate(
                     0.0f, -offset, 0.0f
                  );
                  matrixStack.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(180.0f));
                  matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0f));
                  Matrix4f matrix = matrixStack.peek().getPositionMatrix();
                  Tessellator tessellator = Tessellator.getInstance();
                  BufferBuilder buffer = tessellator.begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                  float y = 0.0f;
                  int colorTheme = this.resolveCosmeticThemeColor();
                  int coneColor = ObsidianComponent_olorUtil.setAlphaColor(colorTheme, 125);
                  int outlineColor = ObsidianComponent_olorUtil.setAlphaColor(ObsidianComponent_olorUtil.darken(colorTheme, 0.5f), 180);

                  for (int i = 0; i <= (180); i++) {
                     float iPi = (float)i * 0.06981316953897476f;
                     float x = (float)((double)MathHelper.sin(iPi) * radius);
                     float z = (float)((double)MathHelper.cos(iPi) * radius);
                     buffer.vertex(matrix, x, y, z).color(coneColor);
                     buffer.vertex(
                           matrix,
                           0.0f,
                           0.30000001192092896f,
                           0.0f
                        )
                        .color(colorTheme);
                  }

                  BufferRenderer.drawWithGlobalProgram(buffer.end());
                  RenderSystem.depthMask(false);
                  buffer = tessellator.begin(DrawMode.LINE_STRIP, VertexFormats.POSITION_COLOR);
                  float firstX = 0.0f;
                  float firstZ = 0.0f;
                  boolean firstSet = false;

                  for (int i = 0; i <= (180); i++) {
                     float iPi = (float)i * 0.06981316953897476f;
                     float x = (float)((double)MathHelper.sin(iPi) * radius);
                     float z = (float)((double)MathHelper.cos(iPi) * radius);
                     buffer.vertex(matrix, x, y, z).color(outlineColor);
                     if (!firstSet) {
                        firstX = x;
                        firstZ = z;
                        firstSet = true;
                     }
                  }

                  if (firstSet) {
                     buffer.vertex(matrix, firstX, y, firstZ).color(outlineColor);
                  }

                  BufferRenderer.drawWithGlobalProgram(buffer.end());
                  RenderSystem.depthMask(true);
                  RenderSystem.enableCull();
                  RenderSystem.defaultBlendFunc();
                  RenderSystem.setShaderColor(
                     1.0f,
                     1.0f,
                     1.0f,
                     1.0f
                  );
                  GL11.glDisable(2848);
                  matrixStack.pop();
               }
            }
         }
      }
   }
   public Identifier getNimbusTexture() {
      return Identifier.of("obsidian", "textures/targetesp/bloom.png");
   }
   public void renderNimbusBillboard(MatrixStack matrices, float cameraYaw, float cameraPitch, double x, double y, double z, float size, int color) {
      int a = color >> (24) & (255);
      if (a > 0) {
         int r = color >> (16) & (255);
         int g = color >> 8 & 255;
         int b = color & 255;
         float half = size * 0.5f;
         matrices.push();
         matrices.translate(x, y, z);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-cameraYaw));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cameraPitch));
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
   public boolean shouldRenderCosmeticForPlayer(PlayerEntity player) {
      if (mc.player == null) {
         return false;
      } else if (player == mc.player) {
         return true;
      } else {
         return (Obsidian.INSTANCE != null
               && Obsidian.INSTANCE.friendStorage != null
               && Obsidian.INSTANCE.friendStorage.isFriend(player.getName().getString()));
      }
   }
   public int getStableThemeColor() {
      if (Obsidian.INSTANCE != null && Obsidian.INSTANCE.themeStorage != null && Obsidian.INSTANCE.themeStorage.getThemes() != null) {
         ObsidianComponent_olorPalette theme = Obsidian.INSTANCE.themeStorage.getThemes().getTheme();
         return theme != null && theme.color != null && theme.color.length != 0
            ? theme.color[0]
            : ObsidianComponent_olorUtil.getThemeColor(0);
      } else {
         return ObsidianComponent_olorUtil.getThemeColor(0);
      }
   }
   public int resolveCosmeticThemeColor() {
      if (Obsidian.INSTANCE != null && Obsidian.INSTANCE.themeStorage != null && Obsidian.INSTANCE.themeStorage.getThemes() != null) {
         ObsidianComponent_olorPalette theme = Obsidian.INSTANCE.themeStorage.getThemes().getTheme();
         if (theme == null) {
            return ObsidianComponent_olorUtil.getThemeColor();
         } else {
            return "Rainbow".equals(theme.getName()) ? ObsidianComponent_olorUtil.getThemeColor() : this.getStableThemeColor();
         }
      } else {
         return ObsidianComponent_olorUtil.getThemeColor();
      }
   }
   public void syncWingSelectionState() {
      boolean butterfly = this.cosmetics.is("Крылья");
      boolean classic = this.cosmetics.is("Крылья 2");
      if (butterfly && classic) {
         if (butterfly != this.lastButterflySelected && classic == this.lastClassicSelected) {
            this.cosmetics.set("Крылья 2", false);
            classic = false;
         } else {
            this.cosmetics.set("Крылья", false);
            butterfly = false;
         }
      }

      this.lastButterflySelected = butterfly;
      this.lastClassicSelected = classic;
   }
   public void applyBackPoseTransform(MatrixStack matrices, PlayerEntity player, float tickDelta, EntityPose pose, boolean fallFlying) {
      if (fallFlying) {
         float pitch = player.getPitch(tickDelta);
         float clampedPitch = MathHelper.clamp(pitch, -65.0f, 65.0f);
         matrices.translate(
            0.0f,
            0.30000001192092896f,
            0.0f
         );
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-(90.0f + clampedPitch)));
         matrices.translate(
            0.0f,
            -0.15000000596046448f,
            0.11999999731779099f
         );
      } else if (pose == EntityPose.SWIMMING) {
         float pitch = player.getPitch(tickDelta);
         float clampedPitch = MathHelper.clamp(
            pitch, -65.0f, 65.0f
         );
         matrices.translate(
            0.0f,
            0.30000001192092896f,
            0.0f
         );
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-(90.0f + clampedPitch)));
         matrices.translate(
            0.0f,
            -0.15000000596046448f,
            0.11999999731779099f
         );
      } else {
         if (player.isSneaking()) {
            matrices.translate(
               0.0f,
               1.149999976158142f,
               0.0f
            );
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(24.0f));
            matrices.translate(
               0.0f,
               0.0f,
               0.07999999821186066f
            );
         } else {
            matrices.translate(
               0.0f,
               1.2999999523162842f,
               0.07999999821186066f
            );
         }
      }
   }
   public float resolveClassicBodyYaw(PlayerEntity player, float tickDelta) {
      float targetBodyYaw = MathHelper.lerpAngleDegrees(tickDelta, player.prevBodyYaw, player.bodyYaw);
      if (player != mc.player) {
         return targetBodyYaw;
      } else if (!this.selfClassicBodyYawInitialized) {
         this.selfClassicBodyYaw = targetBodyYaw;
         this.selfClassicBodyYawInitialized = true;
         return this.selfClassicBodyYaw;
      } else {
         float delta = MathHelper.wrapDegrees(targetBodyYaw - this.selfClassicBodyYaw);
         this.selfClassicBodyYaw = this.selfClassicBodyYaw
            + MathHelper.clamp(delta, -14.0f, 14.0f);
         return this.selfClassicBodyYaw;
      }
   }
   public ObsidianComponent_194 resolveClassicWingPose(PlayerEntity player, float tickDelta, EntityPose pose) {
      float pitch = player.getPitch(tickDelta);
      if (player.isGliding()) {
         float clampedPitch = MathHelper.clamp(
            pitch, -65.0f, 65.0f
         );
         return new ObsidianComponent_194(
            1.1799999475479126f,
            0.10000000149011612f,
            0.0f,
            0.0f,
            -(90.0f + clampedPitch),
            0.0f,
            0.7599999904632568f,
            0.9200000166893005f,
            0.10000000149011612f,
            0.5799999833106995f,
            0.05000000074505806f,
            0.0f,
            0.05999999865889549f,
            -5.0f,
            -2.0f,
            0.12999999523162842f
         );
      } else if (pose == EntityPose.SWIMMING || player.isInSwimmingPose()) {
         float clampedPitch = MathHelper.clamp(
            pitch, -65.0f, 65.0f
         );
         float bodyShiftY = player.isInSwimmingPose()
            ? 1.100000023841858f
            : 1.1799999475479126f;
         float bodyShiftZ = player.isInSwimmingPose()
            ? 0.18000000715255737f
            : 0.11999999731779099f;
         return new ObsidianComponent_194(
            bodyShiftY,
            bodyShiftZ,
            0.18000000715255737f,
            0.47999998927116394f,
            -(90.0f + clampedPitch),
            0.0f,
            0.8399999737739563f,
            0.9599999785423279f,
            0.11999999731779099f,
            0.699999988079071f,
            0.029999999329447746f,
            0.0f,
            0.009999999776482582f,
            -7.0f,
            -3.0f,
            0.1599999964237213f
         );
      } else {
         return player.isSneaking()
            ? new ObsidianComponent_194(
               0.0f,
               0.0f,
               0.9599999785423279f,
               0.10000000149011612f,
               18.0f,
               0.0f,
               1.0f,
               1.0f,
               0.18000000715255737f,
               4.5f,
               0.05999999865889549f,
               0.0f,
               0.019999999552965164f,
               -11.0f,
               -4.0f,
               0.11999999731779099f
            )
            : new ObsidianComponent_194(
               0.0f,
               0.0f,
               1.1799999475479126f,
               0.10000000149011612f,
               0.0f,
               0.0f,
               1.0f,
               1.0f,
               0.18000000715255737f,
               4.5f,
               0.05999999865889549f,
               0.0f,
               0.019999999552965164f,
               -11.0f,
               -4.0f,
               0.11999999731779099f
            );
      }
   }
   public void renderClassicWingSide(
      MatrixStack matrices,
      float side,
      float open,
      float sidePitch,
      float sideRoll,
      int baseColor,
      int glowColor,
      int coreColor,
      int outlineColor,
      int ribsColor,
      ObsidianComponent_194 pose
   ) {
      matrices.push();
      matrices.translate(side * pose.sideOffset, pose.sideYOffset, pose.sideZOffset);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * open));
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side * sideRoll));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sidePitch));
      RenderSystem.blendFunc(770, 1);
      this.drawClassicWingLayer(
         matrices, side, 1.2200000286102295f, glowColor, ObsidianComponent_olorUtil.setAlphaColor(glowColor, 0)
      );
      this.drawClassicWingLayer(
         matrices,
         side,
         0.8399999737739563f,
         coreColor,
         ObsidianComponent_olorUtil.setAlphaColor(coreColor, 0)
      );
      RenderSystem.blendFunc(770, 771);
      this.drawClassicWingLayer(
         matrices, side, 1.0f, baseColor, ObsidianComponent_olorUtil.setAlphaColor(baseColor, 10)
      );
      RenderSystem.blendFunc(770, 1);
      this.drawClassicWingOutline(matrices, side, 1.0f, outlineColor);
      this.drawClassicWingRibs(matrices, side, 0.9599999785423279f, ribsColor);
      matrices.pop();
   }
   public void drawClassicWingLayer(MatrixStack matrices, float side, float scale, int rootColor, int edgeColor) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

      for (int i = 0; i < CLASSIC_WING_SHAPE.length; i++) {
         ObsidianComponent_433 current = CLASSIC_WING_SHAPE[i];
         ObsidianComponent_433 next = CLASSIC_WING_SHAPE[(i + 1) % CLASSIC_WING_SHAPE.length];
         this.vertex(
            buffer,
            matrix,
            0.0f,
            0.0f,
            0.0f,
            rootColor
         );
         this.vertex(
            buffer,
            matrix,
            side * current.x * scale,
            current.y * scale,
            0.0f,
            this.applyClassicWingPointAlpha(edgeColor, current.alphaMultiplier)
         );
         this.vertex(
            buffer,
            matrix,
            side * next.x * scale,
            next.y * scale,
            0.0f,
            this.applyClassicWingPointAlpha(edgeColor, next.alphaMultiplier)
         );
      }

      BufferRenderer.drawWithGlobalProgram(buffer.end());
   }
   public void drawClassicWingOutline(MatrixStack matrices, float side, float scale, int color) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
      RenderSystem.lineWidth(1.350000023841858f);
      GL11.glEnable(2848);

      for (int i = 0; i < CLASSIC_WING_SHAPE.length; i++) {
         ObsidianComponent_433 current = CLASSIC_WING_SHAPE[i];
         ObsidianComponent_433 next = CLASSIC_WING_SHAPE[(i + (1)) % CLASSIC_WING_SHAPE.length];
         this.addLine(
            buffer,
            matrix,
            side * current.x * scale,
            current.y * scale,
            0.0f,
            side * next.x * scale,
            next.y * scale,
            0.0f,
            color
         );
      }

      BufferRenderer.drawWithGlobalProgram(buffer.end());
      GL11.glDisable(2848);
   }
   public void drawClassicWingRibs(MatrixStack matrices, float side, float scale, int color) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
      int[] var10000 = new int[5];
      var10000[0] = 2;
      var10000[1] = 4;
      var10000[2] = 7;
      var10000[3] = 9;
      var10000[4] = 11;
      int[] ribIndices = var10000;
      RenderSystem.lineWidth(0.8999999761581421f);
      int[] var8 = ribIndices;
      int var9 = ribIndices.length;

      for (int var10 = 0; var10 < var9; var10++) {
         int ribIndex = var8[var10];
         ObsidianComponent_433 point = CLASSIC_WING_SHAPE[ribIndex];
         this.vertex(
            buffer,
            matrix,
            0.0f,
            0.0f,
            0.0f,
            ObsidianComponent_olorUtil.setAlphaColor(
               color,
               Math.max(
                  8,
                  (int)((float)(color >> 24 & 255) * 0.75f)
               )
            )
         );
         this.vertex(
            buffer,
            matrix,
            side * point.x * scale,
            point.y * scale,
            0.0f,
            this.applyClassicWingPointAlpha(color, point.alphaMultiplier)
         );
      }

      BufferRenderer.drawWithGlobalProgram(buffer.end());
   }
   public int applyClassicWingPointAlpha(int color, float multiplier) {
      int alpha = color >> (24) & 0xFF;
      return ObsidianComponent_olorUtil.setAlphaColor(
         color, Math.max(0, Math.min(255, (int)((float)alpha * multiplier)))
      );
   }
   public void vertex(BufferBuilder buffer, Matrix4f matrix, float x, float y, float z, int color) {
      buffer.vertex(matrix, x, y, z).color(color);
   }
   public void renderButterflyWing(
      BufferBuilder buffer, MatrixStack matrices, float side, float spread, float pitch, float roll, float scale, int topColor, int bottomColor
   ) {
      float root = 0.11999999731779099f * scale;
      float topW = 1.5199999809265137f * scale;
      float topH = 0.6399999856948853f * scale;
      float lowW = 1.1399999856948853f * scale;
      float lowH = 0.38999998569488525f * scale;
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
      this.addDoubleSidedGradientTriangle(
         buffer,
         matrix,
         side * root,
         0.019999999552965164f,
         -0.009999999776482582f,
         side * (root + topW * 0.2199999988079071f),
         topH * 0.9800000190734863f,
         -0.05999999865889549f,
         side * (root + topW * 0.8799999952316284f),
         topH * 0.6000000238418579f,
         -0.12999999523162842f,
         topColor,
         bottomColor
      );
      this.addDoubleSidedGradientTriangle(
         buffer,
         matrix,
         side * root,
         0.019999999552965164f,
         -0.009999999776482582f,
         side * (root + topW * 0.8799999952316284f),
         topH * 0.6000000238418579f,
         -0.12999999523162842f,
         side * (root + topW),
         topH * 0.11999999731779099f,
         -0.17F,
         topColor,
         bottomColor
      );
      this.addDoubleSidedGradientTriangle(
         buffer,
         matrix,
         side * root,
         -0.029999999329447746f,
         -0.029999999329447746f,
         side * (root + lowW * 0.25999999046325684f),
         -lowH * 0.9599999785423279f,
         -0.10999999940395355f,
         side * (root + lowW * 0.8399999737739563f),
         -lowH * 0.5400000214576721f,
         -0.18F,
         bottomColor,
         topColor
      );
      this.addDoubleSidedGradientTriangle(
         buffer,
         matrix,
         side * root,
         -0.029999999329447746f,
         -0.029999999329447746f,
         side * (root + lowW * 0.8399999737739563f),
         -lowH * 0.5400000214576721f,
         -0.18000000715255737f,
         side * (root + lowW),
         -lowH * 0.11999999731779099f,
         -0.20999999344348907f,
         bottomColor,
         topColor
      );
      matrices.pop();
   }
   public void renderButterflyWingOutline(
      BufferBuilder buffer, MatrixStack matrices, float side, float spread, float pitch, float roll, float scale, int outlineColor
   ) {
      float root = 0.11999999731779099f * scale;
      float topW = 1.5199999809265137f * scale;
      float topH = 0.6399999856948853f * scale;
      float lowW = 1.1399999856948853f * scale;
      float lowH = 0.38999998569488525f * scale;
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
      this.addLine(
         buffer,
         matrix,
         side * root,
         0.019999999552965164f,
         -0.009999999776482582f,
         side * (root + topW * 0.22F),
         topH * 0.9800000190734863f,
         -0.05999999865889549f,
         outlineColor
      );
      this.addLine(
         buffer,
         matrix,
         side * (root + topW * 0.2199999988079071f),
         topH * 0.9800000190734863f,
         -0.05999999865889549f,
         side * (root + topW * 0.8799999952316284f),
         topH * 0.6000000238418579f,
         -0.12999999523162842f,
         outlineColor
      );
      this.addLine(
         buffer,
         matrix,
         side * (root + topW * 0.8799999952316284f),
         topH * 0.6000000238418579f,
         -0.12999999523162842f,
         side * (root + topW),
         topH * 0.12F,
         -0.17000000178813934f,
         outlineColor
      );
      this.addLine(
         buffer,
         matrix,
         side * root,
         -0.029999999329447746f,
         -0.029999999329447746f,
         side * (root + lowW * 0.25999999046325684f),
         -lowH * 0.9599999785423279f,
         -0.10999999940395355f,
         outlineColor
      );
      this.addLine(
         buffer,
         matrix,
         side * (root + lowW * 0.25999999046325684f),
         -lowH * 0.9599999785423279f,
         -0.10999999940395355f,
         side * (root + lowW * 0.8399999737739563f),
         -lowH * 0.5400000214576721f,
         -0.18000000715255737f,
         outlineColor
      );
      this.addLine(
         buffer,
         matrix,
         side * (root + lowW * 0.8399999737739563f),
         -lowH * 0.5400000214576721f,
         -0.18000000715255737f,
         side * (root + lowW),
         -lowH * 0.11999999731779099f,
         -0.20999999344348907f,
         outlineColor
      );
      this.addLine(
         buffer,
         matrix,
         side * root,
         -0.009999999776482582f,
         -0.019999999552965164f,
         side * (root + topW * 0.6000000238418579f),
         0.07999999821186066f,
         -0.07999999821186066f,
         outlineColor
      );
      matrices.pop();
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
      int nr = nearColor >> 16 & (255);
      int ng = nearColor >> (8) & (255);
      int nb = nearColor & 255;
      int na = nearColor >> (24) & 255;
      int fr = farColor >> (16) & (255);
      int fg = farColor >> (8) & (255);
      int fb = farColor & (255);
      int fa = farColor >> (24) & (255);
      buffer.vertex(matrix, x1, y1, z1).color(nr, ng, nb, na);
      buffer.vertex(matrix, x2, y2, z2).color(fr, fg, fb, fa);
      buffer.vertex(matrix, x3, y3, z3).color(fr, fg, fb, fa);
      buffer.vertex(matrix, x4, y4, z4).color(nr, ng, nb, na);
      buffer.vertex(matrix, x4, y4, z4).color(nr, ng, nb, na);
      buffer.vertex(matrix, x3, y3, z3).color(fr, fg, fb, fa);
      buffer.vertex(matrix, x2, y2, z2).color(fr, fg, fb, fa);
      buffer.vertex(matrix, x1, y1, z1).color(nr, ng, nb, na);
   }
   public void addDoubleSidedGradientTriangle(
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
      int nearColor,
      int farColor
   ) {
      int nr = nearColor >> (16) & 255;
      int ng = nearColor >> 8 & (255);
      int nb = nearColor & 255;
      int na = nearColor >> (24) & 255;
      int fr = farColor >> (16) & (255);
      int fg = farColor >> (8) & (255);
      int fb = farColor & 255;
      int fa = farColor >> (24) & (255);
      buffer.vertex(matrix, x1, y1, z1).color(nr, ng, nb, na);
      buffer.vertex(matrix, x2, y2, z2).color(fr, fg, fb, fa);
      buffer.vertex(matrix, x3, y3, z3).color(fr, fg, fb, fa);
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
      int aG = colorA >> (8) & 255;
      int aB = colorA & 255;
      int aA = colorA >> 24 & 255;
      int bR = colorB >> 16 & 255;
      int bG = colorB >> (8) & 255;
      int bB = colorB & (255);
      int bA = colorB >> 24 & 255;
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
   public void addLine(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, int color) {
      int r = color >> 16 & 255;
      int g = color >> (8) & (255);
      int b = color & (255);
      int a = color >> (24) & (255);
      buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a);
      buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a);
   }

   static {
      ObsidianComponent_433[] var10000 = new ObsidianComponent_433[13];
      var10000[0] = new ObsidianComponent_433(
         0.07999999821186066f,
         0.10000000149011612f,
         0.8799999952316284f
      );
      var10000[1] = new ObsidianComponent_433(
         0.2800000011920929f,
         0.3400000035762787f,
         0.7799999713897705f
      );
      var10000[2] = new ObsidianComponent_433(
         0.5600000023841858f,
         0.8199999928474426f,
         0.6200000047683716f
      );
      var10000[3] = new ObsidianComponent_433(
         0.86F, 0.30000001192092896f, 0.5199999809265137f
      );
      var10000[4] = new ObsidianComponent_433(
         1.1399999856948853f,
         0.46000000834465027f,
         0.4000000059604645f
      );
      var10000[5] = new ObsidianComponent_433(
         1.2400000095367432f,
         0.03999999910593033f,
         0.30000001192092896f
      );
      var10000[6] = new ObsidianComponent_433(
         1.0199999809265137f,
         -0.18000000715255737f,
         0.2800000011920929f
      );
      var10000[7] = new ObsidianComponent_433(
         1.1799999475479126f,
         -0.6399999856948853f,
         0.2199999988079071f
      );
      var10000[8] = new ObsidianComponent_433(
         0.8600000143051147f,
         -0.46000000834465027f,
         0.20000000298023224f
      );
      var10000[9] = new ObsidianComponent_433(
         0.800000011920929f,
         -0.9800000190734863f,
         0.14000000059604645f
      );
      var10000[10] = new ObsidianComponent_433(
         0.5400000214576721f,
         -0.7400000095367432f,
         0.1599999964237213f
      );
      var10000[11] = new ObsidianComponent_433(
         0.30000001192092896f,
         -1.159999966621399f,
         0.11999999731779099f
      );
      var10000[12] = new ObsidianComponent_433(
         0.10000000149011612f,
         -0.5400000214576721f,
         0.18000000715255737f
      );
      CLASSIC_WING_SHAPE = var10000;
   }
   public static CosmeticsModule INSTANCE = new CosmeticsModule();

}
