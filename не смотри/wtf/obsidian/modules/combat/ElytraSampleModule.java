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
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class ElytraSampleModule extends Module {
   
   private static final Identifier GLOW_TEXTURE = Identifier.of("obsidian", "textures/trajectories/glow.png");
   public static final float BOX_GLOW_OUTER_THICKNESS = 0.17F;
   public static final float BOX_GLOW_MID_THICKNESS = 0.13F;
   public static final float BOX_GLOW_CORE_THICKNESS = 0.11F;
   public static final float BOX_GLOW_LINE_U = 0.4F;
   public static final int[][] BOX_EDGES;
   public Box smoothedPredictionBox;
   public LivingEntity smoothedTarget;
   public final SliderSetting forward = new SliderSetting(
      "Сила предикта",
      3.0f,
      1.0f,
      6.0f,
      1.0f
   );

   public ElytraSampleModule() {
      super("ElytraSample", "Таргетит игрока на элитрах", Category.COMBAT);
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.forward;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onRender3D(RenderEvent event) {
      if (mc.player == null || mc.world == null) {
         this.resetPredictionSmoothing();
      } else if (!mc.player.isGliding()) {
         this.resetPredictionSmoothing();
      } else {
         KillAuraModule aura = ModuleManager.aura;
         if (aura != null && aura.isEnable()) {
            LivingEntity target = aura.getTarget();
            if (target != null && target.isAlive() && target.isGliding()) {
               Box predictedBox = this.buildPredictedBox(target);
               this.renderPredictionBox(event, this.smoothPredictionBox(target, predictedBox));
            } else {
               this.resetPredictionSmoothing();
            }
         } else {
            this.resetPredictionSmoothing();
         }
      }
   }

   @Override
   public void onDisable() {
      this.resetPredictionSmoothing();
      super.onDisable();
   }
   public Box smoothPredictionBox(LivingEntity target, Box predictedBox) {
      if (this.smoothedPredictionBox != null
         && this.smoothedTarget == target
         && !(
            this.smoothedPredictionBox.getCenter().squaredDistanceTo(predictedBox.getCenter())
               > 144.0
         )) {
         double distance = Math.sqrt(this.smoothedPredictionBox.getCenter().squaredDistanceTo(predictedBox.getCenter()));
         double smoothFactor = MathHelper.clamp(
            0.08
               + distance * 0.035,
            0.08,
            0.18
         );
         this.smoothedPredictionBox = this.lerpBox(this.smoothedPredictionBox, predictedBox, smoothFactor);
         return this.smoothedPredictionBox;
      } else {
         this.smoothedPredictionBox = predictedBox;
         this.smoothedTarget = target;
         return predictedBox;
      }
   }
   public void resetPredictionSmoothing() {
      this.smoothedPredictionBox = null;
      this.smoothedTarget = null;
   }
   public Box buildPredictedBox(LivingEntity target) {
      Box currentBox = target.getBoundingBox();
      Vec3d predictedCenter = ObsidianComponent_215.predict(
         target, currentBox.getCenter(), Math.max(0, this.forward.getValue().intValue())
      );
      Vec3d offset = predictedCenter.subtract(currentBox.getCenter());
      return currentBox.offset(offset);
   }
   public Box lerpBox(Box from, Box to, double factor) {
      return new Box(
         MathHelper.lerp(factor, from.minX, to.minX),
         MathHelper.lerp(factor, from.minY, to.minY),
         MathHelper.lerp(factor, from.minZ, to.minZ),
         MathHelper.lerp(factor, from.maxX, to.maxX),
         MathHelper.lerp(factor, from.maxY, to.maxY),
         MathHelper.lerp(factor, from.maxZ, to.maxZ)
      );
   }
   public void renderPredictionBox(RenderEvent event, Box box) {
      MatrixStack matrices = event.getMatrices();
      Camera camera = event.getCamera();
      Vec3d cameraPos = camera.getPos();
      int themeColor = ObsidianComponent_olorUtil.getThemeColor();
      int outerColor = ObsidianComponent_olorUtil.setAlphaColor(themeColor, 118);
      int midColor = ObsidianComponent_olorUtil.setAlphaColor(
         ObsidianComponent_olorUtil.interpolateColor(themeColor, -1, 0.23999999463558197f),
         210
      );
      int coreColor = ObsidianComponent_olorUtil.setAlphaColor(
         ObsidianComponent_olorUtil.interpolateColor(themeColor, -1, 0.6F), 255
      );
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(770, 1);
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.setShaderTexture(0, GLOW_TEXTURE);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      BufferBuilder quads = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      this.addGlowBox(quads, matrix, cameraPos, box, outerColor, 0.17000000178813934f);
      this.addGlowBox(quads, matrix, cameraPos, box, midColor, 0.12999999523162842f);
      this.addGlowBox(quads, matrix, cameraPos, box, coreColor, 0.10999999940395355f);
      BufferRenderer.drawWithGlobalProgram(quads.end());
      RenderSystem.setShaderTexture(0, 0);
      RenderSystem.defaultBlendFunc();
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }
   public void addGlowBox(BufferBuilder buffer, Matrix4f matrix, Vec3d camera, Box box, int color, float thickness) {
      Vec3d[] corners = this.getBoxVectors(box);
      int[][] var8 = BOX_EDGES;
      int var9 = var8.length;

      for (int var10 = 0; var10 < var9; var10++) {
         int[] edge = var8[var10];
         this.addGlowEdge(buffer, matrix, camera, corners[edge[0]], corners[edge[1]], color, thickness);
      }
   }
   public void addGlowEdge(BufferBuilder buffer, Matrix4f matrix, Vec3d camera, Vec3d start, Vec3d end, int color, float thickness) {
      Vec3d edge = end.subtract(start);
      if (!(edge.lengthSquared() <= 1e-06)) {
         Vec3d direction = edge.normalize();
         double overlap = (double)(thickness * 0.2199999988079071f);
         start = start.subtract(direction.multiply(overlap));
         end = end.add(direction.multiply(overlap));
         edge = end.subtract(start);
         Vec3d center = start.add(end).multiply(0.5);
         Vec3d toCamera = camera.subtract(center);
         if (toCamera.lengthSquared() <= 1e-06) {
            toCamera = new Vec3d(
               0.0,
               1.0,
               0.0
            );
         }

         Vec3d side = edge.crossProduct(toCamera);
         if (side.lengthSquared() <= 1e-06) {
            side = edge.crossProduct(
               new Vec3d(
                  0.0,
                  1.0,
                  0.0
               )
            );
            if (side.lengthSquared() <= 1e-06) {
               side = edge.crossProduct(
                  new Vec3d(
                     1.0,
                     0.0,
                     0.0
                  )
               );
            }
         }

         side = side.normalize().multiply((double)(thickness * 0.47999998927116394f));
         Vec3d p1 = start.add(side).subtract(camera);
         Vec3d p2 = start.subtract(side).subtract(camera);
         Vec3d p3 = end.subtract(side).subtract(camera);
         Vec3d p4 = end.add(side).subtract(camera);
         float[] rgba = ObsidianComponent_olorUtil.rgba(color);
         buffer.vertex(matrix, (float)p1.x, (float)p1.y, (float)p1.z)
            .texture(0.4000000059604645f, 0.0f)
            .color(
               rgba[0],
               rgba[1],
               rgba[2],
               rgba[3]
            );
         buffer.vertex(matrix, (float)p2.x, (float)p2.y, (float)p2.z)
            .texture(0.4000000059604645f, 1.0f)
            .color(
               rgba[0],
               rgba[1],
               rgba[2],
               rgba[3]
            );
         buffer.vertex(matrix, (float)p3.x, (float)p3.y, (float)p3.z)
            .texture(0.4000000059604645f, 1.0f)
            .color(
               rgba[0], rgba[1], rgba[2], rgba[3]
            );
         buffer.vertex(matrix, (float)p4.x, (float)p4.y, (float)p4.z)
            .texture(0.4000000059604645f, 0.0f)
            .color(
               rgba[0],
               rgba[1],
               rgba[2],
               rgba[3]
            );
      }
   }
   public Vec3d[] getBoxVectors(Box box) {
      Vec3d[] var10000 = new Vec3d[8];
      var10000[0] = new Vec3d(box.minX, box.minY, box.minZ);
      var10000[1] = new Vec3d(box.minX, box.maxY, box.minZ);
      var10000[2] = new Vec3d(box.maxX, box.minY, box.minZ);
      var10000[3] = new Vec3d(box.maxX, box.maxY, box.minZ);
      var10000[4] = new Vec3d(box.minX, box.minY, box.maxZ);
      var10000[5] = new Vec3d(box.minX, box.maxY, box.maxZ);
      var10000[6] = new Vec3d(box.maxX, box.minY, box.maxZ);
      var10000[7] = new Vec3d(box.maxX, box.maxY, box.maxZ);
      return var10000;
   }

   static {
      int[][] var10000 = new int[12][];
      int var10002 = 0;
      int[] var10003 = new int[2];
      var10003[0] = 0;
      var10003[1] = 2;
      var10000[var10002] = var10003;
      var10002 = 1;
      var10003 = new int[2];
      var10003[0] = 2;
      var10003[1] = 6;
      var10000[var10002] = var10003;
      var10003 = new int[2];
      var10003[0] = 6;
      var10003[1] = 4;
      var10000[2] = var10003;
      var10002 = 3;
      var10003 = new int[2];
      var10003[0] = 4;
      var10003[1] = 0;
      var10000[var10002] = var10003;
      var10002 = 4;
      var10003 = new int[2];
      var10003[0] = 1;
      var10003[1] = 3;
      var10000[var10002] = var10003;
      var10002 = 5;
      var10003 = new int[2];
      var10003[0] = 3;
      var10003[1] = 7;
      var10000[var10002] = var10003;
      var10002 = 6;
      var10003 = new int[2];
      var10003[0] = 7;
      var10003[1] = 5;
      var10000[var10002] = var10003;
      var10002 = 7;
      var10003 = new int[2];
      var10003[0] = 5;
      var10003[1] = 1;
      var10000[var10002] = var10003;
      var10002 = 8;
      var10003 = new int[2];
      var10003[0] = 0;
      var10003[1] = 1;
      var10000[var10002] = var10003;
      var10002 = 9;
      var10003 = new int[2];
      var10003[0] = 2;
      var10003[1] = 3;
      var10000[var10002] = var10003;
      var10002 = 10;
      var10003 = new int[2];
      var10003[0] = 6;
      var10003[1] = 7;
      var10000[var10002] = var10003;
      var10002 = 11;
      var10003 = new int[2];
      var10003[0] = 4;
      var10003[1] = 5;
      var10000[var10002] = var10003;
      BOX_EDGES = var10000;
   }
   public static ElytraSampleModule INSTANCE = new ElytraSampleModule();

}
