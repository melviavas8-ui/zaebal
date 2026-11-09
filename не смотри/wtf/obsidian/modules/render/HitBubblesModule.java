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
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class HitBubblesModule extends Module {
   public static HitBubblesModule INSTANCE = new HitBubblesModule();
   public static final long LIFE_MS = 1600L;
   private final CopyOnWriteArrayList<ObsidianComponent_176> bubbles = new CopyOnWriteArrayList<>();
   private final Identifier bubbleTexture = Identifier.of("obsidian", "textures/hitbubble/bubble.png");

   public HitBubblesModule() {
      super("HitBubbles", "Круг при ударе игрока", Category.RENDER);
   }

   @Override
   public void onDisable() {
      this.bubbles.clear();
      super.onDisable();
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      long now = System.currentTimeMillis();
      this.bubbles
         .removeIf(
            b -> (now - b.spawnTime() >= (1600L))
         );
   }

   @EventTarget
   public void onAttack(Event_358 event) {
      if (event != null && event.getTarget() != null) {
         if (event.getTarget() instanceof LivingEntity living) {
            if (event.getPlayer() != null) {
               Vec3d sideDir = this.getHitSideDirection(living, event.getPlayer().getPos());
               Vec3d pos = this.getHitPosition(living, sideDir);
               float sideYaw = (float)Math.toDegrees(Math.atan2(sideDir.x, sideDir.z));
               this.bubbles
                  .add(
                     new ObsidianComponent_176(
                        pos,
                        System.currentTimeMillis(),
                        (float)(Math.random() * 360.0),
                        sideYaw
                     )
                  );
            }
         }
      }
   }

   @EventTarget
   public void onWorldRender(RenderEvent event) {
      if (!this.bubbles.isEmpty() && mc.player != null) {
         MatrixStack stack = event.getMatrices();
         Vec3d cameraPos = event.getCamera().getPos();
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 1);
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.disableCull();
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
         RenderSystem.setShaderTexture(0, this.bubbleTexture);
         long now = System.currentTimeMillis();

         for (ObsidianComponent_176 bubble : this.bubbles) {
            this.renderSingleBubble(stack, cameraPos, bubble, now);
         }

         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(true);
         RenderSystem.enableCull();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
      }
   }
   public void renderSingleBubble(MatrixStack stack, Vec3d cameraPos, ObsidianComponent_176 bubble, long now) {
      float progress = (float)(now - bubble.spawnTime()) / 1600.0f;
      if (!(progress >= 1.0f)) {
         float inPhase = Math.max(
            0.0f,
            Math.min(1.0f, progress / 0.2199999988079071f)
         );
         float outPhase = Math.max(
            0.0f,
            Math.min(
               1.0f,
               (progress - 0.22499999403953552f) / 0.4000000059604645f
            )
         );
         float scaleIn = inPhase
            * inPhase
            * (3.0f - 2.0f * inPhase);
         float scaleOut = 1.0f - outPhase * outPhase;
         float scale = 0.019999999552965164f
            + 1.5499999523162842f * scaleIn * scaleOut;
         float alpha = 1.0f - outPhase * outPhase * outPhase;
         float rotation = (float)(now - bubble.spawnTime()) / 1.5f + bubble.spinSeed();
         Vec3d rel = bubble.pos().subtract(cameraPos);
         int color = ObsidianComponent_olorUtil.multAlpha(ObsidianComponent_olorUtil.getThemeColor(), alpha);
         stack.push();
         stack.translate(rel.x, rel.y, rel.z);
         stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(bubble.sideYaw()));
         stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-210.0f));
         stack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotation));
         this.drawTexturedQuad(
            stack,
            -scale * 0.5f,
            -scale * 0.5f,
            scale,
            scale,
            color
         );
         stack.pop();
      }
   }
   public void drawTexturedQuad(MatrixStack stack, float x, float y, float width, float height, int color) {
      int r = color >> 16 & 255;
      int g = color >> (8) & (255);
      int b = color & 255;
      int a = color >> 24 & (255);
      if (a > 0) {
         Matrix4f mat = stack.peek().getPositionMatrix();
         BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         buffer.vertex(mat, x, y, 0.0f)
            .texture(0.0f, 0.0f)
            .color(r, g, b, a);
         buffer.vertex(mat, x, y + height, 0.0f)
            .texture(0.0f, 1.0f)
            .color(r, g, b, a);
         buffer.vertex(mat, x + width, y + height, 0.0f)
            .texture(1.0f, 1.0f)
            .color(r, g, b, a);
         buffer.vertex(mat, x + width, y, 0.0f)
            .texture(1.0f, 0.0f)
            .color(r, g, b, a);
         BufferRenderer.drawWithGlobalProgram(buffer.end());
      }
   }
   public Vec3d getHitSideDirection(LivingEntity target, Vec3d attackerPos) {
      Vec3d dir = attackerPos.subtract(target.getPos());
      dir = new Vec3d(dir.x, 0.0, dir.z);
      if (dir.lengthSquared() < 0.0001) {
         Vec3d fallback = target.getRotationVector();
         dir = new Vec3d(fallback.x, 0.0, fallback.z);
      }

      if (dir.lengthSquared() < 0.0001) {
         dir = new Vec3d(
            0.0,
            0.0,
            1.0
         );
      }

      return dir.normalize();
   }
   public Vec3d getHitPosition(LivingEntity target, Vec3d sideDir) {
      Vec3d head = new Vec3d(
         target.getX(),
         target.getY() + (double)target.getHeight() + 0.18,
         target.getZ()
      );
      return head.add(sideDir.multiply(0.1));
   }
}
