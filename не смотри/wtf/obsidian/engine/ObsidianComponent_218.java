package wtf.obsidian.engine;

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
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

public class ObsidianComponent_218 {
   public final ObsidianComponent_271 state;
   public final ObsidianComponent_190 settingRenderer;
   public final ObsidianComponent_325 themeManager;
   public final ObsidianComponent_383 configManager;
   private final AnimationUtil widthAnimation = new AnimationUtil(
      60.0f, 10.5f, ObsidianComponent_57.QUAD_OUT
   );
   public int shaderProgram = -1;
   public int vaoId = -1;
   public int vboId = -1;
   private boolean shaderFailed = false;
   public long startTime = -1L;
   public int uResolutionLoc = -1;
   public int uTimeLoc = -1;
   public int uColorLoc = -1;
   public int uAlphaLoc = -1;
   public int uTrailCountLoc = -1;
   public int uTrailPointsLoc = -1;
   public int uTrailAlphasLoc = -1;
   private final List<ObsidianComponent_424> trailPoints = new ArrayList<>();
   public float waveInertia = 0.0f;
   public static final int COLS = 40;
   public static final int ROWS = 30;
   public static final float INFLUENCE = 130.0F;
   public static final float STRENGTH = 18.0F;
   public static final float SPEED = 2.2E-4F;
   public static final float LERP = 0.018F;
   public static final float DAMPING = 0.96F;
   public ObsidianComponent_418[] gridNodes = new ObsidianComponent_418[0];
   public int lastW = -1;
   public int lastH = -1;
   public long lastT = -1L;

   public ObsidianComponent_218(ObsidianComponent_271 state, ObsidianComponent_190 settingRenderer, ObsidianComponent_325 themeManager) {
      this.state = state;
      this.settingRenderer = settingRenderer;
      this.themeManager = themeManager;
      this.configManager = new ObsidianComponent_383();
      this.startTime = System.currentTimeMillis();
   }

   public ObsidianComponent_383 getConfigManager() {
      return this.configManager;
   }

   public ObsidianComponent_325 getThemeManager() {
      return this.themeManager;
   }

