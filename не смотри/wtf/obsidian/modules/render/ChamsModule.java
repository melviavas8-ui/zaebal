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
import java.awt.Color;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;


public class ChamsModule extends Module {
   public static final ChamsModule INSTANCE = new ChamsModule();
   public static final String TARGET_PLAYERS = "Игроков";
   public static final String TARGET_FRIENDS = "Друзей";
   public static final String TARGET_SELF = "Себя";
   public static final int DEFAULT_FILL_ALPHA = 130;
   public static final float DEFAULT_LINE_WIDTH = 0.5F;
   public static final float CLIENT_FILL_SATURATION = 1.18F;
   public static final float CLIENT_FILL_BRIGHTNESS = 1.12F;
   public static final float CLIENT_OUTLINE_SATURATION = 1.12F;
   public static final float CLIENT_OUTLINE_BRIGHTNESS = 1.08F;
   public static final float MIN_PULSE_ALPHA = 0.65F;
   public static final float PULSE_SWING = 0.35F;
   private static final int FRIEND_FILL_COLOR = new Color(
         85, 255, 85, 60
      )
      .getRGB();
   private static final int FRIEND_OUTLINE_COLOR = new Color(
         100, 255, 100, 255
      )
      .getRGB();
   public static final long OUTLINE_RETRY_DELAY_MS = 3000L;
   public final MultiBooleanSetting rendering;
   public final BooleanSetting waves;
   public final SliderSetting waveSpeedX;
   public final SliderSetting waveSpeedY;
   public final SliderSetting waveScale;
   public final SliderSetting waveDensity;
   public final SliderSetting waveGlow;
   public final BooleanSetting glow;
   public final SliderSetting glowIntensity;
   public final SliderSetting glowLayers;
   public final BooleanSetting pulse;
   public final SliderSetting pulseSpeed;
   public final BooleanSetting hideOriginal;
   public final BooleanSetting hideItemsAndCape;
   public final long startTime;
   public boolean outlineAssistReady;
   public long nextOutlineRetryAt;

   public ChamsModule() {
      super("Chams", "Чамсы по модели игрока", Category.RENDER);
      BooleanSetting[] var10004 = new BooleanSetting[3];
      var10004[0] = new BooleanSetting("Игроков", true);
      var10004[1] = new BooleanSetting("Друзей", true);
      var10004[2] = new BooleanSetting("Себя", false);
      this.rendering = new MultiBooleanSetting("Отображать", var10004);
      this.waves = new BooleanSetting("Волны", true);
      this.waveSpeedX = new SliderSetting(
            "Скорость X",
            0.2199999988079071f,
            0.0f,
            1.5f,
            0.009999999776482582f
         )
         .visible(this.waves::isState);
      this.waveSpeedY = new SliderSetting(
            "Скорость Y",
            0.15000000596046448f,
            0.0f,
            1.5f,
            0.009999999776482582f
         )
         .visible(this.waves::isState);
      this.waveScale = new SliderSetting(
            "Размер волн",
            1.350000023841858f,
            0.20000000298023224f,
            4.0f,
            0.05000000074505806f
         )
         .visible(this.waves::isState);
      this.waveDensity = new SliderSetting(
            "Плотность волн",
            1.149999976158142f,
            0.5f,
            3.0f,
            0.05000000074505806f
         )
         .visible(this.waves::isState);
      this.waveGlow = new SliderSetting(
            "Сила волн",
            1.0f,
            0.20000000298023224f,
            3.0f,
            0.05000000074505806f
         )
         .visible(this.waves::isState);
      this.glow = new BooleanSetting("Свечение", true);
      this.glowIntensity = new SliderSetting(
            "Сила свечения",
            2.0f,
            1.0f,
            5.0f,
            0.10000000149011612f
         )
         .visible(this.glow::isState);
      this.glowLayers = new SliderSetting(
            "Слои свечения",
            3.0f,
            1.0f,
            6.0F,
            1.0f
         )
         .visible(this.glow::isState);
      this.pulse = new BooleanSetting("Пульсирование", false);
      this.pulseSpeed = new SliderSetting(
            "Скорость пульсации",
            2.0f,
            0.5f,
            5.0f,
            0.10000000149011612f
         )
         .visible(this.pulse::isState);
      this.hideOriginal = new BooleanSetting("Скрыть оригинал", false);
      this.hideItemsAndCape = new BooleanSetting("Скрывать предметы и плащ", false);
      this.startTime = System.currentTimeMillis();
      Setting[] var10001 = new Setting[14];
      var10001[0] = this.rendering;
      var10001[1] = this.waves;
      var10001[2] = this.waveSpeedX;
      var10001[3] = this.waveSpeedY;
      var10001[4] = this.waveScale;
      var10001[5] = this.waveDensity;
      var10001[6] = this.waveGlow;
      var10001[7] = this.glow;
      var10001[8] = this.glowIntensity;
      var10001[9] = this.glowLayers;
      var10001[10] = this.pulse;
      var10001[11] = this.pulseSpeed;
      var10001[12] = this.hideOriginal;
      var10001[13] = this.hideItemsAndCape;
      this.addSettings(var10001);
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.outlineAssistReady = false;
      this.nextOutlineRetryAt = 0L;
      this.tryEnsureOutlineProcessor();
   }

