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
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class LineGlyphesModule extends Module {
   
   public static final Vec3i[] AXIS_DIRECTIONS;
   public final SliderSetting glyphsCount = new SliderSetting(
      "Количество Линий",
      70.0f,
      10.0f,
      200.0f,
      1.0f
   );
   public final BooleanSetting slowSpeed = new BooleanSetting("Медленная Скорость", false);
   public final BooleanSetting applyStippleLines = new BooleanSetting("Пунктирные Линии", false);
   public final SliderSetting stippleStepPixels = new SliderSetting(
         "Шаг Пунктира",
         3.0f,
         0.5f,
         20.0f,
         0.5f
      )
      .visible(() -> this.applyStippleLines.isState());
   public final BooleanSetting linesGlowing = new BooleanSetting("Свечение", false);
   private final Random rand = new Random(93882L);
   private final List<ObsidianComponent_137> glyphs = new ArrayList<>();

   public LineGlyphesModule() {
      super("LineGlyphes", "Анимированные линии по миру", Category.RENDER);
      Setting[] var10001 = new Setting[5];
      var10001[0] = this.glyphsCount;
      var10001[1] = this.slowSpeed;
      var10001[2] = this.applyStippleLines;
      var10001[3] = this.stippleStepPixels;
      var10001[4] = this.linesGlowing;
      this.addSettings(var10001);
   }

   @Override
   public void onDisable() {
      this.glyphs.clear();
      super.onDisable();
   }

   @EventTarget
   public void onUpdate(EventTick ignored) {
      if (mc.player != null && mc.world != null) {
         this.updateGlyphs();
         this.maintainGlyphCount();
      } else {
         this.glyphs.clear();
      }
   }

   @EventTarget
   public void onRender3D(RenderEvent event) {
      if (mc.player != null && mc.world != null && event.getCamera() != null) {
         this.drawAllGlyphs(event);
      }
   }
   public void drawAllGlyphs(RenderEvent event) {
      if (!this.glyphs.isEmpty()) {
         List<ObsidianComponent_137> drawable = new ArrayList<>();

         for (ObsidianComponent_137 glyph : this.glyphs) {
            if (glyph.getAlpha() > 0.009999999776482582f
               && glyph.getPointCount() >= 2) {
               drawable.add(glyph);
            }
         }

         if (!drawable.isEmpty()) {
            MatrixStack matrices = event.getMatrices();
            Vec3d cameraPos = event.getCamera().getPos();
            matrices.push();
            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            try {
               this.setupRenderState();
               if (this.linesGlowing.isState()) {
                  this.setAdditiveBlend();
                  this.drawGlyphPass(
                     matrices,
                     drawable,
                     event.getTickDelta(),
                     4.0f,
                     0.07999999821186066f
                  );
                  this.drawGlyphPass(
                     matrices,
                     drawable,
                     event.getTickDelta(),
                     2.5f,
                     0.15000000596046448f
                  );
                  this.drawGlyphPass(
                     matrices,
                     drawable,
                     event.getTickDelta(),
                     1.5f,
                     0.25f
                  );
               }

               this.setNormalBlend();
               this.drawGlyphPass(
                  matrices,
                  drawable,
                  event.getTickDelta(),
                  1.0f,
                  1.0f
               );
            } finally {
               this.restoreRenderState();
               matrices.pop();
            }
         }
      }
   }
   public void drawGlyphPass(MatrixStack matrices, List<ObsidianComponent_137> drawable, float partialTicks, float widthMul, float alphaMul) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      boolean dashed = this.applyStippleLines.isState();
      float dashStep = Math.max(
         0.03999999910593033f,
         this.stippleStepPixels.get() * 0.07999999821186066f
      );
      float gapStep = dashStep * 0.8500000238418579f;
      RenderSystem.lineWidth(
         Math.min(1.149999976158142f * widthMul, 12.0f)
      );
      List<ObsidianComponent_262> vertices = new ArrayList<>();
      int colorIndex = 0;

      for (ObsidianComponent_137 glyph : drawable) {
         float glyphAlpha = glyph.getAlpha() * alphaMul;
         if (!(glyphAlpha <= 0.009999999776482582f)) {
            List<Vec3d> points = glyph.getSmoothedPositions(partialTicks);
            int pointCount = points.size();
            if (pointCount >= 2) {
               for (int i = 0; i < pointCount - 1; i++) {
                  Vec3d from = points.get(i);
                  Vec3d to = points.get(i + (1));
                  float segmentAlpha = glyphAlpha
                     * (
                        0.25f
                           + (float)i / (float)pointCount / 1.75f
                     );
                  int color = this.getThemeColorWithAlpha(segmentAlpha);
                  colorIndex += 180;
                  if (dashed) {
                     this.collectDashedSegment(vertices, from, to, color, dashStep, gapStep);
                  } else {
                     this.collectSegment(vertices, from, to, color);
                  }
               }
            }
         }
      }

      float markerSize = 0.017999999225139618f * widthMul;
      int pointColorIndex = 0;

      for (ObsidianComponent_137 glyphx : drawable) {
         float glyphAlpha = glyphx.getAlpha() * alphaMul;
         if (!(glyphAlpha <= 0.009999999776482582f)) {
            List<Vec3d> points = glyphx.getSmoothedPositions(partialTicks);
            int pointCount = points.size();
            if (pointCount >= 2) {
               for (int ix = 0; ix < pointCount; ix++) {
                  Vec3d pos = points.get(ix);
                  float localAlpha = glyphAlpha
                     * (
                        0.25f
                           + (float)ix / (float)pointCount / 1.75f
                     );
                  int color = this.getThemeColorWithAlpha(localAlpha);
                  pointColorIndex += 180;
                  this.collectPointCross(vertices, pos, color, markerSize);
               }
            }
         }
      }

      if (!vertices.isEmpty()) {
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

         for (ObsidianComponent_262 v : vertices) {
            buffer.vertex(matrix, v.x, v.y, v.z).color(v.r, v.g, v.b, v.a);
         }

         BufferRenderer.drawWithGlobalProgram(buffer.end());
      }

      RenderSystem.lineWidth(1.0f);
   }
   public int getThemeColorWithAlpha(float alphaMul) {
      int themed = ObsidianComponent_olorUtil.getThemeColor();
      float alpha = MathHelper.clamp(
         alphaMul, 0.0f, 1.0f
      );
      int r = themed >> (16) & 255;
      int g = themed >> 8 & (255);
      int b = themed & 255;
      int a = (int)(alpha * 255.0f);
      return a << 24
         | r << 16
         | g << 8
         | b;
   }
   public void collectSegment(List<ObsidianComponent_262> vertices, Vec3d from, Vec3d to, int color) {
      vertices.add(new ObsidianComponent_262((float)from.x, (float)from.y, (float)from.z, color));
      vertices.add(new ObsidianComponent_262((float)to.x, (float)to.y, (float)to.z, color));
   }
   public void collectDashedSegment(List<ObsidianComponent_262> vertices, Vec3d from, Vec3d to, int color, float dashLen, float gapLen) {
      double dx = to.x - from.x;
      double dy = to.y - from.y;
      double dz = to.z - from.z;
      double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (!(length < 0.0001)) {
         double nx = dx / length;
         double ny = dy / length;
         double nz = dz / length;
         double cursor = 0.0;

         for (boolean draw = true;
            cursor < length;
            draw = (!draw)
         ) {
            double step = draw ? (double)dashLen : (double)gapLen;
            double next = Math.min(length, cursor + step);
            if (draw) {
               float sx = (float)(from.x + nx * cursor);
               float sy = (float)(from.y + ny * cursor);
               float sz = (float)(from.z + nz * cursor);
               float ex = (float)(from.x + nx * next);
               float ey = (float)(from.y + ny * next);
               float ez = (float)(from.z + nz * next);
               vertices.add(new ObsidianComponent_262(sx, sy, sz, color));
               vertices.add(new ObsidianComponent_262(ex, ey, ez, color));
            }

            cursor = next;
         }
      }
   }
   public void collectPointCross(List<ObsidianComponent_262> vertices, Vec3d pos, int color, float size) {
      float x = (float)pos.x;
      float y = (float)pos.y;
      float z = (float)pos.z;
      vertices.add(new ObsidianComponent_262(x - size, y, z, color));
      vertices.add(new ObsidianComponent_262(x + size, y, z, color));
      vertices.add(new ObsidianComponent_262(x, y - size, z, color));
      vertices.add(new ObsidianComponent_262(x, y + size, z, color));
      vertices.add(new ObsidianComponent_262(x, y, z - size, color));
      vertices.add(new ObsidianComponent_262(x, y, z + size, color));
   }
   public void setupRenderState() {
      RenderSystem.enableBlend();
      RenderSystem.disableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
   }
   public void setAdditiveBlend() {
      RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE, SrcFactor.ONE, DstFactor.ZERO);
   }
   public void setNormalBlend() {
      RenderSystem.defaultBlendFunc();
   }
   public void restoreRenderState() {
      RenderSystem.lineWidth(1.0f);
      GL11.glDisable(2848);
      RenderSystem.depthMask(true);
      RenderSystem.enableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
      RenderSystem.setShaderColor(
         1.0f,
         1.0f,
         1.0f,
         1.0f
      );
   }
   public void updateGlyphs() {
      Iterator<ObsidianComponent_137> iterator = this.glyphs.iterator();

      while (iterator.hasNext()) {
         ObsidianComponent_137 glyph = iterator.next();
         glyph.update();
         if (glyph.isDead()) {
            iterator.remove();
         }
      }
   }
   public void maintainGlyphCount() {
      int targetCount = this.maxGlyphCount();

      int activeCount;
      for (activeCount = this.countActiveGlyphs(); activeCount < targetCount; activeCount++) {
         this.glyphs
            .add(
               new ObsidianComponent_137(this, this.randomGlyphSpawnPos(), this.randInt(7, 12))
            );
      }

      if (activeCount > targetCount) {
         for (ObsidianComponent_137 glyph : this.glyphs) {
            if (activeCount <= targetCount) {
               break;
            }

            if (!glyph.isRemoving()) {
               glyph.setWantToRemove();
               activeCount--;
            }
         }
      }
   }
   public int countActiveGlyphs() {
      int count = 0;

      for (ObsidianComponent_137 glyph : this.glyphs) {
         if (!glyph.isRemoving()) {
            count++;
         }
      }

      return count;
   }
   public int maxGlyphCount() {
      return Math.max(1, Math.round(this.glyphsCount.get()));
   }
   public Vec3i randomGlyphSpawnPos() {
      int minDistance = 6;
      int maxDistance = 24;
      int minY = 0;
      int maxY = 12;
      Vec3d cameraPos = this.getCameraPos();

      for (int attempt = 0; attempt < (16); attempt++) {
         int distance = this.randInt(6, 25);
         float yawBase = mc.player != null ? mc.player.getYaw() : 0.0f;
         float randomYaw = yawBase
            + this.randFloat(
               -135.0f, 135.0f
            );
         float yawRad = (float)Math.toRadians((double)randomYaw);
         int offsetX = (int)(-(MathHelper.sin(yawRad) * (float)distance));
         int offsetY = this.randInt(0, 13);
         int offsetZ = (int)(MathHelper.cos(yawRad) * (float)distance);
         Vec3i spawn = new Vec3i((int)Math.floor(cameraPos.x) + offsetX, (int)Math.floor(cameraPos.y) + offsetY, (int)Math.floor(cameraPos.z) + offsetZ);
         if (this.isSpawnPosFree(spawn)) {
            return spawn;
         }
      }

      return new Vec3i((int)Math.floor(cameraPos.x), (int)Math.floor(cameraPos.y), (int)Math.floor(cameraPos.z));
   }
   public boolean isSpawnPosFree(Vec3i pos) {
      if (mc.world == null) {
         return true;
      } else {
         BlockPos bp = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
         BlockState state = mc.world.getBlockState(bp);
         if (state.isAir()) {
            return true;
         } else {
            return (!state.getFluidState().isEmpty() ? false : state.getCollisionShape(mc.world, bp).isEmpty());
         }
      }
   }
   public Vec3d getCameraPos() {
      Camera camera = mc.gameRenderer != null ? mc.gameRenderer.getCamera() : null;
      if (camera != null) {
         return camera.getPos();
      } else {
         return mc.player != null ? mc.player.getPos() : Vec3d.ZERO;
      }
   }
   public Vec3i randomAxisDirection() {
      return AXIS_DIRECTIONS[this.rand.nextInt(AXIS_DIRECTIONS.length)];
   }
   public Vec3i nextOrthogonalDirection(Vec3i previousDirection) {
      for (int i = 0; i < 12; i++) {
         Vec3i candidate = this.randomAxisDirection();
         int dot = candidate.getX() * previousDirection.getX() + candidate.getY() * previousDirection.getY() + candidate.getZ() * previousDirection.getZ();
         if (dot == 0) {
            return candidate;
         }
      }

      return this.randomAxisDirection();
   }
   public int randInt(int minInclusive, int maxExclusive) {
      return maxExclusive <= minInclusive ? minInclusive : this.rand.nextInt(maxExclusive - minInclusive) + minInclusive;
   }
   public float randFloat(float minInclusive, float maxInclusive) {
      return maxInclusive <= minInclusive ? minInclusive : minInclusive + this.rand.nextFloat() * (maxInclusive - minInclusive);
   }
   public float moveAdvanceFromTicks(int ticksSet, int ticksLeft, float partialTicks) {
      if (ticksSet <= 0) {
         return 1.0f;
      } else {
         float progress = 1.0f - ((float)ticksLeft - partialTicks) / (float)ticksSet;
         return MathHelper.clamp(progress, 0.0f, 1.0f);
      }
   }
   public static double lerp(double start, double end, double delta) {
      return start + (end - start) * delta;
   }

   static {
      Vec3i[] var10000 = new Vec3i[6];
      var10000[0] = new Vec3i(
         1, 0, 0
      );
      var10000[1] = new Vec3i(
         -1, 0, 0
      );
      var10000[2] = new Vec3i(
         0, 1, 0
      );
      var10000[3] = new Vec3i(
         0, -1, 0
      );
      var10000[4] = new Vec3i(
         0, 0, 1
      );
      var10000[5] = new Vec3i(
         0, 0, -1
      );
      AXIS_DIRECTIONS = var10000;
   }
   public static LineGlyphesModule INSTANCE = new LineGlyphesModule();

}
