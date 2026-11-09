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
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class TrailsModule extends Module {
   public static TrailsModule INSTANCE = new TrailsModule();
   private final SliderSetting duration = new SliderSetting(
      "Длительность",
      300.0f,
      100.0f,
      1000.0f,
      10.0f
   );
   private final List<ObsidianComponent_72> points = new ArrayList<>();

   public TrailsModule() {
      super("Trails", "Красивый след за игроком", Category.RENDER);
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.duration;
      this.addSettings(var10001);
   }

   @Override
   public void onDisable() {
      this.points.clear();
      super.onDisable();
   }

   @EventTarget
   public void onRender(RenderEvent event) {
      if (mc.options.getPerspective() != Perspective.FIRST_PERSON) {
         if (mc.player != null && mc.world != null) {
            long currentTime = System.currentTimeMillis();
            this.points
               .removeIf(
                  p -> ((float)(currentTime - p.time) > this.duration.get())
               );
            Vec3d playerPos = this.interpolatePlayerPosition(event.getTickDelta());
            this.points.add(new ObsidianComponent_72(playerPos));
            this.render3DPoints(event.getMatrices());
         }
      }
   }
   public Vec3d interpolatePlayerPosition(float partialTicks) {
      return new Vec3d(
         MathHelper.lerp((double)partialTicks, mc.player.prevX, mc.player.getX()),
         MathHelper.lerp((double)partialTicks, mc.player.prevY, mc.player.getY()),
         MathHelper.lerp((double)partialTicks, mc.player.prevZ, mc.player.getZ())
      );
   }
   public Vec3d interpolatePlayerPosition(PlayerEntity playerEntity, float partialTicks) {
      return new Vec3d(
         MathHelper.lerp((double)partialTicks, playerEntity.prevX, playerEntity.getX()),
         MathHelper.lerp((double)partialTicks, playerEntity.prevY, playerEntity.getY()),
         MathHelper.lerp((double)partialTicks, playerEntity.prevZ, playerEntity.getZ())
      );
   }
   public void render3DPoints(MatrixStack matrixStack) {
      if (this.points.size() >= (2)) {
         this.startRendering();
         matrixStack.push();
         Vec3d view = mc.gameRenderer.getCamera().getPos();
         matrixStack.translate(-view.x, -view.y, -view.z);
         Matrix4f matrix = matrixStack.peek().getPositionMatrix();
         int themeColor = ObsidianComponent_olorUtil.getThemeColor();
         float red = ObsidianComponent_olorUtil.redf(themeColor);
         float green = ObsidianComponent_olorUtil.greenf(themeColor);
         float blue = ObsidianComponent_olorUtil.bluef(themeColor);
         BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
         int index = 0;

         for (ObsidianComponent_72 p : this.points) {
            float alpha = (float)index / (float)this.points.size() * 0.699999988079071f;
            int alphaInt = (int)(alpha * 255.0f);
            buffer.vertex(matrix, (float)p.pos.x, (float)(p.pos.y + (double)mc.player.getHeight()), (float)p.pos.z)
               .color(
                  (int)(red * 255.0f),
                  (int)(green * 255.0f),
                  (int)(blue * 255.0f),
                  alphaInt
               );
            buffer.vertex(matrix, (float)p.pos.x, (float)p.pos.y, (float)p.pos.z)
               .color(
                  (int)(red * 255.0f),
                  (int)(green * 255.0f),
                  (int)(blue * 255.0f),
                  alphaInt
               );
            index++;
         }

         BufferRenderer.drawWithGlobalProgram(buffer.end());
         RenderSystem.lineWidth(2.0f);
         this.renderLineStrip(matrix, this.points, true, red, green, blue);
         this.renderLineStrip(matrix, this.points, false, red, green, blue);
         matrixStack.pop();
         this.stopRendering();
      }
   }
   public void renderLineStrip(Matrix4f matrix, List<ObsidianComponent_72> points, boolean withHeight, float red, float green, float blue) {
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
      int index = 0;

      for (ObsidianComponent_72 p : points) {
         float alpha = Math.min(
            (float)index / (float)points.size() * 1.5f,
            1.0f
         );
         int alphaInt = (int)(alpha * 255.0f);
         float y = withHeight ? (float)(p.pos.y + (double)mc.player.getHeight()) : (float)p.pos.y;
         buffer.vertex(matrix, (float)p.pos.x, y, (float)p.pos.z)
            .color(
               (int)(red * 255.0f),
               (int)(green * 255.0f),
               (int)(blue * 255.0f),
               alphaInt
            );
         index++;
      }

      BufferRenderer.drawWithGlobalProgram(buffer.end());
   }
   public void startRendering() {
      RenderSystem.enableBlend();
      RenderSystem.disableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
   }
   public void stopRendering() {
      RenderSystem.depthMask(true);
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }
}