   public void render(DrawContext context, int mouseX, int mouseY, Window window, float animationProgress) {
      if (window != null) {
         float alphaMul = MathHelper.clamp(animationProgress, 0.0f, 1.0F);
         int shadeColor = this.getFadeShadeColor(alphaMul, 120);
         int colorTheme = this.getThemeColor();
         this.waveInertia = this.waveInertia
            + 0.3499999940395355f
               * (
                  1.0f
                     + MathHelper.sin((float)System.currentTimeMillis() * 0.0012000000569969416f)
                        * 0.25f
               );
         Category[] categories = Category.values();
         float totalWidth = ObsidianComponent_200.getTotalCategoriesWidth(categories.length);
         float guiX = MathHelper.clamp(
            this.state.getX(),
            10.0f,
            Math.max(
               10.0f,
               (float)window.getScaledWidth() - totalWidth - 10.0f
            )
         );
         float guiY = MathHelper.clamp(
            this.state.getY(),
            160.0f,
            Math.max(
               160.0f,
               (float)window.getScaledHeight() - 50.0f
            )
         );
         int bgAlpha = (int)(200.0f * alphaMul);
         RenderUtil.drawRoundedRect(
            context.getMatrices(),
            0.0f,
            0.0f,
            (float)window.getScaledWidth(),
            (float)window.getScaledHeight(),
            0.0f,
            ObsidianComponent_olorUtil.rgba(10, 10, 13, bgAlpha)
         );
         this.renderLiquidShader(context, window, colorTheme, alphaMul, mouseX, mouseY);
         this.renderGrid(context, window, mouseX, mouseY, alphaMul);
         Module hoveredModule = null;

         for (int i = 0; i < categories.length; i++) {
            Category category = categories[i];
            float panelX = ObsidianComponent_200.getCategoryPanelX(guiX, i);
            Module categoryHoveredModule = this.renderCategoryPanel(context, mouseX, mouseY, panelX, guiY, category, colorTheme, alphaMul, shadeColor, window);
            if (categoryHoveredModule != null) {
               hoveredModule = categoryHoveredModule;
            }
         }

         this.themeManager.updatePosition(window, categories.length);
         this.themeManager.render(context, mouseX, mouseY, this.state.getRenderOffsetY(), alphaMul, colorTheme);
         this.renderDescription(context, window, hoveredModule, colorTheme, animationProgress);
         this.configManager.updatePosition(window, categories.length);
         this.configManager.render(context, mouseX, mouseY, this.state.getRenderOffsetY(), alphaMul, colorTheme);
      }
   }
   public void renderLiquidShader(DrawContext context, Window window, int colorTheme, float alphaMul, int mouseX, int mouseY) {
      if (this.shaderFailed) {
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      } else {
         if (this.shaderProgram == (-1)) {
            this.initShader();
         }

         if (this.shaderProgram != (-1)) {
            float realWidth = (float)window.getWidth();
            float realHeight = (float)window.getHeight();
            float time = (float)(System.currentTimeMillis() - this.startTime) / 1000.0f;
            float r = (float)(colorTheme >> (16) & 0xFF) / 255.0f;
            float g = (float)(colorTheme >> (8) & 255)
               / 255.0f;
            float b = (float)(colorTheme & 255) / 255.0f;
            long now = System.currentTimeMillis();
            float aspect = (float)window.getScaledWidth() / (float)window.getScaledHeight();
            float normX = (float)mouseX / (float)window.getScaledWidth() * aspect;
            float normY = 1.0f - (float)mouseY / (float)window.getScaledHeight();
            if (this.trailPoints.isEmpty()) {
               this.trailPoints.add(new ObsidianComponent_424((float)mouseX, (float)mouseY, normX, normY, now));
            } else {
               ObsidianComponent_424 last = this.trailPoints.get(this.trailPoints.size() - 1);
               float dx = (float)mouseX - last.rawX;
               float dy = (float)mouseY - last.rawY;
               float dist = (float)Math.sqrt((double)(dx * dx + dy * dy));
               if (dist > 3.0f
                  || now - last.time > (15L)) {
                  this.trailPoints.add(new ObsidianComponent_424((float)mouseX, (float)mouseY, normX, normY, now));
               }
            }

            this.trailPoints
               .removeIf(
                  p -> (now - p.time > (350L))
               );

            while (this.trailPoints.size() > (16)) {
               this.trailPoints.remove(0);
            }

            float[] pointsArray = new float[32];
            float[] alphasArray = new float[16];
            int count = this.trailPoints.size();

            for (int i = 0; i < 16; i++) {
               if (i < count) {
                  ObsidianComponent_424 p = this.trailPoints.get(i);
                  pointsArray[i * (2)] = p.x;
                  pointsArray[i * (2) + (1)] = p.y;
                  float age = (float)(now - p.time) / 350.0f;
                  alphasArray[i] = MathHelper.clamp(
                     1.0f - age,
                     0.0f,
                     1.0f
                  );
               } else {
                  pointsArray[i * 2] = 0.0f;
                  pointsArray[i * (2) + 1] = 0.0f;
                  alphasArray[i] = 0.0f;
               }
            }

            int lastProgram = GL30.glGetInteger(35725);
            int lastVao = GL30.glGetInteger(34229);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            GL30.glUseProgram(this.shaderProgram);
            GL30.glUniform2f(this.uResolutionLoc, realWidth, realHeight);
            GL30.glUniform1f(this.uTimeLoc, time);
            GL30.glUniform3f(this.uColorLoc, r, g, b);
            GL30.glUniform1f(this.uAlphaLoc, alphaMul);
            GL30.glUniform1i(this.uTrailCountLoc, count);
            GL30.glUniform2fv(this.uTrailPointsLoc, pointsArray);
            GL30.glUniform1fv(this.uTrailAlphasLoc, alphasArray);
            GL30.glBindVertexArray(this.vaoId);
            GL30.glDrawArrays(4, 0, 6);
            GL30.glBindVertexArray(0);
            GL30.glUseProgram(0);
            GL30.glBindVertexArray(lastVao);
            GL30.glUseProgram(lastProgram);
            RenderSystem.enableDepthTest();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         }
      }
   }
   public void initShader() {
      String vertexSource = "#version 150\nin vec2 in_position;\nvoid main() {\n    gl_Position = vec4(in_position, 0.0, 1.0);\n}";
      String fragmentSource = "#version 150\nout vec4 fragColor;\nuniform vec2 u_resolution;\nuniform vec3 u_color;\nuniform float u_alpha;\nuniform int u_trail_count;\nuniform vec2 u_trail_points[16];\nuniform float u_trail_alphas[16];\n\nfloat distToSegment(vec2 p, vec2 a, vec2 b) {\n    vec2 pa = p - a, ba = b - a;\n    float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);\n    return length(pa - ba * h);\n}\n\nvoid main() {\n    vec2 uv = gl_FragCoord.xy / u_resolution.xy;\n    float aspect = u_resolution.x / u_resolution.y;\n    vec2 uvAspect = vec2(uv.x * aspect, uv.y);\n    \n    float trailGlow = 0.0;\n    for (int i = 0; i < 16; i++) {\n        if (i >= u_trail_count) break;\n        \n        vec2 p = u_trail_points[i];\n        float alpha = u_trail_alphas[i];\n        float dist = length(uvAspect - p);\n        \n        // Компактная неоновая точка курсора\n        float pointGlow = exp(-dist * 450.0) * 0.85 + exp(-dist * 160.0) * 0.15;\n        trailGlow += pointGlow * alpha;\n        \n        // Тончайшая неоновая нить шлейфа\n        if (i > 0) {\n            vec2 prev = u_trail_points[i - 1];\n            float dSeg = distToSegment(uvAspect, prev, p);\n            \n            float thinFactor = mix(280.0, 550.0, 1.0 - alpha);\n            trailGlow += (exp(-dSeg * thinFactor) * 0.6) * min(alpha, u_trail_alphas[i - 1]);\n        }\n    }\n    \n    // Выводим ТОЛЬКО цвет шлейфа, остальное пространство абсолютно прозрачно\n    vec3 finalColor = u_color * trailGlow * 1.8;\n    float finalAlpha = clamp(trailGlow * u_alpha, 0.0, 1.0);\n    \n    fragColor = vec4(finalColor, finalAlpha);\n}";

      try {
         int vertexShader = GL30.glCreateShader(35633);
         GL30.glShaderSource(vertexShader, vertexSource);
         GL30.glCompileShader(vertexShader);
         if (GL30.glGetShaderi(vertexShader, 35713) == 0) {
            throw new RuntimeException("Vertex shader compile error: " + GL30.glGetShaderInfoLog(vertexShader));
         }

         int fragmentShader = GL30.glCreateShader(35632);
         GL30.glShaderSource(fragmentShader, fragmentSource);
         GL30.glCompileShader(fragmentShader);
         if (GL30.glGetShaderi(fragmentShader, 35713) == 0) {
            throw new RuntimeException("Fragment shader compile error: " + GL30.glGetShaderInfoLog(fragmentShader));
         }

         this.shaderProgram = GL30.glCreateProgram();
         GL30.glAttachShader(this.shaderProgram, vertexShader);
         GL30.glAttachShader(this.shaderProgram, fragmentShader);
         GL30.glBindAttribLocation(this.shaderProgram, 0, "in_position");
         GL30.glLinkProgram(this.shaderProgram);
         if (GL30.glGetProgrami(this.shaderProgram, 35714) == 0) {
            throw new RuntimeException("Shader link error!");
         }

         GL30.glDeleteShader(vertexShader);
         GL30.glDeleteShader(fragmentShader);
         this.uResolutionLoc = GL30.glGetUniformLocation(this.shaderProgram, "u_resolution");
         this.uTimeLoc = GL30.glGetUniformLocation(this.shaderProgram, "u_time");
         this.uColorLoc = GL30.glGetUniformLocation(this.shaderProgram, "u_color");
         this.uAlphaLoc = GL30.glGetUniformLocation(this.shaderProgram, "u_alpha");
         this.uTrailCountLoc = GL30.glGetUniformLocation(this.shaderProgram, "u_trail_count");
         this.uTrailPointsLoc = GL30.glGetUniformLocation(this.shaderProgram, "u_trail_points");
         this.uTrailAlphasLoc = GL30.glGetUniformLocation(this.shaderProgram, "u_trail_alphas");
         float[] var10000 = new float[12];
         var10000[0] = -1.0f;
         var10000[1] = -1.0f;
         var10000[2] = 1.0f;
         var10000[3] = -1.0f;
         var10000[4] = -1.0f;
         var10000[5] = 1.0f;
         var10000[6] = -1.0f;
         var10000[7] = 1.0f;
         var10000[8] = 1.0f;
         var10000[9] = -1.0f;
         var10000[10] = 1.0f;
         var10000[11] = 1.0f;
         float[] quadVertices = var10000;
         this.vaoId = GL30.glGenVertexArrays();
         this.vboId = GL30.glGenBuffers();
         GL30.glBindVertexArray(this.vaoId);
         GL30.glBindBuffer(34962, this.vboId);
         FloatBuffer buffer = BufferUtils.createFloatBuffer(quadVertices.length);
         buffer.put(quadVertices).flip();
         GL30.glBufferData(34962, buffer, 35044);
         GL30.glEnableVertexAttribArray(0);
         GL30.glVertexAttribPointer(
            0,
            2,
            5126,
            false,
            8,
            0L
         );
         GL30.glBindBuffer(34962, 0);
         GL30.glBindVertexArray(0);
      } catch (Exception var7) {
         var7.printStackTrace();
         this.shaderFailed = true;
         this.shaderProgram = -1;
         this.vaoId = -1;
         this.vboId = -1;
      }
   }
   public void renderGrid(DrawContext context, Window window, int mouseX, int mouseY, float alphaMul) {
      int screenW = window.getScaledWidth();
      int screenH = window.getScaledHeight();
      if (screenW != this.lastW || screenH != this.lastH) {
         this.rebuildGrid(screenW, screenH);
         this.lastW = screenW;
         this.lastH = screenH;
      }

      long t = System.currentTimeMillis();
      float dt = this.lastT < (0L)
         ? 16.0f
         : Math.min((float)(t - this.lastT), 50.0f);
      this.lastT = t;
      this.updateGridNodes(t, dt, mouseX, mouseY);
      this.drawGridLines(context, alphaMul);
      this.drawGridDots(context, alphaMul);
   }
   public void rebuildGrid(int w, int h) {
      float cellW = (float)w / 39.0f;
      float cellH = (float)h / 29.0f;
      this.gridNodes = new ObsidianComponent_418[1200];
      int idx = 0;

      for (int row = 0; row < 30; row++) {
         for (int col = 0; col < (40); col++) {
            ObsidianComponent_418 n = new ObsidianComponent_418((float)col * cellW, (float)row * cellH);
            float nx = (float)col / 39.0f * 2.0f
               - 1.0f;
            float ny = (float)row
                  / 29.0f
                  * 2.0f
               - 1.0f;
            float dist = (float)Math.sqrt((double)(nx * nx + ny * ny))
               / (float)Math.sqrt(2.0);
            n.fade = Math.max(
               0.0f,
               1.0f
                  - (float)Math.pow((double)dist, 2.5)
            );
            this.gridNodes[idx++] = n;
         }
      }

      this.lastT = -1L;
   }
   public void updateGridNodes(long t, float dt, int mouseX, int mouseY) {
      float scale = dt / 16.0f;
      ObsidianComponent_418[] var7 = this.gridNodes;
      int var8 = var7.length;

      for (int var9 = 0; var9 < var8; var9++) {
         ObsidianComponent_418 n = var7[var9];
         float targetX = n.ox
            + (float)Math.sin((double)((float)t * 2.2E-4F * n.speed + n.phase)) * 6.0f
            + (float)Math.sin(
                  (double)(
                     (float)t * 0.00022000000171829015f * n.speed * 0.30000001192092896f
                        + n.phase * 1.7000000476837158f
                  )
               )
               * 3.0f;
         float targetY = n.oy
            + (float)Math.cos(
                  (double)(
                     (float)t * 0.00022000000171829015f * n.speed * 0.699999988079071f + n.phase
                  )
               )
               * 5.0f
            + (float)Math.cos(
                  (double)(
                     (float)t
                           * 0.00022000000171829015f
                           * n.speed
                           * 0.20000000298023224f
                        + n.phase * 2.0999999046325684f
                  )
               )
               * 2.5f;
         float mdx = (float)mouseX - n.x;
         float mdy = (float)mouseY - n.y;
         float mdist = (float)Math.sqrt((double)(mdx * mdx + mdy * mdy));
         if (mdist < 130.0f && mdist > 0.01F) {
            float force = (1.0f - mdist / 130.0f)
               * 18.0f;
            targetX += mdx / mdist * force;
            targetY += mdy / mdist * force;
         }

         float ax = (targetX - n.x) * 0.017999999225139618f;
         float ay = (targetY - n.y) * 0.017999999225139618f;
         n.vx = (n.vx + ax) * 0.96F;
         n.vy = (n.vy + ay) * 0.9599999785423279f;
         n.x = n.x + n.vx * scale;
         n.y = n.y + n.vy * scale;
      }
   }
   public void drawGridLines(DrawContext context, float alphaMul) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableDepthTest();
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      RenderSystem.lineWidth(0.699999988079071f);
      Matrix4f mat = context.getMatrices().peek().getPositionMatrix();
      Tessellator tess = Tessellator.getInstance();
      BufferBuilder buf = tess.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

