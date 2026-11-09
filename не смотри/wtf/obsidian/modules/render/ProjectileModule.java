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
import net.minecraft.block.Blocks;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ProjectileModule extends Module {
   private final FontRenderer impactFont = ObsidianComponent_201.getFont("sf_regular", 14);
   public static ProjectileModule INSTANCE = new ProjectileModule();
   private static final Identifier BLOOM_TEXTURE = Identifier.of("obsidian", "textures/particle/bloom.png");
   private final SliderSetting size = new SliderSetting(
      "Размер",
      1.2000000476837158f,
      0.6000000238418579f,
      2.4000000953674316f,
      0.10000000149011612f
   );
   private final List<ObsidianComponent_60> impactPoints = new ArrayList<>();
   private final Matrix4f lastProjectionMatrix = new Matrix4f();
   private final Quaternionf lastCameraRotation = new Quaternionf();
   public Vec3d lastCameraPos = Vec3d.ZERO;
   public boolean hasMatrices;

   public ProjectileModule() {
      super("Projectile", "Траектория жемчуга эндера", Category.RENDER);
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.size;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onRender3D(RenderEvent event) {
      if (mc.world != null && mc.player != null) {
         this.impactPoints.clear();
         this.hasMatrices = true;
         this.lastProjectionMatrix.set(event.getProjectionMatrix());
         this.lastCameraPos = event.getCamera().getPos();
         this.lastCameraRotation.set(event.getCamera().getRotation());
         MatrixStack matrices = event.getMatrices();
         Camera camera = event.getCamera();
         Vec3d cameraPos = camera.getPos();
         Quaternionf cameraRotation = camera.getRotation();
         float tickDelta = event.getTickDelta();
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 1);
         RenderSystem.disableCull();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
         RenderSystem.setShaderTexture(0, BLOOM_TEXTURE);
         Box searchBox = mc.player.getBoundingBox().expand(128.0);

         for (EnderPearlEntity pearl : mc.world.getEntitiesByClass(EnderPearlEntity.class, searchBox, Entity::isAlive)) {
            List<Vec3d> points = this.simulate(pearl, tickDelta);
            if (points.size() >= 2) {
               float seconds = (float)(points.size() - 1)
                  / 20.0f;
               Vec3d impactPos = points.get(points.size() - (1));
               this.impactPoints.add(new ObsidianComponent_60(impactPos, seconds));
               float quadSize = this.size.get() * 0.20000000298023224f;
               int color = ObsidianComponent_olorUtil.setAlphaColor(ObsidianComponent_olorUtil.getThemeColor(), 40);
               int r = color >> 16 & (255);
               int g = color >> 8 & (255);
               int b = color & (255);
               int a = color >> 24 & (255);
               matrices.push();
               matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
               BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

               for (int i = 0; i < points.size() - (1); i++) {
                  Vec3d start = points.get(i);
                  Vec3d end = points.get(i + 1);
                  int samples = Math.max(
                     2,
                     Math.min(
                        12,
                        (int)Math.ceil(
                           start.distanceTo(end)
                              / (double)Math.max(
                                 quadSize * 1.75f,
                                 0.07999999821186066f
                              )
                        )
                     )
                  );

                  for (int j = 0; j < samples; j++) {
                     Vec3d interp = start.lerp(end, (double)j / (double)samples);
                     matrices.push();
                     matrices.translate(interp.x, interp.y, interp.z);
                     matrices.multiply(cameraRotation);
                     Matrix4f matrix = matrices.peek().getPositionMatrix();
                     buffer.vertex(matrix, -quadSize, -quadSize, 0.0f)
                        .texture(
                           0.0f, 0.0f
                        )
                        .color(r, g, b, a);
                     buffer.vertex(matrix, -quadSize, quadSize, 0.0F)
                        .texture(
                           0.0f, 1.0f
                        )
                        .color(r, g, b, a);
                     buffer.vertex(matrix, quadSize, quadSize, 0.0f)
                        .texture(1.0f, 1.0f)
                        .color(r, g, b, a);
                     buffer.vertex(matrix, quadSize, -quadSize, 0.0f)
                        .texture(
                           1.0f, 0.0f
                        )
                        .color(r, g, b, a);
                     matrices.pop();
                  }
               }

               BufferRenderer.drawWithGlobalProgram(buffer.end());
               matrices.pop();
               float markerSize = quadSize * 1.600000023841858f;
               int markerColor = ObsidianComponent_olorUtil.setAlphaColor(ObsidianComponent_olorUtil.getThemeColor(), 170);
               int mr = markerColor >> 16 & (255);
               int mg = markerColor >> (8) & (255);
               int mb = markerColor & (255);
               int ma = markerColor >> 24 & 255;
               float mx = (float)(impactPos.x - cameraPos.x);
               float my = (float)(impactPos.y - cameraPos.y + 0.029999999329447746);
               float mz = (float)(impactPos.z - cameraPos.z);
               matrices.push();
               matrices.translate(mx, my, mz);
               matrices.multiply(cameraRotation);
               Matrix4f markerMatrix = matrices.peek().getPositionMatrix();
               BufferBuilder marker = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
               marker.vertex(markerMatrix, -markerSize, -markerSize, 0.0f)
                  .texture(0.0f, 0.0f)
                  .color(mr, mg, mb, ma);
               marker.vertex(markerMatrix, -markerSize, markerSize, 0.0f)
                  .texture(0.0f, 1.0f)
                  .color(mr, mg, mb, ma);
               marker.vertex(markerMatrix, markerSize, markerSize, 0.0f)
                  .texture(1.0f, 1.0f)
                  .color(mr, mg, mb, ma);
               marker.vertex(markerMatrix, markerSize, -markerSize, 0.0f)
                  .texture(1.0f, 0.0f)
                  .color(mr, mg, mb, ma);
               BufferRenderer.drawWithGlobalProgram(marker.end());
               matrices.pop();
            }
         }

         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
         RenderSystem.enableCull();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
      }
   }

   @EventTarget
   public void onRender2D(EventRender2D event) {
      if (this.hasMatrices && !this.impactPoints.isEmpty() && mc.player != null) {
         MatrixStack matrices = event.getContext().getMatrices();
         FontRenderer font = this.impactFont;
         if (font != null) {
            int themeColor = ObsidianComponent_olorUtil.getThemeColor();

            for (ObsidianComponent_60 impact : this.impactPoints) {
               Vec3d screen = this.worldToScreen(impact.pos());
               if (screen != null) {
                  String text = this.formatOneDecimal(impact.seconds()) + " сек";
                  float textWidth = font.getStringWidth(text);
                  float boxWidth = textWidth + 10.0f;
                  float boxHeight = 12.5f;
                  float x = (float)screen.x - boxWidth / 2.0f;
                  float y = (float)screen.y - 6.0f;
                  RenderUtil.drawDefaultHudThemedPanel(
                     matrices,
                     x,
                     y,
                     boxWidth,
                     boxHeight,
                     3.0f,
                     3.5f,
                     themeColor
                  );
                  font.drawString(
                     matrices,
                     text,
                     x + 5.5f,
                     y + 4.550000190734863f,
                     -1
                  );
               }
            }
         }
      }
   }
   public Vec3d worldToScreen(Vec3d worldPos) {
      if (mc != null && mc.getWindow() != null) {
         Vector3f relative = new Vector3f(
            (float)(worldPos.x - this.lastCameraPos.x), (float)(worldPos.y - this.lastCameraPos.y), (float)(worldPos.z - this.lastCameraPos.z)
         );
         Quaternionf invCameraRot = new Quaternionf(this.lastCameraRotation).conjugate();
         relative.rotate(invCameraRot);
         Vector4f clip = new Vector4f(relative.x, relative.y, relative.z, 1.0f);
         this.lastProjectionMatrix.transform(clip);
         float w = clip.w;
         if (w <= 9.999999747378752e-06f) {
            return null;
         } else {
            float ndcX = clip.x / w;
            float ndcY = clip.y / w;
            float ndcZ = clip.z / w;
            float screenX = (
                  ndcX * 0.5f + 0.5f
               )
               * (float)mc.getWindow().getScaledWidth();
            float screenY = (
                  1.0f
                     - (
                        ndcY * 0.5f
                           + 0.5f
                     )
               )
               * (float)mc.getWindow().getScaledHeight();
            if (!Float.isNaN(screenX) && !Float.isNaN(screenY) && !Float.isInfinite(screenX) && !Float.isInfinite(screenY)) {
               return !(screenX < -400.0f)
                     && !(screenY < -400.0f)
                     && !(screenX > (float)(mc.getWindow().getScaledWidth() + (400)))
                     && !(screenY > (float)(mc.getWindow().getScaledHeight() + 400))
                  ? new Vec3d((double)screenX, (double)screenY, (double)ndcZ)
                  : null;
            } else {
               return null;
            }
         }
      } else {
         return null;
      }
   }
   public String formatOneDecimal(float value) {
      int scaled = Math.round(value * 10.0f);
      return scaled / 10 + "." + Math.abs(scaled % 10);
   }
   public List<Vec3d> simulate(EnderPearlEntity pearl, float tickDelta) {
      List<Vec3d> points = new ArrayList<>();
      Vec3d pos = new Vec3d(
         MathHelper.lerp((double)tickDelta, pearl.prevX, pearl.getX()),
         MathHelper.lerp((double)tickDelta, pearl.prevY, pearl.getY()),
         MathHelper.lerp((double)tickDelta, pearl.prevZ, pearl.getZ())
      );
      Vec3d motion = pearl.getVelocity();
      points.add(pos);

      for (int i = 0; i < 300; i++) {
         Vec3d nextPos = pos.add(motion);
         BlockHitResult hit = mc.world.raycast(new RaycastContext(pos, nextPos, ShapeType.COLLIDER, FluidHandling.NONE, mc.player));
         if (hit.getType() == Type.BLOCK) {
            points.add(hit.getPos());
            break;
         }

         points.add(nextPos);
         pos = nextPos;
         boolean inWater = mc.world.getBlockState(BlockPos.ofFloored(nextPos)).isOf(Blocks.WATER);
         double drag = inWater
            ? 0.8
            : 0.99;
         motion = motion.multiply(drag)
            .subtract(
               0.0,
               0.03,
               0.0
            );
         if (nextPos.y <= (double)mc.world.getBottomY()) {
            break;
         }
      }

      return points;
   }
}
