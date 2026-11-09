package wtf.obsidian.modules.misc;

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
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class LayerCooldownModule extends Module {
   public static LayerCooldownModule INSTANCE = new LayerCooldownModule();
   public static final long DELAYED_SCAN_MS = 250L;
   public static final int SEARCH_RADIUS = 4;
   public static final int SEARCH_HEIGHT = 4;
   public static final int MAX_TIMERS = 100;
   public static final float TIMER_SECONDS = 19.5F;
   public static final float MAX_DISTANCE = 96.0F;
   public static final double TIMER_Y_OFFSET = 0.6;
   private static final ItemStack LAYER_ICON = new ItemStack(Items.DRIED_KELP);
   private final Matrix4f lastProjectionMatrix = new Matrix4f();
   private final Quaternionf lastCameraRotation = new Quaternionf();
   public Vec3d lastCameraPos = Vec3d.ZERO;
   public boolean hasProjection;
   private final List<ObsidianComponent_302> timers = new ArrayList<>();
   private final List<ObsidianComponent_213> pendingScans = new ArrayList<>();

   public LayerCooldownModule() {
      super("LayerCooldown", "Показывает таймер возле поставленного пласта", Category.MISC);
   }

   @Override
   public void onDisable() {
      this.timers.clear();
      this.pendingScans.clear();
      this.hasProjection = false;
      super.onDisable();
   }

   @EventTarget
   public void onPacket(PacketEvent event) {
      if (event.getType() == PacketType.RECEIVE && mc.world != null && mc.player != null) {
         if (event.getPacket() instanceof PlaySoundS2CPacket packet) {
            String sound = this.getSoundPath(packet);
            if (sound != null) {
               Vec3d soundPos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
               BlockPos blockPos = BlockPos.ofFloored(soundPos);
               if ("block.piston.extend".equals(sound)) {
                  this.addTimer(blockPos, soundPos);
               } else {
                  if (this.isDelayedTrapSound(sound)) {
                     this.pendingScans
                        .add(new ObsidianComponent_213(blockPos, System.currentTimeMillis() + (250L)));
                  }
               }
            }
         }
      }
   }

   @EventTarget(
      priority = 100
   )
   public void onRender3D(RenderEvent event) {
      if (mc.world != null && mc.player != null) {
         this.hasProjection = true;
         this.lastProjectionMatrix.set(event.getProjectionMatrix());
         this.lastCameraRotation.set(event.getCamera().getRotation());
         this.lastCameraPos = event.getCamera().getPos();
         this.processPendingScans();
      }
   }

   @EventTarget(
      priority = 100
   )
   public void onRender2D(EventRender2D event) {
      if (this.hasProjection && mc.world != null && mc.player != null) {
         long now = System.currentTimeMillis();
         this.timers.removeIf(timerx -> (timerx.endTime <= now));

         while (this.timers.size() > 100) {
            this.timers.remove(0);
         }

         if (!this.timers.isEmpty()) {
            MatrixStack matrices = event.getContext().getMatrices();
            FontRenderer font = ObsidianComponent_201.getFont("sf_regular", 13);
            if (font != null) {
               float maxDistSq = 9216.0f;

               for (int i = 0; i < this.timers.size(); i++) {
                  ObsidianComponent_302 timer = this.timers.get(i);
                  if (!(mc.player.squaredDistanceTo(timer.pos) > (double)maxDistSq)) {
                     Vec3d screen = this.worldToScreen(timer.pos);
                     if (screen != null) {
                        float seconds = Math.max(
                           0.0f,
                           (float)(timer.endTime - now) / 1000.0f
                        );
                        this.drawTimer(event.getContext(), matrices, font, (float)screen.x, (float)screen.y, seconds);
                     }
                  }
               }
            }
         }
      }
   }
   public void processPendingScans() {
      if (!this.pendingScans.isEmpty() && mc.world != null) {
         long now = System.currentTimeMillis();
         Iterator<ObsidianComponent_213> iterator = this.pendingScans.iterator();

         while (iterator.hasNext()) {
            ObsidianComponent_213 scan = iterator.next();
            if (scan.runAt <= now) {
               BlockPos found = this.findLayerLikeBlock(scan.center);
               Vec3d pos = found == null
                  ? Vec3d.ofCenter(scan.center)
                  : new Vec3d(
                     (double)found.getX() + 0.5,
                     (double)found.getY() + 0.65,
                     (double)found.getZ() + 0.5
                  );
               this.addTimer(found == null ? scan.center : found, pos);
               iterator.remove();
            }
         }
      }
   }
   public BlockPos findLayerLikeBlock(BlockPos center) {
      BlockPos best = null;
      double bestDistance = 179769313486231570814527423731704356798070567525844996598917476803157260780028538760589558632766878171540458953514382464234321326889464182768467546703537516986049910576551282076245490090389328944075868508455133942304583236903222948165808559332123348274797826204144723168738177180919299881250404026184124858368.0;

      for (int x = -4; x <= (4); x++) {
         for (int y = -4; y <= 4; y++) {
            for (int z = -4; z <= (4); z++) {
               BlockPos pos = center.add(x, y, z);
               BlockState state = mc.world.getBlockState(pos);
               if (this.isLayerLikeBlock(state)) {
                  double distance = pos.getSquaredDistance(center);
                  if (distance < bestDistance) {
                     bestDistance = distance;
                     best = pos;
                  }
               }
            }
         }
      }

      return best;
   }
   public boolean isLayerLikeBlock(BlockState state) {
      if (state != null && !state.isAir()) {
         Block block = state.getBlock();
         return (!(block != Blocks.PISTON
               && block != Blocks.STICKY_PISTON
               && block != Blocks.MOVING_PISTON
               && block != Blocks.DRIED_KELP_BLOCK
               && block != Blocks.ANVIL
               && block != Blocks.CHIPPED_ANVIL
               && block != Blocks.DAMAGED_ANVIL));
      } else {
         return false;
      }
   }
   public void addTimer(BlockPos blockPos, Vec3d renderPos) {
      long endTime = System.currentTimeMillis() + (19500L);

      for (int i = 0; i < this.timers.size(); i++) {
         ObsidianComponent_302 timer = this.timers.get(i);
         if (timer.blockPos.getSquaredDistance(blockPos) <= 2.25) {
            this.timers
               .set(
                  i,
                  new ObsidianComponent_302(
                     blockPos,
                     renderPos.add(
                        0.0,
                        0.6,
                        0.0
                     ),
                     endTime
                  )
               );
            return;
         }
      }

      this.timers
         .add(
            new ObsidianComponent_302(
               blockPos,
               renderPos.add(
                  0.0,
                  0.6,
                  0.0
               ),
               endTime
            )
         );
   }
   public boolean isDelayedTrapSound(String sound) {
      return (!(!"block.anvil.place".equals(sound) && !"entity.zombie_horse.death".equals(sound) && !"entity.ender_dragon.growl".equals(sound)));
   }
   public String getSoundPath(PlaySoundS2CPacket packet) {
      try {
         return Registries.SOUND_EVENT.getId((SoundEvent)packet.getSound().value()).getPath();
      } catch (Exception var3) {
         return null;
      }
   }
   public void drawTimer(DrawContext context, MatrixStack matrices, FontRenderer font, float x, float y, float seconds) {
      String text = this.formatOneDecimal(seconds) + "с";
      float textWidth = font.getStringWidth(text);
      float iconSize = 10.0f;
      float iconScale = 0.6200000047683716f;
      float gap = 3.0f;
      float boxWidth = iconSize + gap + textWidth + 8.0f;
      float boxHeight = 12.5f;
      float boxX = x - boxWidth * 0.5f;
      float boxY = y - boxHeight * 0.5f;
      int themeColor = ObsidianComponent_olorUtil.getThemeColor();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderUtil.drawDefaultHudThemedPanel(
         matrices,
         boxX,
         boxY,
         boxWidth,
         boxHeight,
         2.0f,
         3.0f,
         themeColor
      );
      this.drawItemIcon(
         context,
         matrices,
         boxX + 4.0f,
         boxY + 1.25f,
         iconScale
      );
      font.drawString(
         matrices,
         text,
         boxX + 4.0f + iconSize + gap,
         boxY + 4.550000190734863f,
         -1
      );
      RenderSystem.disableBlend();
   }
   public String formatOneDecimal(float value) {
      int scaled = Math.round(value * 10.0f);
      return scaled / (10) + "." + Math.abs(scaled % 10);
   }
   public void drawItemIcon(DrawContext context, MatrixStack matrices, float x, float y, float scale) {
      if (context != null) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         matrices.push();
         matrices.translate(x, y, 0.0f);
         matrices.scale(scale, scale, 1.0f);
         context.drawItem(LAYER_ICON, 0, 0);
         matrices.pop();
         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
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
            float screenX = (ndcX * 0.5f + 0.5f)
               * (float)mc.getWindow().getScaledWidth();
            float screenY = (
                  1.0f
                     - (ndcY * 0.5f + 0.5f)
               )
               * (float)mc.getWindow().getScaledHeight();
            if (!Float.isNaN(screenX) && !Float.isNaN(screenY) && !Float.isInfinite(screenX) && !Float.isInfinite(screenY)) {
               return !(screenX < -400.0f)
                     && !(screenY < -400.0f)
                     && !(screenX > (float)(mc.getWindow().getScaledWidth() + 400))
                     && !(screenY > (float)(mc.getWindow().getScaledHeight() + (400)))
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
}