      for (int row = 0; row < (30); row++) {
         for (int col = 0; col < 40; col++) {
            int i = row * 40 + col;
            ObsidianComponent_418 n = this.gridNodes[i];
            if (col < 39) {
               ObsidianComponent_418 m = this.gridNodes[i + 1];
               float a = 0.05000000074505806f
                  * Math.min(n.fade, m.fade)
                  * Math.max(alphaMul, 0.05000000074505806f);
               buf.vertex(mat, n.x, n.y, 0.0f)
                  .color(
                     1.0f,
                     1.0f,
                     1.0f,
                     a
                  );
               buf.vertex(mat, m.x, m.y, 0.0f)
                  .color(
                     1.0f,
                     1.0f,
                     1.0f,
                     a
                  );
            }

            if (row < 29) {
               ObsidianComponent_418 m = this.gridNodes[i + (40)];
               float a = 0.05000000074505806f
                  * Math.min(n.fade, m.fade)
                  * Math.max(alphaMul, 0.05000000074505806f);
               buf.vertex(mat, n.x, n.y, 0.0f)
                  .color(
                     1.0f,
                     1.0f,
                     1.0f,
                     a
                  );
               buf.vertex(mat, m.x, m.y, 0.0f)
                  .color(
                     1.0f,
                     1.0f,
                     1.0f,
                     a
                  );
            }
         }
      }

