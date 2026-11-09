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
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class KillEffectModule extends Module {
   public static KillEffectModule INSTANCE = new KillEffectModule();
   private static final Identifier GLOW_TEX = Identifier.of("obsidian", "textures/particle/bloom.png");
   public static final float DURATION = 1.5F;
   public static final float HEIGHT = 4.0F;
   public static final float MAX_RADIUS = 1.0F;
   public static final int SLICES = 40;
   private final Map<Entity, Vec3d> trackedEntities = new IdentityHashMap<>();
   private final List<ObsidianComponent_333> effects = new ArrayList<>();

   public KillEffectModule() {
      super("KillEffect", "Эффект при исчезновении цели", Category.RENDER);
   }

   @Override
   public void onDisable() {
      this.trackedEntities.clear();
      this.effects.clear();
      super.onDisable();
   }

   @EventTarget
   public void onAttack(Event_358 event) {
      if (mc.player != null && mc.world != null) {
         Entity target = event.getTarget();
         if (target instanceof LivingEntity && target != mc.player) {
            this.trackedEntities.put(target, target.getPos());
         }
      }
   }

   @EventTarget
   public void onRender3D(RenderEvent event) {
      if (mc.world != null && mc.player != null) {
         long currentTime = System.currentTimeMillis();
         Iterator<Entry<Entity, Vec3d>> trackIterator = this.trackedEntities.entrySet().iterator();

         while (trackIterator.hasNext()) {
            Entry<Entity, Vec3d> entry = trackIterator.next();
            Entity entity = entry.getKey();
            if (!entity.isRemoved() && entity.isAlive()) {
               entry.setValue(entity.getPos());
            } else {
               this.effects.add(new ObsidianComponent_333(entry.getValue(), currentTime));
               trackIterator.remove();
            }
         }

         RenderSystem.enableBlend();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.disableCull();
         RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE, SrcFactor.ZERO, DstFactor.ONE);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
         RenderSystem.setShaderTexture(0, GLOW_TEX);
         Iterator<ObsidianComponent_333> effectIterator = this.effects.iterator();

         while (effectIterator.hasNext()) {
            ObsidianComponent_333 effect = effectIterator.next();
            float progress = (float)(currentTime - effect.startTime) / 1500.0f;
            if (progress >= 1.0f) {
               effectIterator.remove();
            } else {
               this.renderEffect(event.getMatrices(), effect, mc.gameRenderer.getCamera().getPos(), progress);
            }
         }

         RenderSystem.enableCull();
         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
      }
   }
   public void renderEffect(MatrixStack matrices, ObsidianComponent_333 effect, Vec3d cameraPos, float progress) {
      int color = ObsidianComponent_olorUtil.getThemeColor();
      float r = (float)(color >> 16 & 255)
         / 255.0f;
      float g = (float)(color >> 8 & (255))
         / 255.0f;
      float b = (float)(color & 255) / 255.0f;
      float globalAlpha = progress < 0.15000000596046448f
         ? progress / 0.15000000596046448f
         : (
            progress > 0.75f
               ? (1.0f - progress) / 0.25f
               : 1.0f
         );
      float sliceHeight = 0.10000000149011612f;

      for (int i = 0; i < 40; i++) {
         float t = (float)i / 40.0f;
         float y = t * 4.0f;
         float radius = 1.0f
            * MathHelper.sin((float)(3.141592653589793 * (double)t));
         float sliceAlpha = (
               1.0f
                  - Math.abs(
                        2.0f * t - 1.0f
                     )
                     * 0.25f
            )
            * globalAlpha;
         Vec3d pos = effect.position
            .add(
               0.0,
               (double)y,
               0.0
            );
         this.renderGlow(
            matrices,
            cameraPos,
            pos,
            radius * 2.0999999046325684f,
            r,
            g,
            b,
            sliceAlpha * 0.2199999988079071f
         );
         this.renderGlow(
            matrices,
            cameraPos,
            pos,
            radius * 1.149999976158142f,
            r,
            g,
            b,
            sliceAlpha * 0.47999998927116394f
         );
         this.renderGlow(
            matrices,
            cameraPos,
            pos,
            radius * 0.550000011920929f,
            r,
            g,
            b,
            sliceAlpha * 0.8500000238418579f
         );
      }

      for (int i = 0; i < 10; i++) {
         float t = (float)i / 10.0f;
         float spread = 1.0f - t;
         float bottomRadius = 3.5999999046325684f * spread;
         float bottomAlpha = spread * spread * globalAlpha * 0.3799999952316284f;
         Vec3d bPos = effect.position
            .add(
               0.0,
               (double)(t * 0.44999998807907104f),
               0.0
            );
         this.renderGlow(matrices, cameraPos, bPos, bottomRadius, r, g, b, bottomAlpha);
         this.renderGlow(
            matrices,
            cameraPos,
            bPos,
            bottomRadius * 0.3499999940395355f,
            r,
            g,
            b,
            bottomAlpha * 1.7000000476837158f
         );
      }
   }
   public void renderGlow(MatrixStack matrices, Vec3d cameraPos, Vec3d position, float size, float r, float g, float b, float a) {
      if (!(a <= 0.009999999776482582f)) {
         matrices.push();
         matrices.translate(position.x - cameraPos.x, position.y - cameraPos.y, position.z - cameraPos.z);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mc.gameRenderer.getCamera().getYaw()));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mc.gameRenderer.getCamera().getPitch()));
         Matrix4f matrix = matrices.peek().getPositionMatrix();
         float half = size * 0.5f;
         int rInt = Math.min(255, (int)(r * 255.0f));
         int gInt = Math.min(255, (int)(g * 255.0f));
         int bInt = Math.min(255, (int)(b * 255.0f));
         int aInt = Math.min(255, (int)(a * 255.0f));
         BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         buffer.vertex(matrix, -half, -half, 0.0f)
            .texture(0.0f, 1.0f)
            .color(rInt, gInt, bInt, aInt);
         buffer.vertex(matrix, -half, half, 0.0f)
            .texture(0.0f, 0.0f)
            .color(rInt, gInt, bInt, aInt);
         buffer.vertex(matrix, half, half, 0.0F).texture(1.0F, 0.0f).color(rInt, gInt, bInt, aInt);
         buffer.vertex(matrix, half, -half, 0.0f)
            .texture(1.0f, 1.0f)
            .color(rInt, gInt, bInt, aInt);
         BufferRenderer.drawWithGlobalProgram(buffer.end());
         matrices.pop();
      }
   }
}
