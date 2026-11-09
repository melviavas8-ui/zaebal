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
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class BlockOverlayModule extends Module {
   public static BlockOverlayModule INSTANCE = new BlockOverlayModule();
   public final ModeSetting mode;
   public final SliderSetting waveSpeed;
   public final SliderSetting waveScale;
   public final SliderSetting lineSpeed;
   public final SliderSetting lineJitter;
   public final SliderSetting outline;
   public final SliderSetting glow;
   public final SliderSetting fill;
   public final SliderSetting alpha;
   public final SliderSetting smooth;
   public Framebuffer maskBuffer;
   public int fbWidth;
   public int fbHeight;
   public boolean hasMask;
   public BlockPos lastBlockPos;
   public Box displayBox;
   public Box targetBox;
   public int cachedThemeColor1;
   public int cachedThemeColor2;

   public BlockOverlayModule() {
      super("BlockOverlay", "Block overlay shader", Category.RENDER);
      String[] var10005 = new String[2];
      var10005[0] = "Шейдер";
      var10005[1] = "Нитки";
      this.mode = new ModeSetting("Режим", "Шейдер", var10005);
      this.waveSpeed = new SliderSetting(
            "Скорость волн",
            1.2000000476837158f,
            0.10000000149011612f,
            5.0f,
            0.10000000149011612f
         )
         .visible(() -> this.mode.is("Шейдер"));
      this.waveScale = new SliderSetting(
            "Частота волн",
            1.0f,
            1.0f,
            3.0f,
            0.10000000149011612f
         )
         .visible(() -> this.mode.is("Шейдер"));
      this.lineSpeed = new SliderSetting(
            "Скорость нитей",
            1.399999976158142f,
            0.10000000149011612f,
            5.0f,
            0.10000000149011612f
         )
         .visible(() -> this.mode.is("Нитки"));
      this.lineJitter = new SliderSetting(
            "Изгиб нитей",
            0.550000011920929f,
            0.0f,
            1.5f,
            0.009999999776482582f
         )
         .visible(() -> this.mode.is("Нитки"));
      this.outline = new SliderSetting(
         "Ширина обводки",
         1.100000023841858f,
         0.10000000149011612f,
         5.0f,
         0.10000000149011612f
      );
      this.glow = new SliderSetting(
         "Сила свечения",
         1.0f,
         0.0f,
         5.0f,
         0.10000000149011612f
      );
      this.fill = new SliderSetting(
         "Заливка",
         0.6000000238418579f,
         0.0f,
         1.0f,
         0.009999999776482582f
      );
      this.alpha = new SliderSetting(
         "Прозрачность",
         1.0f,
         0.0F,
         1.0f,
         0.009999999776482582f
      );
      this.smooth = new SliderSetting(
         "Плавность",
         0.23999999463558197f,
         0.05000000074505806f,
         0.6000000238418579f,
         0.01F
      );
      this.fbWidth = -1;
      this.fbHeight = -1;
      this.cachedThemeColor1 = -1;
      this.cachedThemeColor2 = -1;
      Setting[] var10001 = new Setting[10];
      var10001[0] = this.mode;
      var10001[1] = this.waveSpeed;
      var10001[2] = this.waveScale;
      var10001[3] = this.lineSpeed;
      var10001[4] = this.lineJitter;
      var10001[5] = this.outline;
      var10001[6] = this.glow;
      var10001[7] = this.fill;
      var10001[8] = this.alpha;
      var10001[9] = this.smooth;
      this.addSettings(var10001);
   }

   @Override
   public void onDisable() {
      this.hasMask = false;
      this.lastBlockPos = null;
      this.displayBox = null;
      this.targetBox = null;
      super.onDisable();
   }

   @EventTarget(
      priority = -100
   )
   public void onRender3D(RenderEvent event) {
      if (mc != null && mc.world != null && mc.player != null) {
         Box worldBox = this.getTargetedBlockBox();
         if (worldBox == null) {
            this.hasMask = false;
            this.lastBlockPos = null;
            this.displayBox = null;
            this.targetBox = null;
         } else {
            if (this.displayBox != null && this.targetBox != null && this.lastBlockPos != null) {
               this.targetBox = worldBox;
               this.displayBox = this.lerpBox(this.displayBox, this.targetBox, this.smooth.get());
            } else {
               this.displayBox = worldBox;
               this.targetBox = worldBox;
            }

            this.lastBlockPos = BlockPos.ofFloored(worldBox.minX, worldBox.minY, worldBox.minZ);
            this.updateCachedThemeColors();
            Vec3d cam = event.getCamera().getPos();
            Box localBox = this.displayBox.offset(-cam.x, -cam.y, -cam.z);
            Matrix4f matrix = event.getMatrices().peek().getPositionMatrix();
            this.ensureMaskBuffer();
            if (this.maskBuffer != null) {
               this.hasMask = true;
               this.maskBuffer
                  .setClearColor(
                     0.0f,
                     0.0f,
                     0.0f,
                     0.0f
                  );
               this.maskBuffer.clear();
               this.copyMainDepthToMask();
               this.maskBuffer.beginWrite(false);
               RenderSystem.enableBlend();
               RenderSystem.defaultBlendFunc();
               RenderSystem.disableCull();
               RenderSystem.enableDepthTest();
               RenderSystem.depthMask(false);
               RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
               this.drawMaskBox(matrix, localBox);
               RenderSystem.depthMask(true);
               RenderSystem.disableDepthTest();
               RenderSystem.enableCull();
               RenderSystem.disableBlend();
               mc.getFramebuffer().beginWrite(false);
               if (this.mode.is("Нитки")) {
                  this.drawAnimatedWeb(matrix, localBox);
               }
            }
         }
      }
   }

   @EventTarget(
      priority = 200
   )
   public void onRender2D(EventRender2D event) {
      if (this.hasMask && this.maskBuffer != null) {
         if (!this.mode.is("Нитки")) {
            ShaderProgram shader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.blockOverlay);
            if (shader != null) {
               boolean lineMode = this.mode.is("Нитки");
               int color1 = this.cachedThemeColor1;
               int color2 = this.cachedThemeColor2;
               mc.getFramebuffer().beginWrite(false);
               RenderSystem.enableBlend();
               RenderSystem.defaultBlendFunc();
               RenderSystem.enableDepthTest();
               RenderSystem.setShader(ObsidianComponent_257.blockOverlay);
               RenderSystem.setShaderTexture(0, this.maskBuffer.getColorAttachment());
               this.setUniform(
                  shader,
                  "texelSize",
                  1.0f / (float)Math.max(1, mc.getWindow().getFramebufferWidth()),
                  1.0f
                     / (float)Math.max(1, mc.getWindow().getFramebufferHeight())
               );
               this.setUniform(shader, "color", ObsidianComponent_olorUtil.redf(color1), ObsidianComponent_olorUtil.greenf(color1), ObsidianComponent_olorUtil.bluef(color1));
               this.setUniform(shader, "color2", ObsidianComponent_olorUtil.redf(color2), ObsidianComponent_olorUtil.greenf(color2), ObsidianComponent_olorUtil.bluef(color2));
               this.setUniform(
                  shader,
                  "time",
                  (float)(System.currentTimeMillis() % (100000L))
                     / 1000.0f
               );
               this.setUniform(shader, "speed", this.waveSpeed.get());
               this.setUniform(shader, "scale", this.waveScale.get());
               this.setUniform(shader, "outline", this.outline.get());
               this.setUniform(shader, "glow", lineMode ? 0.0f : this.glow.get());
               this.setUniform(shader, "fill", lineMode ? 0.0f : this.fill.get());
               this.setUniform(shader, "alpha", lineMode ? 1.0f : this.alpha.get());
               this.setUniform(
                  shader,
                  "outlineOnly",
                  lineMode ? 1.0f : 0.0f
               );
               this.drawFullscreenQuad();
               RenderSystem.enableDepthTest();
               RenderSystem.disableBlend();
               RenderSystem.defaultBlendFunc();
               RenderSystem.setShaderTexture(0, 0);
            }
         }
      }
   }
   public void setUniform(ShaderProgram shader, String name, float value) {
      GlUniform uniform = shader.getUniform(name);
      if (uniform != null) {
         uniform.set(value);
      }
   }
   public void setUniform(ShaderProgram shader, String name, float x, float y) {
      GlUniform uniform = shader.getUniform(name);
      if (uniform != null) {
         uniform.set(x, y);
      }
   }
   public void setUniform(ShaderProgram shader, String name, float x, float y, float z) {
      GlUniform uniform = shader.getUniform(name);
      if (uniform != null) {
         uniform.set(x, y, z);
      }
   }
   public void ensureMaskBuffer() {
      int w = mc.getWindow().getFramebufferWidth();
      int h = mc.getWindow().getFramebufferHeight();
      if (this.maskBuffer == null || this.fbWidth != w || this.fbHeight != h) {
         if (this.maskBuffer != null) {
            this.maskBuffer.delete();
         }

         this.maskBuffer = new SimpleFramebuffer(w, h, true);
         this.fbWidth = w;
         this.fbHeight = h;
      }
   }
   public Box getTargetedBlockBox() {
      HitResult hit = mc.crosshairTarget;
      if (hit instanceof BlockHitResult blockHit && hit.getType() == Type.BLOCK) {
         BlockPos pos = blockHit.getBlockPos();
         if (pos == null) {
            return null;
         }

         if (mc.world.getBlockState(pos).isAir()) {
            return null;
         }

         VoxelShape shape = mc.world.getBlockState(pos).getOutlineShape(mc.world, pos);
         Box box = shape.isEmpty() ? new Box(pos) : shape.getBoundingBox().offset(pos);
         return box.expand(0.002);
      }

      return null;
   }
   public Box lerpBox(Box a, Box b, float t) {
      return new Box(
         a.minX + (b.minX - a.minX) * (double)t,
         a.minY + (b.minY - a.minY) * (double)t,
         a.minZ + (b.minZ - a.minZ) * (double)t,
         a.maxX + (b.maxX - a.maxX) * (double)t,
         a.maxY + (b.maxY - a.maxY) * (double)t,
         a.maxZ + (b.maxZ - a.maxZ) * (double)t
      );
   }
   public void drawAnimatedWeb(Matrix4f matrix, Box box) {
      int strandsPerFace = 5;
      int samples = 18;
      float t = (float)(System.currentTimeMillis() % (100000L))
         / 1000.0f
         * this.lineSpeed.get();
      float lineWidth = 0.0024999999441206455f;
      float bendBase = 0.05999999865889549f
         + this.lineJitter.get() * 0.20000000298023224f;
      int baseAlpha = Math.max(
         20,
         Math.min(255, (int)(this.alpha.get() * 210.0f))
      );
      int themeColor = this.cachedThemeColor1;
      long seed = this.lastBlockPos != null ? this.lastBlockPos.asLong() : 1L;
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      this.drawFilledBox(
         matrix, box, ObsidianComponent_olorUtil.setAlphaColor(themeColor, (int)(this.alpha.get() * this.fill.get() * 170.0f))
      );

      for (int face = 0; face < (6); face++) {
         int[] neighbors = this.faceNeighbors(face);

         for (int strand = 0; strand < strandsPerFace; strand++) {
            int key = face * (1000) + strand * 53;
            int adj = neighbors[strand % neighbors.length];
            double phase = (double)t
                  * (
                     0.95
                        + this.rand01(seed, key + (1))
                           * 0.55
                  )
               + (double)strand * 0.83
               + (double)face * 1.11;
            double edgeT = this.clamp01(
               0.5
                  + Math.sin(
                        phase * 1.37
                           + this.rand01(seed, key + (2))
                              * 6.2831853
                     )
                     * 0.38
            );
            Vec3d pivot = this.edgePoint(box, face, adj, edgeT, 0.0015);
            Vec3d start = this.facePoint(
               box,
               face,
               this.clamp01(
                  0.5
                     + (
                           this.rand01(seed, key + (3))
                              - 0.5
                        )
                        * 0.46
               ),
               this.clamp01(
                  0.5
                     + (
                           this.rand01(seed, key + 4)
                              - 0.5
                        )
                        * 0.46
               ),
               0.0015
            );
            Vec3d end = this.facePoint(
               box,
               adj,
               this.clamp01(
                  0.5
                     + (
                           this.rand01(seed, key + 5)
                              - 0.5
                        )
                        * 0.46
               ),
               this.clamp01(
                  0.5
                     + (
                           this.rand01(seed, key + 6)
                              - 0.5
                        )
                        * 0.46
               ),
               0.0015
            );
            Vec3d[] basisA = this.faceBasis(face);
            Vec3d[] basisB = this.faceBasis(adj);
            Vec3d normalA = this.faceNormal(face);
            Vec3d normalB = this.faceNormal(adj);
            double bendA = (double)bendBase
               * (
                  0.7
                     + this.rand01(seed, key + (7))
               )
               * Math.sin(
                  phase * 1.9
                     + this.rand01(seed, key + (8))
                        * 6.2831853
               );
            double bendB = (double)bendBase
               * (
                  0.7
                     + this.rand01(seed, key + (9))
               )
               * Math.cos(
                  phase * 1.7
                     + this.rand01(seed, key + 10)
                        * 6.2831853
               );
            Vec3d dirA = pivot.subtract(start);
            Vec3d c1a = start.add(dirA.multiply(0.38))
               .add(basisA[0].multiply(bendA))
               .add(
                  basisA[1]
                     .multiply(-bendA * 0.55)
               );
            Vec3d c2a = start.add(dirA.multiply(0.76))
               .add(
                  basisA[0]
                     .multiply(-bendA * 0.65)
               )
               .add(
                  basisA[1]
                     .multiply(bendA * 0.4)
               );
            Vec3d dirB = end.subtract(pivot);
            Vec3d c1b = pivot.add(dirB.multiply(0.24))
               .add(basisB[0].multiply(bendB))
               .add(
                  basisB[1]
                     .multiply(bendB * 0.45)
               );
            Vec3d c2b = pivot.add(dirB.multiply(0.62))
               .add(
                  basisB[0]
                     .multiply(-bendB * 0.7)
               )
               .add(
                  basisB[1]
                     .multiply(-bendB * 0.35)
               );
            int alphaLine = Math.max(
               18,
               Math.min(
                  255,
                  (int)(
                     (double)baseAlpha
                        * (
                           0.74
                              + 0.26
                                 * Math.sin(phase * 2.6)
                        )
                  )
               )
            );
            int color = ObsidianComponent_olorUtil.setAlphaColor(themeColor, alphaLine);
            this.drawBezierRibbon(matrix, start, c1a, c2a, pivot, normalA, samples, color, lineWidth);
            this.drawBezierRibbon(matrix, pivot, c1b, c2b, end, normalB, samples, color, lineWidth);
         }
      }

      RenderSystem.enableDepthTest();
      RenderSystem.depthMask(true);
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }
   public void copyMainDepthToMask() {
      if (this.maskBuffer != null) {
         int readFbo = GL11.glGetInteger(36010);
         int drawFbo = GL11.glGetInteger(36006);
         int w = mc.getWindow().getFramebufferWidth();
         int h = mc.getWindow().getFramebufferHeight();
         GL30.glBindFramebuffer(36008, mc.getFramebuffer().fbo);
         GL30.glBindFramebuffer(36009, this.maskBuffer.fbo);
         GL30.glBlitFramebuffer(
            0,
            0,
            w,
            h,
            0,
            0,
            w,
            h,
            256,
            9728
         );
         GL30.glBindFramebuffer(36008, readFbo);
         GL30.glBindFramebuffer(36009, drawFbo);
      }
   }
   public Vec3d cubicBezier(Vec3d p0, Vec3d p1, Vec3d p2, Vec3d p3, float t) {
      double it = 1.0 - (double)t;
      double it2 = it * it;
      double t2 = (double)(t * t);
      return p0.multiply(it2 * it)
         .add(p1.multiply(3.0 * it2 * (double)t))
         .add(p2.multiply(3.0 * it * t2))
         .add(p3.multiply(t2 * (double)t));
   }
   public void drawBezierRibbon(Matrix4f matrix, Vec3d p0, Vec3d p1, Vec3d p2, Vec3d p3, Vec3d faceNormal, int samples, int color, float halfWidth) {
      Vec3d[] points = new Vec3d[samples + 1];

      for (int s = 0; s <= samples; s++) {
         float u = (float)s / (float)samples;
         points[s] = this.cubicBezier(p0, p1, p2, p3, u);
      }

      BufferBuilder quads = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);

      for (int i = 0; i < samples; i++) {
         Vec3d a = points[i];
         Vec3d b = points[i + 1];
         Vec3d dir = b.subtract(a);
         if (!(dir.lengthSquared() < 1e-06)) {
            Vec3d perp = faceNormal.crossProduct(dir).normalize().multiply((double)halfWidth);
            Vec3d aL = a.add(perp);
            Vec3d aR = a.subtract(perp);
            Vec3d bL = b.add(perp);
            Vec3d bR = b.subtract(perp);
            quads.vertex(matrix, (float)aL.x, (float)aL.y, (float)aL.z).color(color);
            quads.vertex(matrix, (float)aR.x, (float)aR.y, (float)aR.z).color(color);
            quads.vertex(matrix, (float)bR.x, (float)bR.y, (float)bR.z).color(color);
            quads.vertex(matrix, (float)bL.x, (float)bL.y, (float)bL.z).color(color);
         }
      }

      BufferRenderer.drawWithGlobalProgram(quads.end());
   }
   public void updateCachedThemeColors() {
      if (Obsidian.INSTANCE != null && Obsidian.INSTANCE.themeStorage != null && Obsidian.INSTANCE.themeStorage.getThemes() != null) {
         ObsidianComponent_olorPalette theme = Obsidian.INSTANCE.themeStorage.getThemes().getTheme();
         if (theme == null) {
            this.cachedThemeColor1 = ObsidianComponent_olorUtil.getThemeColor(0);
            this.cachedThemeColor2 = ObsidianComponent_olorUtil.getThemeColor(180);
         } else {
            if (!"Rainbow".equals(theme.getName())) {
               int base = theme.color != null && theme.color.length > 0
                  ? theme.color[0]
                  : ObsidianComponent_olorUtil.getThemeColor(0);
               this.cachedThemeColor1 = base;
               this.cachedThemeColor2 = base;
            } else {
               this.cachedThemeColor1 = ObsidianComponent_olorUtil.getThemeColor();
               this.cachedThemeColor2 = ObsidianComponent_olorUtil.getThemeColor(180);
            }
         }
      } else {
         this.cachedThemeColor1 = ObsidianComponent_olorUtil.getThemeColor(0);
         this.cachedThemeColor2 = ObsidianComponent_olorUtil.getThemeColor(180);
      }
   }
   public int[] faceNeighbors(int face) {
      int[] var10000;
      switch (face) {
         case 0:
         case 1:
            var10000 = new int[4];
            var10000[0] = 2;
            var10000[1] = 3;
            var10000[2] = 4;
            var10000[3] = 5;
            break;
         case 2:
         case 3:
            var10000 = new int[4];
            var10000[0] = 0;
            var10000[1] = 1;
            var10000[2] = 4;
            var10000[3] = 5;
            break;
         default:
            var10000 = new int[4];
            var10000[0] = 0;
            var10000[1] = 1;
            var10000[2] = 2;
            var10000[3] = 3;
      }

      return var10000;
   }
   public Vec3d[] faceBasis(int face) {
      Vec3d[] var10000;
      switch (face) {
         case 0:
         case 1:
            var10000 = new Vec3d[2];
            var10000[0] = new Vec3d(
               1.0,
               0.0,
               0.0
            );
            var10000[1] = new Vec3d(
               0.0,
               0.0,
               1.0
            );
            break;
         case 2:
         case 3:
            var10000 = new Vec3d[2];
            var10000[0] = new Vec3d(
               1.0,
               0.0,
               0.0
            );
            var10000[1] = new Vec3d(
               0.0,
               1.0,
               0.0
            );
            break;
         default:
            var10000 = new Vec3d[2];
            var10000[0] = new Vec3d(
               0.0,
               0.0,
               1.0
            );
            var10000[1] = new Vec3d(
               0.0,
               1.0,
               0.0
            );
      }

      return var10000;
   }
   public Vec3d faceNormal(int face) {
      return switch (face) {
         case 0 -> new Vec3d(
         0.0,
         1.0,
         0.0
      );
         case 1 -> new Vec3d(
         0.0,
         Double.longBitsToDouble(-4616189618054758400L),
         0.0
      );
         case 2 -> new Vec3d(
         0.0,
         0.0,
         Double.longBitsToDouble(-4616189618054758400L)
      );
         case 3 -> new Vec3d(
         0.0,
         0.0,
         1.0
      );
         case 4 -> new Vec3d(
         Double.longBitsToDouble(-4616189618054758400L),
         0.0,
         0.0
      );
         default -> new Vec3d(
         1.0,
         0.0,
         0.0
      );
      };
   }
   public Vec3d edgePoint(Box box, int faceA, int faceB, double t, double inset) {
      double x = Double.NaN;
      double y = Double.NaN;
      double z = Double.NaN;
      double[] fixedA = this.faceFixedCoords(box, faceA, inset);
      if (!Double.isNaN(fixedA[0])) {
         x = fixedA[0];
      }

      if (!Double.isNaN(fixedA[1])) {
         y = fixedA[1];
      }

      if (!Double.isNaN(fixedA[2])) {
         z = fixedA[2];
      }

      double[] fixedB = this.faceFixedCoords(box, faceB, inset);
      if (!Double.isNaN(fixedB[0])) {
         x = fixedB[0];
      }

      if (!Double.isNaN(fixedB[1])) {
         y = fixedB[1];
      }

      if (!Double.isNaN(fixedB[2])) {
         z = fixedB[2];
      }

      double tt = this.clamp01(t);
      if (Double.isNaN(x)) {
         x = this.lerp(box.minX, box.maxX, tt);
      }

      if (Double.isNaN(y)) {
         y = this.lerp(box.minY, box.maxY, tt);
      }

      if (Double.isNaN(z)) {
         z = this.lerp(box.minZ, box.maxZ, tt);
      }

      return new Vec3d(x, y, z);
   }
   public double[] faceFixedCoords(Box box, int face, double inset) {
      double[] var10000;
      switch (face) {
         case 0:
            var10000 = new double[3];
            var10000[0] = Double.NaN;
            var10000[1] = box.maxY - inset;
            var10000[2] = Double.NaN;
            break;
         case 1:
            var10000 = new double[3];
            var10000[0] = Double.NaN;
            var10000[1] = box.minY + inset;
            var10000[2] = Double.NaN;
            break;
         case 2:
            var10000 = new double[3];
            var10000[0] = Double.NaN;
            var10000[1] = Double.NaN;
            var10000[2] = box.minZ + inset;
            break;
         case 3:
            var10000 = new double[3];
            var10000[0] = Double.NaN;
            var10000[1] = Double.NaN;
            var10000[2] = box.maxZ - inset;
            break;
         case 4:
            var10000 = new double[3];
            var10000[0] = box.minX + inset;
            var10000[1] = Double.NaN;
            var10000[2] = Double.NaN;
            break;
         default:
            var10000 = new double[3];
            var10000[0] = box.maxX - inset;
            var10000[1] = Double.NaN;
            var10000[2] = Double.NaN;
      }

      return var10000;
   }
   public Vec3d facePoint(Box box, int face, double u, double v, double inset) {
      u = this.clamp01(u);
      v = this.clamp01(v);

      return switch (face) {
         case 0 -> new Vec3d(this.lerp(box.minX, box.maxX, u), box.maxY - inset, this.lerp(box.minZ, box.maxZ, v));
         case 1 -> new Vec3d(this.lerp(box.minX, box.maxX, u), box.minY + inset, this.lerp(box.minZ, box.maxZ, v));
         case 2 -> new Vec3d(this.lerp(box.minX, box.maxX, u), this.lerp(box.minY, box.maxY, v), box.minZ + inset);
         case 3 -> new Vec3d(this.lerp(box.minX, box.maxX, u), this.lerp(box.minY, box.maxY, v), box.maxZ - inset);
         case 4 -> new Vec3d(box.minX + inset, this.lerp(box.minY, box.maxY, v), this.lerp(box.minZ, box.maxZ, u));
         default -> new Vec3d(box.maxX - inset, this.lerp(box.minY, box.maxY, v), this.lerp(box.minZ, box.maxZ, u));
      };
   }
   public double rand01(long seed, int salt) {
      long x = seed
         + (-7046029254386353131L)
            * ((long)salt + (1L));
      x ^= x >>> (30);
      x *= -4658895280553007687L;
      x ^= x >>> 27;
      x *= -7723592293110705685L;
      x ^= x >>> (31);
      return (double)(x & (16777215L))
         / 16777216.0;
   }
   public double lerp(double a, double b, double t) {
      return a + (b - a) * t;
   }
   public double clamp01(double v) {
      return Math.max(
         0.0,
         Math.min(1.0, v)
      );
   }
   public void drawFilledBox(Matrix4f matrix, Box box, int color) {
      BufferBuilder b = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      b.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).color(color);
      b.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).color(color);
      b.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(color);
      b.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).color(color);
      b.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).color(color);
      b.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(color);
      b.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(color);
      b.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(color);
      b.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).color(color);
      b.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).color(color);
      b.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(color);
      b.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).color(color);
      b.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).color(color);
      b.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(color);
      b.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(color);
      b.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(color);
      b.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).color(color);
      b.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).color(color);
      b.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(color);
      b.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).color(color);
      b.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).color(color);
      b.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(color);
      b.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(color);
      b.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(color);
      BufferRenderer.drawWithGlobalProgram(b.end());
   }
   public void drawMaskBox(Matrix4f matrix, Box box) {
      BufferBuilder b = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      int white = -1;
      b.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).color(white);
      b.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).color(white);
      b.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(white);
      b.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).color(white);
      b.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).color(white);
      b.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(white);
      b.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(white);
      b.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(white);
      b.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).color(white);
      b.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).color(white);
      b.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(white);
      b.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).color(white);
      b.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).color(white);
      b.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(white);
      b.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(white);
      b.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(white);
      b.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).color(white);
      b.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).color(white);
      b.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(white);
      b.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).color(white);
      b.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).color(white);
      b.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(white);
      b.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(white);
      b.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(white);
      BufferRenderer.drawWithGlobalProgram(b.end());
   }
   public void drawFullscreenQuad() {
      BufferBuilder b = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      float width = (float)Math.max(mc.getWindow().getScaledWidth(), 1);
      float height = (float)Math.max(mc.getWindow().getScaledHeight(), 1);
      b.vertex(0.0f, 0.0f, 0.0F)
         .texture(0.0f, 1.0F)
         .color(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      b.vertex(0.0f, height, 0.0f)
         .texture(0.0f, 0.0f)
         .color(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      b.vertex(width, height, 0.0f)
         .texture(1.0f, 0.0f)
         .color(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      b.vertex(width, 0.0f, 0.0f)
         .texture(1.0f, 1.0f)
         .color(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      BufferRenderer.drawWithGlobalProgram(b.end());
   }
}