      BufferRenderer.drawWithGlobalProgram(buf.end());
      RenderSystem.enableDepthTest();
   }
   public void drawGridDots(DrawContext context, float alphaMul) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableDepthTest();
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      Matrix4f mat = context.getMatrices().peek().getPositionMatrix();
      Tessellator tess = Tessellator.getInstance();
      BufferBuilder buf = tess.begin(DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
      int segments = 8;
      float radius = 1.5f;
      ObsidianComponent_418[] var8 = this.gridNodes;
      int var9 = var8.length;

      for (int var10 = 0; var10 < var9; var10++) {
         ObsidianComponent_418 n = var8[var10];
         float a = 0.10000000149011612f
            * n.fade
            * Math.max(alphaMul, 0.05000000074505806f);

         for (int s = 0; s < segments; s++) {
            float a1 = (float)(
               (double)(s * (2))
                  * 3.141592653589793
                  / (double)segments
            );
            float a2 = (float)(
               (double)((s + (1)) * 2)
                  * 3.141592653589793
                  / (double)segments
            );
            buf.vertex(mat, n.x, n.y, 0.0f)
               .color(
                  1.0f,
                  1.0f,
                  1.0f,
                  a
               );
            buf.vertex(
                  mat,
                  n.x + (float)Math.cos((double)a1) * radius,
                  n.y + (float)Math.sin((double)a1) * radius,
                  0.0f
               )
               .color(
                  1.0f,
                  1.0f,
                  1.0f,
                  a
               );
            buf.vertex(
                  mat,
                  n.x + (float)Math.cos((double)a2) * radius,
                  n.y + (float)Math.sin((double)a2) * radius,
                  0.0f
               )
               .color(
                  1.0f,
                  1.0f,
                  1.0f,
                  a
               );
         }
      }

      BufferRenderer.drawWithGlobalProgram(buf.end());
      RenderSystem.enableDepthTest();
   }
   public Module renderCategoryPanel(
      DrawContext context, int mouseX, int mouseY, float panelX, float guiY, Category category, int colorTheme, float alphaMul, int shadeColor, Window window
   ) {
      float panelY = guiY + this.state.getRenderOffsetY();
      int time = (int)(this.waveInertia % 360.0f);
      int leftTop = ObsidianComponent_olorUtil.getThemeColor(time);
      int leftBottom = ObsidianComponent_olorUtil.getThemeColor(time + 30);
      int centerTop = ObsidianComponent_olorUtil.getThemeColor(time + (90));
      int centerBottom = ObsidianComponent_olorUtil.getThemeColor(time + 120);
      int rightTop = ObsidianComponent_olorUtil.getThemeColor(time + (180));
      int rightBottom = ObsidianComponent_olorUtil.getThemeColor(time + 210);
      this.widthAnimation.update(115.0f);
      RenderUtil.drawWaveHudHeader(
         context.getMatrices(),
         panelX,
         panelY - 20.0f,
         115.0f,
         20.0f,
         0.0f,
         10.0f,
         10.0f,
         leftTop,
         leftBottom,
         centerTop,
         centerBottom,
         rightTop,
         rightBottom
      );
      float contentY = ObsidianComponent_200.getContentY(panelY);
      float contentHeight = ObsidianComponent_200.getCategoryVisibleHeight(this.state, category, (float)window.getScaledHeight(), contentY);
      float categoryPanelHeight = contentY - panelY + contentHeight;
      RenderUtil.drawRoundedRect(
         context.getMatrices(),
         panelX,
         panelY,
         115.0f,
         categoryPanelHeight,
         0.0f,
         ObsidianComponent_olorUtil.rgba(
            25,
            25,
            25,
            150
         )
      );
      this.issue(15)
         .drawCenteredString(
            context.getMatrices(),
            category.getName(),
            panelX + 57.0f,
            panelY - 12.0f,
            this.alpha(-1, alphaMul)
         );
      this.state.clampScroll(category, contentHeight);
      float moduleY = contentY + this.state.getScroll(category);
      Module hoveredModule = null;
      ObsidianComponent_343.push();
      ObsidianComponent_343.setFromComponentCoordinates(
         (double)panelX, (double)contentY, 115.0, (double)contentHeight
      );

      for (Module module : this.state.getModules(category)) {
         float openProgress = this.state.getOpenProgress(module);
         float moduleHeight = ObsidianComponent_200.getModuleHeight(module, openProgress);
         if (moduleY + moduleHeight + 0.0f >= contentY && moduleY <= contentY + contentHeight) {
            Module moduleHover = this.renderModule(
               context, mouseX, mouseY, panelX, moduleY, module, openProgress, moduleHeight, colorTheme, alphaMul, shadeColor
            );
            if (moduleHover != null) {
               hoveredModule = moduleHover;
            }
         }

         moduleY += 0.0f + moduleHeight;
      }

      ObsidianComponent_343.pop();
      return hoveredModule;
   }
   public Module renderModule(
      DrawContext context,
      int mouseX,
      int mouseY,
      float panelX,
      float moduleY,
      Module module,
      float openProgress,
      float moduleHeight,
      int colorTheme,
      float alphaMul,
      int shadeColor
   ) {
      List<Setting> settings = module.getSettings();
      this.renderModuleBackground(context, panelX, moduleY, moduleHeight, module.isEnable(), colorTheme, shadeColor);
      String moduleName = module.getName();
      String bindText = "";
      if (this.state.getBindingModule() == module) {
         bindText = " [...]";
      } else if (module.getKey() != -1) {
         bindText = " [" + this.state.toEnglish(ObsidianComponent_349.getBindName(module.getKey())) + "]";
      }

      int nameColor = module.isEnable()
         ? this.alpha(-1, alphaMul)
         : this.alpha(
            ObsidianComponent_olorUtil.rgba(
               255,
               255,
               255,
               170
            ),
            alphaMul
         );
      int bindColor = module.isEnable()
         ? this.alpha(
            ObsidianComponent_olorUtil.rgba(255, 255, 255, 150), alphaMul
         )
         : this.alpha(
            ObsidianComponent_olorUtil.rgba(
               255,
               255,
               255,
               100
            ),
            alphaMul
         );
      this.issue(14)
         .draw(
            context.getMatrices(),
            moduleName,
            panelX + 10.0f - 5.0f,
            moduleY + 8.0f,
            nameColor
         );
      if (!bindText.isEmpty()) {
         float nameWidth = this.issue(14).getWidth(moduleName);
         this.issue(11)
            .draw(
               context.getMatrices(),
               bindText,
               panelX + 10.0f - 7.0f + nameWidth,
               moduleY + 9.0f,
               bindColor
            );
      }

      if (settings != null && !settings.isEmpty() && ObsidianComponent_200.hasVisibleSettings(settings)) {
         this.renderModuleDots(context, panelX, moduleY, module, module.isEnable(), alphaMul);
      }

      if (settings != null && !settings.isEmpty()) {
         this.settingRenderer.render(context, module, panelX, moduleY, openProgress, colorTheme, (double)mouseX, (double)mouseY, this.state);
      }

      return MathUtil.isHovered(
            (double)mouseX,
            (double)mouseY,
            (double)(panelX + 0.0f),
            (double)moduleY,
            115.0,
            (double)moduleHeight
         )
         ? module
         : null;
   }
   public void renderModuleBackground(DrawContext context, float panelX, float moduleY, float moduleHeight, boolean enabled, int colorTheme, int shadeColor) {
      if (enabled) {
         float alpha = 0.25f;
         int alphaInt = (int)(alpha * 255.0f) << (24);
         int time = (int)(this.waveInertia % 360.0f);
         int leftTop = ObsidianComponent_olorUtil.getThemeColor(time) & (16777215) | alphaInt;
         int leftBottom = ObsidianComponent_olorUtil.getThemeColor(time + 30) & 16777215 | alphaInt;
         int centerTop = ObsidianComponent_olorUtil.getThemeColor(time + 90) & 16777215 | alphaInt;
         int centerBottom = ObsidianComponent_olorUtil.getThemeColor(time + (120)) & (16777215) | alphaInt;
         int rightTop = ObsidianComponent_olorUtil.getThemeColor(time + 180) & (16777215) | alphaInt;
         int rightBottom = ObsidianComponent_olorUtil.getThemeColor(time + (210)) & (16777215) | alphaInt;
         RenderUtil.drawWaveHudHeader(
            context.getMatrices(),
            panelX,
            moduleY - 0.5f,
            135.0f,
            moduleHeight,
            0.0f,
            5.0f,
            5.0f,
            leftTop,
            leftBottom,
            centerTop,
            centerBottom,
            rightTop,
            rightBottom
         );
      }
   }
   public void renderModuleDots(DrawContext context, float panelX, float moduleY, Module module, boolean enabled, float alphaMul) {
      int dotsColor = enabled
         ? this.alpha(
            ObsidianComponent_olorUtil.rgba(
               255,
               255,
               255,
               220
            ),
            alphaMul
         )
         : this.alpha(
            ObsidianComponent_olorUtil.rgba(255, 255, 255, 100),
            alphaMul
         );
      float dotsX = panelX + 105.5F;
      float baseY = moduleY + 10.0f;
      float spacing = 2.0f;
      float radius = 2.0999999046325684f;
      float bottomXOffset = 2.0999999046325684f;
      float angle = this.state
         .updateDotsRotation(
            module,
            module.isOpen() ? 1.5707963705062866f : 0.0f
         );
      float cos = (float)Math.cos((double)angle);
      float sin = (float)Math.sin((double)angle);
      float[][] var10000 = new float[3][];
      int var10002 = 0;
      float[] var10003 = new float[2];
      var10003[0] = 0.0f;
      var10003[1] = -spacing;
      var10000[var10002] = var10003;
      var10002 = 1;
      var10003 = new float[2];
      var10003[0] = -bottomXOffset;
      var10003[1] = spacing;
      var10000[var10002] = var10003;
      var10002 = 2;
      var10003 = new float[2];
      var10003[0] = bottomXOffset;
      var10003[1] = spacing;
      var10000[var10002] = var10003;
      float[][] offsets = var10000;
      float[][] var17 = offsets;
      int var18 = offsets.length;

      for (int var19 = 0; var19 < var18; var19++) {
         float[] offset = var17[var19];
         float rx = offset[0] * cos - offset[1] * sin;
         float ry = offset[0] * sin + offset[1] * cos;
         RenderUtil.drawRoundCircle(context.getMatrices(), dotsX + rx, baseY + ry, radius, dotsColor);
      }
   }
   public int getThemeColor() {
      return !Obsidian.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")
         ? Obsidian.INSTANCE.themeStorage.getThemes().getTheme().color[0]
         : ObsidianComponent_olorUtil.getThemeColor();
   }
   public void renderDescription(DrawContext context, Window window, Module hoveredModule, int colorTheme, float alphaMul) {
      if (hoveredModule != null) {
         String description = hoveredModule.getDisplayDescription();
         if (description != null && !description.isBlank() && !"NULLABLE".equalsIgnoreCase(description) && !"desc".equalsIgnoreCase(description)) {
            FontRenderer descriptionFont = this.issue(16);
            float maxWidth = (float)window.getScaledWidth() - 40.0f;
            List<String> lines = this.wrapDescription(descriptionFont, description, maxWidth);
            if (!lines.isEmpty()) {
               float lineHeight = descriptionFont.getHeight() - 2.0f;
               float boxHeight = (float)lines.size() * lineHeight;
               float centerX = (float)window.getScaledWidth() * 0.5f;
               float startY = 0.0f - boxHeight - 6.0f;

               for (int i = 0; i < lines.size(); i++) {
                  descriptionFont.drawCenteredString(
                     context.getMatrices(), lines.get(i), centerX, startY + (float)i * lineHeight, ObsidianComponent_olorUtil.applyAlpha(-1, alphaMul)
                  );
               }
            }
         }
      }
   }
   public List<String> wrapDescription(FontRenderer font, String text, float maxWidth) {
      List<String> lines = new ArrayList<>();
      String[] words = text.trim().split("\\s+");
      if (words.length == 0) {
         return lines;
      } else {
         StringBuilder currentLine = new StringBuilder();
         String[] var7 = words;
         int var8 = words.length;

         for (int var9 = 0; var9 < var8; var9++) {
            String word = var7[var9];
            String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (!(font.getWidth(candidate) <= maxWidth) && !currentLine.isEmpty()) {
               lines.add(currentLine.toString());
               currentLine.setLength(0);
               currentLine.append(word);
            } else {
               currentLine.setLength(0);
               currentLine.append(candidate);
            }
         }

         if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
         }

         return lines;
      }
   }
   public float getSearchWidth() {
      String query = this.state.getSearchText();
      String text = query.isEmpty() ? "Search..." : query;
      float contentWidth = 19.0f
         + this.issue(14).getWidth(text)
         + 8.0f;
      return Math.max(75.0f, contentWidth);
   }
   public FontRenderer issue(int size) {
      return ObsidianComponent_201.getFont("suisse", size);
   }
   public int alpha(int color, float alphaMul) {
      return ObsidianComponent_olorUtil.applyAlpha(color, alphaMul);
   }
   public int getFadeShadeColor(float alphaMul, int maxAlpha) {
      int alpha = MathHelper.clamp(
         (int)((1.0f - alphaMul) * (float)maxAlpha),
         0,
         255
      );
      return ObsidianComponent_olorUtil.rgba(0, 0, 0, alpha);
   }
}
