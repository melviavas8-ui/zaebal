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

























import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class CubesModule extends Module {
   
   private static final Identifier GLOW_TEX = Identifier.of("obsidian", "textures/particle/bloom.png");
   public static final float SPAWN_RADIUS = 12.0F;
   public static final float PARTICLE_SIZE = 0.18F;
   public static final float PARTICLE_SPEED = 0.25F;
   public static final float GLOW_INTENSITY = 1.7F;
   public static final float MAX_RENDER_DISTANCE_SQ = 900.0F;
   public static final byte[][] CUBE_EDGES;
   public static final byte[][] TRIANGLE_EDGES;
   public static final float[] GLOW_SCALES;
   public static final float[] GLOW_ALPHA_SCALES;
   public final ModeSetting animation;
   public final ModeSetting shape;
   public final SliderSetting count;
   public final SliderSetting size;
   public final SliderSetting speed;
   public final List<ObsidianComponent_427> cubes;
   public final List<ObsidianComponent_427> visibleCubes;
   public final Random random;
   public boolean lastAttackPressed;
   public float cr;
   public float cg;
   public float cb;
   public int updateCounter;

   public CubesModule() {
      super("Cubes", "3D Кубы по миру", Category.RENDER);
      String[] var10005 = new String[2];
      var10005[0] = "Разлет";
      var10005[1] = "Падение";
      this.animation = new ModeSetting("Анимация", "Разлет", var10005);
      var10005 = new String[2];
      var10005[0] = "Кубы";
      var10005[1] = "Треугольники";
      this.shape = new ModeSetting("Форма", "Кубы", var10005);
      this.count = new SliderSetting(
         "Количество",
         30.0f,
         5.0f,
         100.0f,
         1.0f
      );
      this.size = new SliderSetting(
         "Размер",
         1.0f,
         0.10000000149011612f,
         3.0f,
         0.10000000149011612f
      );
      this.speed = new SliderSetting(
         "Скорость",
         1.0f,
         0.10000000149011612f,
         5.0f,
         0.10000000149011612f
      );
      this.cubes = new ArrayList<>();
      this.visibleCubes = new ArrayList<>();
      this.random = new Random();
      this.updateCounter = 0;
      Setting[] var10001 = new Setting[5];
      var10001[0] = this.animation;
      var10001[1] = this.shape;
      var10001[2] = this.count;
      var10001[3] = this.size;
      var10001[4] = this.speed;
      this.addSettings(var10001);
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.cubes.clear();
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.cubes.clear();
   }

   @EventTarget
   public void onRender3D(RenderEvent event) {
      if (mc.player != null && mc.world != null) {
         boolean attackPressed = mc.options.attackKey.isPressed();
         if (attackPressed && !this.lastAttackPressed) {
            this.applyHitImpulseFromCrosshair(event.getCamera());
         }

         this.lastAttackPressed = attackPressed;
         this.updateCounter += 1;
         if (this.updateCounter % 2 == 0) {
            this.updateCubes();
         }

         this.renderCubes(event);
      }
   }

   @EventTarget
   public void onAttack(Event_358 event) {
      if (mc.gameRenderer != null && mc.gameRenderer.getCamera() != null) {
         this.applyHitImpulseFromCrosshair(mc.gameRenderer.getCamera());
      }
   }
   public void applyHitImpulseFromCrosshair(Camera camera) {
      if (!this.cubes.isEmpty() && camera != null) {
         Vec3d origin = camera.getPos();
         float yaw = (float)Math.toRadians((double)camera.getYaw());
         float pitch = (float)Math.toRadians((double)camera.getPitch());
         double dirX = (double)(-MathHelper.sin(yaw) * MathHelper.cos(pitch));
         double dirY = (double)(-MathHelper.sin(pitch));
         double dirZ = (double)(MathHelper.cos(yaw) * MathHelper.cos(pitch));
         ObsidianComponent_427 best = null;
         double bestT = 179769313486231570814527423731704356798070567525844996598917476803157260780028538760589558632766878171540458953514382464234321326889464182768467546703537516986049910576551282076245490090389328944075868508455133942304583236903222948165808559332123348274797826204144723168738177180919299881250404026184124858368.0;
         int i = 0;

         for (int sz = this.cubes.size(); i < sz; i++) {
            ObsidianComponent_427 p = this.cubes.get(i);
            double opX = p.x - origin.x;
            double opY = p.y - origin.y;
            double opZ = p.z - origin.z;
            double t = opX * dirX + opY * dirY + opZ * dirZ;
            if (!(t < 0.0)
               && !(t > 128.0)) {
               double closestX = origin.x + dirX * t;
               double closestY = origin.y + dirY * t;
               double closestZ = origin.z + dirZ * t;
               double dx = p.x - closestX;
               double dy = p.y - closestY;
               double dz = p.z - closestZ;
               double distSq = dx * dx + dy * dy + dz * dz;
               if (!(distSq > 1.32) && !(t >= bestT)) {
                  bestT = t;
                  best = p;
               }
            }
         }

         if (best != null) {
            double force = 0.08 * (double)this.speed.get();
            best.vx = (float)((double)best.vx + dirX * force);
            best.vy = (float)((double)best.vy + dirY * force + 0.02);
            best.vz = (float)((double)best.vz + dirZ * force);
         }
      }
   }
   public void updateCubes() {
      int target = (int)this.count.get();
      int currentSize = this.cubes.size();
      if (currentSize < target) {
         int toAdd = Math.min(target - currentSize, 5);

         for (int i = 0; i < toAdd; i++) {
            this.cubes.add(this.spawnCube());
         }
      } else if (currentSize > target) {
         this.cubes.subList(target, currentSize).clear();
      }

      float spd = 0.25f * this.speed.get();
      float maxR = 12.0f;
      boolean falling = this.animation.is("Падение");
      Vec3d playerPos = mc.player.getPos();
      double maxRSq = (double)(maxR * maxR) * 6.25;

      for (int i = this.cubes.size() - (1); i >= 0; i--) {
         ObsidianComponent_427 p = this.cubes.get(i);
         if (falling) {
            p.wobblePhase = p.wobblePhase + 0.05999999865889549f * spd;
            p.x = p.x
               + (double)(p.vx * spd)
               + Math.sin((double)(p.wobblePhase + p.wobbleOffset))
                  * 0.002400000113993883
                  * (double)spd;
            p.y = p.y + (double)(p.vy * spd);
            p.z = p.z
               + (double)(p.vz * spd)
               + Math.cos((double)(p.wobblePhase * 0.800000011920929f + p.wobbleOffset))
                  * 0.0020000000949949026
                  * (double)spd;
            p.vy = Math.max(p.vy - 7.999999797903001e-05f * spd, -0.03200000151991844f);
            p.rotX = p.rotX + p.rotSpeedX * 0.20000000298023224f * spd;
            p.rotY = p.rotY + p.rotSpeedY * 0.20000000298023224f * spd;
            p.rotZ = p.rotZ + p.rotSpeedZ * 0.20000000298023224f * spd;
         } else {
            p.x = p.x + (double)(p.vx * spd);
            p.y = p.y + (double)(p.vy * spd);
            p.z = p.z + (double)(p.vz * spd);
            p.rotX = p.rotX + p.rotSpeedX * spd;
            p.rotY = p.rotY + p.rotSpeedY * spd;
            p.rotZ = p.rotZ + p.rotSpeedZ * spd;
            p.vx = p.vx * 0.9950000047683716f;
            p.vy = p.vy * 0.9950000047683716f;
            p.vz = p.vz * 0.9950000047683716f;
         }

         p.life -= 1;
         double dx = p.x - playerPos.x;
         double dy = p.y - playerPos.y;
         double dz = p.z - playerPos.z;
         double distSq = dx * dx + dy * dy + dz * dz;
         if (p.life <= 0
            || distSq > maxRSq
            || falling && p.y < playerPos.y - 2.5) {
            this.cubes.remove(i);
            this.cubes.add(this.spawnCube());
         }
      }
   }
   public void renderCubes(RenderEvent e) {
      if (mc.player != null) {
         MatrixStack ms = e.getMatrices();
         Vec3d cam = e.getCamera().getPos();
         Camera camera = e.getCamera();
         float s = 0.18000000715255737f * this.size.get();
         float glow = 1.7000000476837158f;
         int baseRGB = ObsidianComponent_olorUtil.getThemeColor();
         this.cr = (float)(baseRGB >> (16) & (255)) / 255.0f;
         this.cg = (float)(baseRGB >> 8 & (255))
            / 255.0f;
         this.cb = (float)(baseRGB & (255)) / 255.0f;
         this.visibleCubes.clear();
         float yaw = (float)Math.toRadians((double)camera.getYaw());
         float pitch = (float)Math.toRadians((double)camera.getPitch());
         double lookX = (double)(-MathHelper.sin(yaw) * MathHelper.cos(pitch));
         double lookY = (double)(-MathHelper.sin(pitch));
         double lookZ = (double)(MathHelper.cos(yaw) * MathHelper.cos(pitch));
         int i = 0;

         for (int sz = this.cubes.size(); i < sz; i++) {
            ObsidianComponent_427 p = this.cubes.get(i);
            double dx = p.x - cam.x;
            double dy = p.y - cam.y;
            double dz = p.z - cam.z;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (!(distSq > 900.0)
               && !(dx * lookX + dy * lookY + dz * lookZ < Double.longBitsToDouble(-4616189618054758400L))) {
               p.renderAlpha = this.getAlpha(p);
               if (!(p.renderAlpha < 0.009999999776482582f)) {
                  this.visibleCubes.add(p);
               }
            }
         }

         if (!this.visibleCubes.isEmpty()) {
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE, SrcFactor.ZERO, DstFactor.ONE);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.setShaderTexture(0, GLOW_TEX);
            this.drawGlowBatch(ms, camera, cam, s, glow);
            RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            boolean isCubes = this.shape.is("Кубы");
            boolean isTriangles = this.shape.is("Треугольники");
            if (isCubes) {
               this.drawCubeFacesBatch(ms, cam, s);
            }

            if (isTriangles) {
               this.drawTriangleFacesBatch(ms, cam, s);
            }

            RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
            if (isCubes) {
               this.drawCubeDashedEdgesBatch(ms, cam, s);
            } else if (isTriangles) {
               this.drawTriangleDashedEdgesBatch(ms, cam, s);
            }

            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
         }
      }
   }
   public void drawGlowBatch(MatrixStack ms, Camera camera, Vec3d cam, float s, float glow) {
      BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      int particleIndex = 0;

      for (int sz = this.visibleCubes.size(); particleIndex < sz; particleIndex++) {
         ObsidianComponent_427 p = this.visibleCubes.get(particleIndex);
         float alpha = p.renderAlpha;
         ms.push();
         ms.translate(p.x - cam.x, p.y - cam.y, p.z - cam.z);
         ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
         ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
         Matrix4f matrix = ms.peek().getPositionMatrix();

         for (int i = 0; i < 3; i++) {
            float scale = s * GLOW_SCALES[i] * glow;
            float a = alpha * GLOW_ALPHA_SCALES[i] * glow;
            float hs = scale * 0.5f;
            builder.vertex(matrix, -hs, hs, 0.0f)
               .texture(0.0f, 1.0f)
               .color(this.cr, this.cg, this.cb, a);
            builder.vertex(matrix, hs, hs, 0.0f)
               .texture(1.0f, 1.0f)
               .color(this.cr, this.cg, this.cb, a);
            builder.vertex(matrix, hs, -hs, 0.0f)
               .texture(1.0f, 0.0f)
               .color(this.cr, this.cg, this.cb, a);
            builder.vertex(matrix, -hs, -hs, 0.0f)
               .texture(0.0f, 0.0f)
               .color(this.cr, this.cg, this.cb, a);
         }

         ms.pop();
      }

      BufferRenderer.drawWithGlobalProgram(builder.end());
   }
   public float getAlpha(ObsidianComponent_427 p) {
      float lifePct = MathHelper.clamp(
         (float)p.life / (float)p.maxLife, 0.0f, 1.0f
      );
      float fadeIn = Math.min(
         1.0f,
         (float)(p.maxLife - p.life) / 20.0f
      );
      return lifePct * fadeIn;
   }
   public void drawCubeFacesBatch(MatrixStack ms, Vec3d cam, float s) {
      if (this.hasFaceRenderableParticles()) {
         BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
         int i = 0;

         for (int sz = this.visibleCubes.size(); i < sz; i++) {
            ObsidianComponent_427 p = this.visibleCubes.get(i);
            float alpha = p.renderAlpha * 0.4000000059604645f;
            if (!(alpha < 0.009999999776482582f)) {
               ms.push();
               ms.translate(p.x - cam.x, p.y - cam.y, p.z - cam.z);
               ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(p.rotX));
               ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(p.rotY));
               ms.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(p.rotZ));
               this.appendCubeFaces(buffer, ms.peek().getPositionMatrix(), s, alpha);
               ms.pop();
            }
         }

         BufferRenderer.drawWithGlobalProgram(buffer.end());
      }
   }
   public void drawTriangleFacesBatch(MatrixStack ms, Vec3d cam, float s) {
      if (this.hasFaceRenderableParticles()) {
         BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
         int i = 0;

         for (int sz = this.visibleCubes.size(); i < sz; i++) {
            ObsidianComponent_427 p = this.visibleCubes.get(i);
            float alpha = p.renderAlpha * 0.4000000059604645f;
            if (!(alpha < 0.009999999776482582f)) {
               ms.push();
               ms.translate(p.x - cam.x, p.y - cam.y, p.z - cam.z);
               ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(p.rotX));
               ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(p.rotY));
               ms.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(p.rotZ));
               this.appendTriangleFaces(buffer, ms.peek().getPositionMatrix(), s, alpha);
               ms.pop();
            }
         }

         BufferRenderer.drawWithGlobalProgram(buffer.end());
      }
   }
   public boolean hasFaceRenderableParticles() {
      int i = 0;

      for (int sz = this.visibleCubes.size(); i < sz; i++) {
         if (this.visibleCubes.get(i).renderAlpha >= 0.02500000037252903f) {
            return true;
         }
      }

      return false;
   }
   public void appendCubeFaces(BufferBuilder buffer, Matrix4f m, float s, float a) {
      buffer.vertex(m, -s, -s, s).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, s, -s, s).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, s, s, s).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, -s, s, s).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, s, -s, -s).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, -s, -s, -s).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, -s, s, -s).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, s, s, -s).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, -s, s, s).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, s, s, s).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, s, s, -s).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, -s, s, -s).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, -s, -s, -s).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, s, -s, -s).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, s, -s, s).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, -s, -s, s).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, s, -s, s).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, s, -s, -s).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, s, s, -s).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, s, s, s).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, -s, -s, -s).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, -s, -s, s).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, -s, s, s).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, -s, s, -s).color(this.cr, this.cg, this.cb, a);
   }
   public void appendTriangleFaces(BufferBuilder buffer, Matrix4f m, float s, float a) {
      float bottom = -s;
      float halfBase = s * 0.8659999966621399f;
      buffer.vertex(m, 0.0f, s, 0.0f)
         .color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, -halfBase, bottom, halfBase).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, halfBase, bottom, halfBase).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, 0.0f, s, 0.0f)
         .color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, halfBase, bottom, halfBase).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, halfBase, bottom, -halfBase).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, 0.0f, s, 0.0f)
         .color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, halfBase, bottom, -halfBase).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, -halfBase, bottom, -halfBase).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, 0.0f, s, 0.0f)
         .color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, -halfBase, bottom, -halfBase).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, -halfBase, bottom, halfBase).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, -halfBase, bottom, halfBase).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, halfBase, bottom, halfBase).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, halfBase, bottom, -halfBase).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, -halfBase, bottom, halfBase).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, halfBase, bottom, -halfBase).color(this.cr, this.cg, this.cb, a);
      buffer.vertex(m, -halfBase, bottom, -halfBase).color(this.cr, this.cg, this.cb, a);
   }
   public void drawCubeDashedEdgesBatch(MatrixStack ms, Vec3d cam, float s) {
      BufferBuilder buf = Tessellator.getInstance().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
      int lineCount = 0;
      int i = 0;

      for (int sz = this.visibleCubes.size(); i < sz; i++) {
         ObsidianComponent_427 p = this.visibleCubes.get(i);
         float alpha = p.renderAlpha;
         ms.push();
         ms.translate(p.x - cam.x, p.y - cam.y, p.z - cam.z);
         ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(p.rotX));
         ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(p.rotY));
         ms.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(p.rotZ));
         lineCount += this.appendCubeDashedEdges(buf, ms.peek().getPositionMatrix(), s, alpha);
         ms.pop();
      }

      if (lineCount > 0) {
         BufferRenderer.drawWithGlobalProgram(buf.end());
      }
   }
   public void drawTriangleDashedEdgesBatch(MatrixStack ms, Vec3d cam, float s) {
      BufferBuilder buf = Tessellator.getInstance().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
      int lineCount = 0;
      int i = 0;

      for (int sz = this.visibleCubes.size(); i < sz; i++) {
         ObsidianComponent_427 p = this.visibleCubes.get(i);
         float alpha = p.renderAlpha;
         ms.push();
         ms.translate(p.x - cam.x, p.y - cam.y, p.z - cam.z);
         ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(p.rotX));
         ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(p.rotY));
         ms.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(p.rotZ));
         lineCount += this.appendTriangleDashedEdges(buf, ms.peek().getPositionMatrix(), s, alpha);
         ms.pop();
      }

      if (lineCount > 0) {
         BufferRenderer.drawWithGlobalProgram(buf.end());
      }
   }
   public int appendCubeDashedEdges(BufferBuilder buf, Matrix4f mat, float s, float alpha) {
      int color = colorToInt(
         Math.min(
            1.0f, this.cr * 1.5f
         ),
         Math.min(1.0f, this.cg * 1.5f),
         Math.min(1.0f, this.cb * 1.5f),
         alpha
      );
      float dashLen = s * 0.30000001192092896f;
      float gapLen = s * 0.25f;
      int lineCount = 0;
      byte[][] var9 = CUBE_EDGES;
      int var10 = var9.length;

      for (int var11 = 0; var11 < var10; var11++) {
         byte[] edge = var9[var11];
         float x1 = (float)edge[0] * s;
         float y1 = (float)edge[1] * s;
         float z1 = (float)edge[2] * s;
         float x2 = (float)edge[3] * s;
         float y2 = (float)edge[4] * s;
         float z2 = (float)edge[5] * s;
         float dx = x2 - x1;
         float dy = y2 - y1;
         float dz = z2 - z1;
         float len = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
         if (!(len < 0.0010000000474974513f)) {
            float nx = dx / len;
            float ny = dy / len;
            float nz = dz / len;
            float pos = 0.0f;

            for (boolean drawing = true;
               pos < len;
               drawing = (!drawing)
            ) {
               float segLen = drawing ? dashLen : gapLen;
               float end = Math.min(pos + segLen, len);
               if (drawing) {
                  buf.vertex(mat, x1 + nx * pos, y1 + ny * pos, z1 + nz * pos).color(color);
                  buf.vertex(mat, x1 + nx * end, y1 + ny * end, z1 + nz * end).color(color);
                  lineCount++;
               }

               pos = end;
            }
         }
      }

      return lineCount;
   }
   public int appendTriangleDashedEdges(BufferBuilder buf, Matrix4f mat, float s, float alpha) {
      int color = colorToInt(
         Math.min(1.0f, this.cr * 1.5f),
         Math.min(1.0f, this.cg * 1.5f),
         Math.min(1.0f, this.cb * 1.5f),
         alpha
      );
      float dashLen = s * 0.30000001192092896f;
      float gapLen = s * 0.25f;
      int lineCount = 0;
      float top = s;
      float bottom = -s;
      float halfBase = s * 0.8659999966621399f;
      byte[][] var12 = TRIANGLE_EDGES;
      int var13 = var12.length;

      for (int var14 = 0; var14 < var13; var14++) {
         byte[] edge = var12[var14];
         float x1 = this.trianglePointX(edge[0], halfBase);
         float y1 = edge[0] == 0 ? top : bottom;
         float z1 = this.trianglePointZ(edge[0], halfBase);
         float x2 = this.trianglePointX(edge[1], halfBase);
         float y2 = edge[1] == 0 ? top : bottom;
         float z2 = this.trianglePointZ(edge[1], halfBase);
         float dx = x2 - x1;
         float dy = y2 - y1;
         float dz = z2 - z1;
         float len = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
         if (!(len < 0.0010000000474974513f)) {
            float nx = dx / len;
            float ny = dy / len;
            float nz = dz / len;
            float pos = 0.0f;

            for (boolean drawing = true;
               pos < len;
               drawing = (!drawing)
            ) {
               float segLen = drawing ? dashLen : gapLen;
               float end = Math.min(pos + segLen, len);
               if (drawing) {
                  buf.vertex(mat, x1 + nx * pos, y1 + ny * pos, z1 + nz * pos).color(color);
                  buf.vertex(mat, x1 + nx * end, y1 + ny * end, z1 + nz * end).color(color);
                  lineCount++;
               }

               pos = end;
            }
         }
      }

      return lineCount;
   }
   public float trianglePointX(int index, float halfBase) {
      return switch (index) {
         case 1, 4 -> -halfBase;
         case 2, 3 -> halfBase;
         default -> 0.0f;
      };
   }
   public float trianglePointZ(int index, float halfBase) {
      return switch (index) {
         case 1, 2 -> halfBase;
         case 3, 4 -> -halfBase;
         default -> 0.0f;
      };
   }
   public ObsidianComponent_427 spawnCube() {
      float r = 12.0f;
      boolean falling = this.animation.is("Падение");
      int life = falling
         ? (260) + this.random.nextInt(220)
         : 420 + this.random.nextInt(420);
      double x = mc.player.getX()
         + (
               this.random.nextDouble() * 2.0
                  - 1.0
            )
            * (double)r;
      double y = falling
         ? mc.player.getY()
            + 4.0
            + this.random.nextDouble() * (double)r * 0.55
         : mc.player.getY()
            + 2.0
            + this.random.nextDouble() * (double)r * 0.8;
      double z = mc.player.getZ()
         + (
               this.random.nextDouble() * 2.0
                  - 1.0
            )
            * (double)r;
      float speedMult = this.speed.get();
      float vx;
      float vy;
      float vz;
      if (falling) {
         vx = (this.random.nextFloat() - 0.5f)
            * 0.00800000037997961f
            * speedMult;
         vy = (
               -0.012000000104308128f
                  - this.random.nextFloat() * 0.012000000104308128f
            )
            * speedMult;
         vz = (this.random.nextFloat() - 0.5f)
            * 0.00800000037997961f
            * speedMult;
      } else {
         float yaw = this.random.nextFloat() * 360.0f;
         float vel = (0.009999999776482582f + this.random.nextFloat() * 0.019999999552965164f)
            * speedMult;
         vx = -MathHelper.sin((float)Math.toRadians((double)yaw)) * vel;
         vz = MathHelper.cos((float)Math.toRadians((double)yaw)) * vel;
         vy = (this.random.nextFloat() - 0.5f)
            * 0.009999999776482582f
            * speedMult;
      }

      return new ObsidianComponent_427(
         x,
         y,
         z,
         vx,
         vy,
         vz,
         this.random.nextFloat() * 360.0f,
         this.random.nextFloat() * 360.0f,
         this.random.nextFloat() * 360.0f,
         (this.random.nextFloat() - 0.5f) * 1.5f,
         (this.random.nextFloat() - 0.5f) * 1.5f,
         (this.random.nextFloat() - 0.5f) * 1.5f,
         life,
         (float)(
            this.random.nextDouble()
               * 3.141592653589793
               * 2.0
         ),
         this.random.nextFloat() * 10.0f
      );
   }
   public static int colorToInt(float r, float g, float b, float a) {
      return (int)(a * 255.0f) << 24
         | (int)(r * 255.0f) << 16
         | (int)(g * 255.0f) << 8
         | (int)(b * 255.0f);
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
      var10002 = 1;
      var10003 = new byte[6];
      var10003[0] = (byte)(1);
      var10003[1] = (byte)(-1);
      var10003[2] = (byte)(-1);
      var10003[3] = (byte)(1);
      var10003[4] = (byte)(-1);
      var10003[5] = (byte)(1);
      var10000[var10002] = var10003;
      var10003 = new byte[6];
      var10003[0] = (byte)(1);
      var10003[1] = (byte)(-1);
      var10003[2] = (byte)(1);
      var10003[3] = (byte)(-1);
      var10003[4] = (byte)(-1);
      var10003[5] = (byte)(1);
      var10000[2] = var10003;
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
      var10003[2] = -1;
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
      var10003[1] = 1;
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
      var10003[0] = (byte)(1);
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
      var10000 = new byte[8][];
      var10002 = 0;
      var10003 = new byte[2];
      var10003[0] = (byte)(0);
      var10003[1] = (byte)(1);
      var10000[var10002] = var10003;
      var10002 = 1;
      var10003 = new byte[2];
      var10003[0] = (byte)(0);
      var10003[1] = (byte)(2);
      var10000[var10002] = var10003;
      var10002 = 2;
      var10003 = new byte[2];
      var10003[0] = (byte)(0);
      var10003[1] = (byte)(3);
      var10000[var10002] = var10003;
      var10002 = 3;
      var10003 = new byte[2];
      var10003[0] = (byte)(0);
      var10003[1] = (byte)(4);
      var10000[var10002] = var10003;
      var10002 = 4;
      var10003 = new byte[2];
      var10003[0] = (byte)(1);
      var10003[1] = (byte)(2);
      var10000[var10002] = var10003;
      var10002 = 5;
      var10003 = new byte[2];
      var10003[0] = (byte)(2);
      var10003[1] = (byte)(3);
      var10000[var10002] = var10003;
      var10002 = 6;
      var10003 = new byte[2];
      var10003[0] = (byte)(3);
      var10003[1] = (byte)(4);
      var10000[var10002] = var10003;
      var10002 = 7;
      var10003 = new byte[2];
      var10003[0] = (byte)(4);
      var10003[1] = (byte)(1);
      var10000[var10002] = var10003;
      TRIANGLE_EDGES = var10000;
      float[] var1 = new float[3];
      var1[0] = 10.0f;
      var1[1] = 6.0f;
      var1[2] = 3.5f;
      GLOW_SCALES = var1;
      float[] var2 = new float[3];
      var2[0] = 0.05999999865889549f;
      var2[1] = 0.14000000059604645f;
      var2[2] = 0.25f;
      GLOW_ALPHA_SCALES = var2;
   }
   public static CubesModule INSTANCE = new CubesModule();

}