   @Override
   public void onDisable() {
      this.outlineAssistReady = false;
      this.nextOutlineRetryAt = 0L;
      super.onDisable();
   }

   @EventTarget(
      priority = 100
   )
   public void onRender3D(RenderEvent event) {
      if (this.isEnable() && mc.world != null && mc.player != null) {
         if (this.hasOutlineAssistTargets() && !this.outlineAssistReady && System.currentTimeMillis() >= this.nextOutlineRetryAt) {
            this.tryEnsureOutlineProcessor();
         }

         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableCull();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);

         for (PlayerEntity player : mc.world.getPlayers()) {
            if (this.affects(player) && (player != mc.player || mc.options.getPerspective() != Perspective.FIRST_PERSON)) {
               this.renderManualPlayer(event, player);
            }
         }

         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         RenderSystem.lineWidth(1.0f);
      }
   }
   public void renderManualPlayer(RenderEvent event, PlayerEntity player) {
      if (player instanceof AbstractClientPlayerEntity clientPlayer) {
         if ((Object)mc.getEntityRenderDispatcher().getRenderer(player) instanceof PlayerEntityRenderer renderer) {
            PlayerEntityRenderState state = renderer.createRenderState();
            renderer.updateRenderState(clientPlayer, state, event.getTickDelta());
            PlayerEntityModel model = (PlayerEntityModel)renderer.getModel();
            model.setAngles(state);
            MatrixStack matrices = event.getMatrices();
            matrices.push();
            this.setupModelMatrix(matrices, state, renderer, event.getCamera().getPos(), player, event.getTickDelta());
            int fillColor = this.resolveFillColor(player);
            int outlineColor = this.resolveOutlineColor(player);
            this.renderShaderFillModel(matrices, model, 0.0f, fillColor);
            this.renderOutlineModel(matrices, model, 0.0f, outlineColor);
            matrices.pop();
         }
      }
   }
   public void setupModelMatrix(
      MatrixStack matrices, PlayerEntityRenderState state, PlayerEntityRenderer renderer, Vec3d cameraPos, PlayerEntity player, float tickDelta
   ) {
      Vec3d pos = player.getLerpedPos(tickDelta);
      double x = pos.x - cameraPos.x;
      double y = pos.y - cameraPos.y;
      double z = pos.z - cameraPos.z;
      matrices.translate(x, y, z);
      if (state.sleepingDirection != null) {
         float eyeOffset = state.standingEyeHeight - 0.10000000149011612f;
         matrices.translate(
            (float)(-state.sleepingDirection.getOffsetX()) * eyeOffset,
            0.0f,
            (float)(-state.sleepingDirection.getOffsetZ()) * eyeOffset
         );
      }

      float baseScale = state.baseScale;
      matrices.scale(baseScale, baseScale, baseScale);
      LivingEntityRendererAccessor accessor = (LivingEntityRendererAccessor)renderer;
      accessor.obsidian$setupTransforms(state, matrices, state.bodyYaw, baseScale);
      matrices.scale(
         -1.0f,
         -1.0f,
         1.0f
      );
      accessor.obsidian$scale(state, matrices);
      matrices.translate(
         0.0f,
         -1.5010000467300415f,
         0.0f
      );
   }
   public void renderShaderFillModel(MatrixStack matrices, BipedEntityModel<?> model, float expand, int color) {
      if (!this.waves.isState()) {
         this.renderSolidFillModel(matrices, model, expand, color);
      } else {
         ShaderProgram shader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.chamsFill);
         if (shader != null) {
            RenderSystem.setShader(ObsidianComponent_257.chamsFill);
            this.setUniform(
               shader,
               "time",
               this.waves.isState()
                  ? (float)(System.currentTimeMillis() - this.startTime) / 1000.0f
                  : 0.0f
            );
            this.setUniform(shader, "speedX", this.waveSpeedX.get());
            this.setUniform(shader, "speedY", this.waveSpeedY.get());
            this.setUniform(shader, "scale", this.waveScale.get());
            this.setUniform(shader, "density", this.waveDensity.get());
            this.setUniform(shader, "glowStrength", this.waveGlow.get());
            BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            ModelPart root = model.getRootPart();
            this.renderFillPart(
               matrices,
               buffer,
               root,
               model.head,
               -4.0f,
               -8.0f,
               -4.0f,
               8.0f,
               8.0f,
               8.0f,
               expand,
               color
            );
            this.renderFillPart(
               matrices,
               buffer,
               root,
               model.body,
               -4.0f,
               0.0f,
               -2.0f,
               8.0f,
               12.0f,
               4.0f,
               expand,
               color
            );
            this.renderFillPart(
               matrices,
               buffer,
               root,
               model.rightArm,
               -3.0f,
               -2.0f,
               -2.0f,
               4.0f,
               12.0f,
               4.0f,
               expand,
               color
            );
            this.renderFillPart(
               matrices,
               buffer,
               root,
               model.leftArm,
               -1.0f,
               -2.0f,
               -2.0f,
               4.0f,
               12.0F,
               4.0f,
               expand,
               color
            );
            this.renderFillPart(
               matrices,
               buffer,
               root,
               model.rightLeg,
               -2.0f,
               0.0f,
               -2.0f,
               4.0f,
               12.0f,
               4.0f,
               expand,
               color
            );
            this.renderFillPart(
               matrices,
               buffer,
               root,
               model.leftLeg,
               -2.0f,
               0.0f,
               -2.0f,
               4.0f,
               12.0f,
               4.0f,
               expand,
               color
            );
            BufferRenderer.drawWithGlobalProgram(buffer.end());
         }
      }
   }
   public void renderSolidFillModel(MatrixStack matrices, BipedEntityModel<?> model, float expand, int color) {
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      ModelPart root = model.getRootPart();
      this.renderSolidFillPart(
         matrices,
         buffer,
         root,
         model.head,
         -4.0f,
         -8.0f,
         -4.0f,
         8.0f,
         8.0f,
         8.0f,
         expand,
         color
      );
      this.renderSolidFillPart(
         matrices,
         buffer,
         root,
         model.body,
         -4.0f,
         0.0f,
         -2.0f,
         8.0f,
         12.0f,
         4.0f,
         expand,
         color
      );
      this.renderSolidFillPart(
         matrices,
         buffer,
         root,
         model.rightArm,
         -3.0f,
         -2.0f,
         -2.0f,
         4.0f,
         12.0f,
         4.0f,
         expand,
         color
      );
      this.renderSolidFillPart(
         matrices,
         buffer,
         root,
         model.leftArm,
         -1.0f,
         -2.0f,
         -2.0f,
         4.0f,
         12.0f,
         4.0f,
         expand,
         color
      );
      this.renderSolidFillPart(
         matrices,
         buffer,
         root,
         model.rightLeg,
         -2.0f,
         0.0f,
         -2.0f,
         4.0f,
         12.0f,
         4.0f,
         expand,
         color
      );
      this.renderSolidFillPart(
         matrices,
         buffer,
         root,
         model.leftLeg,
         -2.0f,
         0.0f,
         -2.0f,
         4.0f,
         12.0f,
         4.0f,
         expand,
         color
      );
      BufferRenderer.drawWithGlobalProgram(buffer.end());
   }
   public void renderSolidFillPart(
      MatrixStack baseStack,
      BufferBuilder buffer,
      ModelPart root,
      ModelPart part,
      float offX,
      float offY,
      float offZ,
      float width,
      float height,
      float depth,
      float expand,
      int color
   ) {
      baseStack.push();
      root.rotate(baseStack);
      part.rotate(baseStack);
      Matrix4f matrix = baseStack.peek().getPositionMatrix();
      float scale = 0.0625f;
      float expandScale = expand * scale;
      float minX = offX * scale - expandScale;
      float minY = offY * scale - expandScale;
      float minZ = offZ * scale - expandScale;
      float maxX = (offX + width) * scale + expandScale;
      float maxY = (offY + height) * scale + expandScale;
      float maxZ = (offZ + depth) * scale + expandScale;
      this.addSolidQuad(buffer, matrix, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, color);
      this.addSolidQuad(buffer, matrix, minX, minY, maxZ, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, color);
      this.addSolidQuad(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, color);
      this.addSolidQuad(buffer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, minX, minY, maxZ, color);
      this.addSolidQuad(buffer, matrix, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, minX, minY, minZ, color);
      this.addSolidQuad(buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, color);
      baseStack.pop();
   }
   public void renderFillPart(
      MatrixStack baseStack,
      BufferBuilder buffer,
      ModelPart root,
      ModelPart part,
      float offX,
      float offY,
      float offZ,
      float width,
      float height,
      float depth,
      float expand,
      int color
   ) {
      baseStack.push();
      root.rotate(baseStack);
      part.rotate(baseStack);
      Matrix4f matrix = baseStack.peek().getPositionMatrix();
      float scale = 0.0625f;
      float expandScale = expand * scale;
      float minX = offX * scale - expandScale;
      float minY = offY * scale - expandScale;
      float minZ = offZ * scale - expandScale;
      float maxX = (offX + width) * scale + expandScale;
      float maxY = (offY + height) * scale + expandScale;
      float maxZ = (offZ + depth) * scale + expandScale;
      this.addQuad(buffer, matrix, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, color);
      this.addQuad(buffer, matrix, minX, minY, maxZ, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, color);
      this.addQuad(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, color);
      this.addQuad(buffer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, minX, minY, maxZ, color);
      this.addQuad(buffer, matrix, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, minX, minY, minZ, color);
      this.addQuad(buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, color);
      baseStack.pop();
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
      int color
   ) {
      int r = ObsidianComponent_olorUtil.red(color);
      int g = ObsidianComponent_olorUtil.green(color);
      int b = ObsidianComponent_olorUtil.blue(color);
      int a = ObsidianComponent_olorUtil.alpha(color);
      float u1 = this.waveU(x1, y1, z1);
      float v1 = this.waveV(x1, y1, z1);
      float u2 = this.waveU(x2, y2, z2);
      float v2 = this.waveV(x2, y2, z2);
      float u3 = this.waveU(x3, y3, z3);
      float v3 = this.waveV(x3, y3, z3);
      float u4 = this.waveU(x4, y4, z4);
      float v4 = this.waveV(x4, y4, z4);
      buffer.vertex(matrix, x1, y1, z1).texture(u1, v1).color(r, g, b, a);
      buffer.vertex(matrix, x2, y2, z2).texture(u2, v2).color(r, g, b, a);
      buffer.vertex(matrix, x3, y3, z3).texture(u3, v3).color(r, g, b, a);
      buffer.vertex(matrix, x4, y4, z4).texture(u4, v4).color(r, g, b, a);
   }
   public void addSolidQuad(
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
      int color
   ) {
      int r = ObsidianComponent_olorUtil.red(color);
      int g = ObsidianComponent_olorUtil.green(color);
      int b = ObsidianComponent_olorUtil.blue(color);
      int a = ObsidianComponent_olorUtil.alpha(color);
      buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a);
      buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a);
      buffer.vertex(matrix, x3, y3, z3).color(r, g, b, a);
      buffer.vertex(matrix, x4, y4, z4).color(r, g, b, a);
   }
   public float waveU(float x, float y, float z) {
      return x * 1.149999976158142f + z * 0.7200000286102295f;
   }
   public float waveV(float x, float y, float z) {
      return y * 1.0499999523162842f
         - z * 0.3799999952316284f
         + x * 0.18000000715255737f;
   }
   public void renderOutlineModel(MatrixStack matrices, BipedEntityModel<?> model, float expand, int color) {
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      RenderSystem.lineWidth(0.5f);
      if (this.glow.isState()) {
         RenderSystem.blendFuncSeparate(
            770,
            1,
            1,
            0
         );
         int layers = Math.max(1, Math.round(this.glowLayers.get()));
         float intensity = Math.max(1.0f, this.glowIntensity.get());

         for (int index = layers; index >= (1); index--) {
            float layerExpand = expand + (float)index * 0.5f * intensity;
            float alphaMul = 1.0f
               / (float)(index + (1))
               * 0.699999988079071f;
            int alpha = Math.max(
               1, Math.min(255, Math.round((float)ObsidianComponent_olorUtil.alpha(color) * alphaMul))
            );
            this.drawOutlineParts(matrices, model, layerExpand, this.withAlpha(color, alpha));
         }
      }

      RenderSystem.defaultBlendFunc();
      this.drawOutlineParts(matrices, model, expand, color);
      GL11.glDisable(2848);
   }
   public void drawOutlineParts(MatrixStack matrices, BipedEntityModel<?> model, float expand, int color) {
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
      ModelPart root = model.getRootPart();
      this.renderPartOutlineLines(
         matrices,
         buffer,
         root,
         model.head,
         -4.0f,
         -8.0f,
         -4.0f,
         8.0f,
         8.0f,
         8.0f,
         expand,
         color
      );
      this.renderPartOutlineLines(
         matrices,
         buffer,
         root,
         model.body,
         -4.0f,
         0.0f,
         -2.0f,
         8.0f,
         12.0f,
         4.0f,
         expand,
         color
      );
      this.renderPartOutlineLines(
         matrices,
         buffer,
         root,
         model.rightArm,
         -3.0f,
         -2.0f,
         -2.0f,
         4.0f,
         12.0f,
         4.0f,
         expand,
         color
      );
      this.renderPartOutlineLines(
         matrices,
         buffer,
         root,
         model.leftArm,
         -1.0f,
         -2.0f,
         -2.0f,
         4.0f,
         12.0f,
         4.0f,
         expand,
         color
      );
      this.renderPartOutlineLines(
         matrices,
         buffer,
         root,
         model.rightLeg,
         -2.0f,
         0.0f,
         -2.0f,
         4.0f,
         12.0f,
         4.0f,
         expand,
         color
      );
      this.renderPartOutlineLines(
         matrices,
         buffer,
         root,
         model.leftLeg,
         -2.0f,
         0.0f,
         -2.0f,
         4.0f,
         12.0f,
         4.0F,
         expand,
         color
      );
      BufferRenderer.drawWithGlobalProgram(buffer.end());
   }
   public void renderPartOutlineLines(
      MatrixStack baseStack,
      BufferBuilder buffer,
      ModelPart root,
      ModelPart part,
      float offX,
      float offY,
      float offZ,
      float width,
      float height,
      float depth,
      float expand,
      int color
   ) {
      baseStack.push();
      root.rotate(baseStack);
      part.rotate(baseStack);
      float scale = 0.0625f;
      float expandScale = expand * scale;
      float minX = offX * scale - expandScale;
      float minY = offY * scale - expandScale;
      float minZ = offZ * scale - expandScale;
      float maxX = (offX + width) * scale + expandScale;
      float maxY = (offY + height) * scale + expandScale;
      float maxZ = (offZ + depth) * scale + expandScale;
      Matrix4f matrix = baseStack.peek().getPositionMatrix();
      this.addLine(buffer, matrix, minX, minY, minZ, maxX, minY, minZ, color);
      this.addLine(buffer, matrix, maxX, minY, minZ, maxX, minY, maxZ, color);
      this.addLine(buffer, matrix, maxX, minY, maxZ, minX, minY, maxZ, color);
      this.addLine(buffer, matrix, minX, minY, maxZ, minX, minY, minZ, color);
      this.addLine(buffer, matrix, minX, maxY, minZ, maxX, maxY, minZ, color);
      this.addLine(buffer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, color);
      this.addLine(buffer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, color);
      this.addLine(buffer, matrix, minX, maxY, maxZ, minX, maxY, minZ, color);
      this.addLine(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, color);
      this.addLine(buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ, color);
      this.addLine(buffer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, color);
      this.addLine(buffer, matrix, minX, minY, maxZ, minX, maxY, maxZ, color);
      baseStack.pop();
   }
   public void addLine(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, int color) {
      int r = ObsidianComponent_olorUtil.red(color);
      int g = ObsidianComponent_olorUtil.green(color);
      int b = ObsidianComponent_olorUtil.blue(color);
      int a = ObsidianComponent_olorUtil.alpha(color);
      buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a);
      buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a);
   }
   public void setUniform(ShaderProgram shader, String name, float value) {
      GlUniform uniform = shader.getUniform(name);
      if (uniform != null) {
         uniform.set(value);
      }
   }

   public boolean affects(PlayerEntity player) {
      if (!this.isEnable() || player == null || !player.isAlive()) {
         return false;
      } else if (player != mc.player) {
         return this.isFriend(player) ? this.rendering.is("Друзей") : this.rendering.is("Игроков");
      } else {
         return (this.rendering.is("Себя") && mc.options.getPerspective() != Perspective.FIRST_PERSON);
      }
   }

   public boolean shouldHideBaseModel(PlayerEntity player) {
      return (this.hideOriginal.isState() && this.affects(player));
   }

   public boolean shouldHideItemsAndCape(PlayerEntity player) {
      return (this.hideItemsAndCape.isState() && this.affects(player));
   }

   public boolean shouldUseOutlineAssist(PlayerEntity player) {
      return this.affects(player);
   }

   public boolean shouldHideOutlineFramebuffer() {
      return (this.isEnable() && this.hasOutlineAssistTargets());
   }

   public int resolveFillColor(PlayerEntity player) {
      return this.applyPulse(this.baseFillColor(player));
   }

   public int resolveOutlineColor(PlayerEntity player) {
      return this.applyPulse(this.baseOutlineColor(player));
   }
   public int baseFillColor(PlayerEntity player) {
      return this.isFriend(player)
         ? FRIEND_FILL_COLOR
         : this.vividWithAlpha(
            ObsidianComponent_olorUtil.getThemeColor(),
            1.1799999475479126f,
            1.1200000047683716f,
            130
         );
   }
   public int baseOutlineColor(PlayerEntity player) {
      return this.isFriend(player)
         ? FRIEND_OUTLINE_COLOR
         : this.vividWithAlpha(
            ObsidianComponent_olorUtil.getThemeColor(),
            1.1200000047683716f,
            1.0800000429153442f,
            255
         );
   }
   public int applyPulse(int color) {
      if (!this.pulse.isState()) {
         return color;
      } else {
         float elapsedSeconds = (float)(System.currentTimeMillis() - this.startTime) / 1000.0f;
         float pulseValue = (float)(
            (
                  Math.sin(
                        (double)(elapsedSeconds * this.pulseSpeed.get())
                           * 3.141592653589793
                     )
                     + 1.0
               )
               * 0.5
         );
         float alphaMul = 0.6499999761581421f + 0.3499999940395355f * pulseValue;
         return ObsidianComponent_olorUtil.multAlpha(color, alphaMul);
      }
   }
   public int vividWithAlpha(int color, float saturationBoost, float brightnessBoost, int alpha) {
      float[] hsb = Color.RGBtoHSB(ObsidianComponent_olorUtil.red(color), ObsidianComponent_olorUtil.green(color), ObsidianComponent_olorUtil.blue(color), null);
      float saturation = MathHelper.clamp(
         hsb[1] * saturationBoost,
         0.0f,
         1.0f
      );
      float brightness = MathHelper.clamp(
         Math.max(hsb[2], 0.800000011920929f) * brightnessBoost,
         0.0f,
         1.0f
      );
      int rgb = Color.HSBtoRGB(hsb[0], saturation, brightness);
      return ObsidianComponent_olorUtil.rgba(ObsidianComponent_olorUtil.red(rgb), ObsidianComponent_olorUtil.green(rgb), ObsidianComponent_olorUtil.blue(rgb), alpha);
   }
   public int withAlpha(int color, int alpha) {
      return color & 16777215 | (alpha & (255)) << (24);
   }
   public boolean isFriend(PlayerEntity player) {
      return (Obsidian.INSTANCE != null
            && Obsidian.INSTANCE.friendStorage != null
            && Obsidian.INSTANCE.friendStorage.isFriend(player.getName().getString()));
   }
   public boolean hasOutlineAssistTargets() {
      if (this.isEnable() && mc.world != null && mc.player != null) {
         for (PlayerEntity player : mc.world.getPlayers()) {
            if (this.shouldUseOutlineAssist(player)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }
   public boolean tryEnsureOutlineProcessor() {
      if (mc.worldRenderer == null) {
         this.outlineAssistReady = false;
         return false;
      } else if (mc.worldRenderer.getEntityOutlinesFramebuffer() != null) {
         this.outlineAssistReady = true;
         return true;
      } else {
         try {
            mc.worldRenderer.loadEntityOutlinePostProcessor();
         } catch (Exception var2) {
         }

         this.outlineAssistReady = (mc.worldRenderer.getEntityOutlinesFramebuffer() != null);
         if (!this.outlineAssistReady) {
            this.nextOutlineRetryAt = System.currentTimeMillis() + (3000L);
         }

         return this.outlineAssistReady;
      }
   }
}
