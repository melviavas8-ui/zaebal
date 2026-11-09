package wtf.obsidian.hud;

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
























import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class TargetHudElement extends DraggableHudElement {
   private final AnimationUtil alphaAnimation = new AnimationUtil(
      0.0f, 9.0f, ObsidianComponent_57.QUAD_OUT
   );
   private final AnimationUtil unusualRevealAnimation = new AnimationUtil(
      0.0f, 4.599999904632568f, ObsidianComponent_57.QUAD_OUT
   );
   private final AnimationUtil hpAnimation = new AnimationUtil(
      1.0f, 9.199999809265137f, ObsidianComponent_57.QUAD_OUT
   );
   private final AnimationUtil hpTrailAnimation = new AnimationUtil(
      1.0f, 7.400000095367432f, ObsidianComponent_57.QUAD_OUT
   );
   private final AnimationUtil hpValueAnimation = new AnimationUtil(
      20.0f, 7.0f, ObsidianComponent_57.QUAD_OUT
   );
   private final AnimationUtil ABValueAnimation = new AnimationUtil(
      0.0f, 7.0f, ObsidianComponent_57.QUAD_OUT
   );
   private final AnimationUtil goldenHpAnimation = new AnimationUtil(
      0.0f, 9.199999809265137f, ObsidianComponent_57.QUAD_OUT
   );
   private final AnimationUtil goldenHpTrailAnimation = new AnimationUtil(
      0.0f, 7.400000095367432f, ObsidianComponent_57.QUAD_OUT
   );
   private final AnimationUtil goldenAlphaAnimation = new AnimationUtil(
      0.0f, 9.0f, ObsidianComponent_57.QUAD_OUT
   );
   private final List<ObsidianComponent_67> headParticles = new ObjectArrayList();
   public LivingEntity lastTarget;
   public float maxAbsorption = 20.0f;
   private boolean headParticlesEnabled = true;
   public boolean healthBarStyleEnabled = false;
   private long lastParticleUpdateNs = System.nanoTime();
   public LivingEntity particleTarget;
   public int lastTargetHurtTime = 0;
   private int cachedBarThemeColor = ObsidianComponent_olorUtil.rgba(
      124, 91, 242, 255
   );
   private int cachedBarThemeColor2 = ObsidianComponent_olorUtil.rgba(
      93,
      67,
      175,
      255
   );
   public Framebuffer burnBuffer;
   public int burnBufferWidth = -1;
   public int burnBufferHeight = -1;
   public final ItemStack[] displayItems = new ItemStack[6];
   public final ItemStack[] armorScratch = new ItemStack[4];
   private final AnimationUtil scaleAnimation = new AnimationUtil(
      0.0f, 12.0f, ObsidianComponent_57.BACK_OUT
   );
   private final AnimationUtil hideScaleAnimation = new AnimationUtil(
      1.0f, 8.0f, ObsidianComponent_57.BACK_IN
   );
   private boolean wasVisible = false;

   public TargetHudElement(ObsidianComponent_280 draggable) {
      super(draggable);
   }
   public FontRenderer issue(int size) {
      return ObsidianComponent_201.getFont("suisse", size);
   }

   public boolean isHeadParticlesEnabled() {
      return this.headParticlesEnabled;
   }

   public void setHeadParticlesEnabled(boolean headParticlesEnabled) {
      this.headParticlesEnabled = headParticlesEnabled;
      if (!headParticlesEnabled) {
         this.headParticles.clear();
      }
   }

   public boolean isHealthBarStyleEnabled() {
      return this.healthBarStyleEnabled;
   }

   public void setHealthBarStyleEnabled(boolean healthBarStyleEnabled) {
      this.healthBarStyleEnabled = healthBarStyleEnabled;
   }
   public int interpolateColor(int color1, int color2, float ratio) {
      ratio = MathHelper.clamp(ratio, 0.0f, 1.0f);
      int r1 = color1 >> 16 & (255);
      int g1 = color1 >> (8) & 255;
      int b1 = color1 & (255);
      int a1 = color1 >> (24) & (255);
      int r2 = color2 >> 16 & (255);
      int g2 = color2 >> (8) & 255;
      int b2 = color2 & (255);
      int a2 = color2 >> (24) & 255;
      int r = (int)((float)r1 + (float)(r2 - r1) * ratio);
      int g = (int)((float)g1 + (float)(g2 - g1) * ratio);
      int b = (int)((float)b1 + (float)(b2 - b1) * ratio);
      int a = (int)((float)a1 + (float)(a2 - a1) * ratio);
      return a << (24) | r << 16 | g << 8 | b;
   }
   public void ensureBurnBuffer() {
      if (mc != null && mc.getWindow() != null) {
         int w = mc.getWindow().getFramebufferWidth();
         int h = mc.getWindow().getFramebufferHeight();
         if (this.burnBuffer == null || this.burnBufferWidth != w || this.burnBufferHeight != h) {
            if (this.burnBuffer != null) {
               this.burnBuffer.delete();
            }

            this.burnBuffer = new SimpleFramebuffer(w, h, true);
            this.burnBufferWidth = w;
            this.burnBufferHeight = h;
         }
      }
   }
   public int collectDisplayItems(LivingEntity target) {
      int armorCount = 0;

      for (ItemStack stack : target.getArmorItems()) {
         if (armorCount < this.armorScratch.length) {
            this.armorScratch[armorCount++] = stack;
         }
      }

      int count = 0;

      for (int i = armorCount - 1; i >= 0; i--) {
         this.displayItems[count++] = this.armorScratch[i];
         this.armorScratch[i] = ItemStack.EMPTY;
      }

      ItemStack mainHand = target.getMainHandStack();
      if (!mainHand.isEmpty()) {
         this.displayItems[count++] = mainHand;
      }

      ItemStack offHand = target.getOffHandStack();
      if (!offHand.isEmpty()) {
         this.displayItems[count++] = offHand;
      }

      return count;
   }
   public boolean beginBurnPassIfNeeded(boolean unusualAnimation) {
      if (!unusualAnimation) {
         return false;
      } else {
         this.ensureBurnBuffer();
         if (this.burnBuffer == null) {
            return false;
         } else {
            this.burnBuffer
               .setClearColor(
                  0.0f,
                  0.0f,
                  0.0f,
                  0.0f
               );
            this.burnBuffer.clear();
            this.burnBuffer.beginWrite(false);
            return true;
         }
      }
   }
   public void applyHudTransform(MatrixStack matrices, float x, float y, float width, float height, float scaleX, float scaleY) {
      float centerX = x + width * 0.5f;
      float centerY = y + height * 0.5f;
      matrices.translate(centerX, centerY, 0.0f);
      matrices.scale(scaleX, scaleY, 1.0f);
      matrices.translate(-centerX, -centerY, 0.0f);
   }
   public void updateAndRenderHeadParticles(MatrixStack matrices, LivingEntity target, float headX, float headY, float headSize, float alpha, int themeColor) {
      if (target != null && !(alpha <= 0.019999999552965164f)) {
         long now = System.nanoTime();
         float deltaTicks = MathHelper.clamp(
            (float)(now - this.lastParticleUpdateNs) / 1000000000.0f * 60.0f,
            0.20000000298023224f,
            3.0f
         );
         this.lastParticleUpdateNs = now;
         if (this.particleTarget != target) {
            this.headParticles.clear();
            this.particleTarget = target;
            this.lastTargetHurtTime = Math.max(0, target.hurtTime);
         }

         ThreadLocalRandom random = ThreadLocalRandom.current();
         float centerX = headX + headSize * 0.5f;
         float centerY = headY + headSize * 0.5f;
         int hurtTime = Math.max(0, target.hurtTime);
         boolean spawnBurst = (!(hurtTime <= 0 || hurtTime <= this.lastTargetHurtTime && hurtTime % 3 != 0));
         this.lastTargetHurtTime = hurtTime;
         if (spawnBurst) {
            int burstCount = (1) + random.nextInt(2);

            for (int n = 0;
               n < burstCount && this.headParticles.size() < 14;
               n++
            ) {
               float angle = (float)(
                  random.nextDouble()
                     * 3.141592653589793
                     * 2.0
               );
               float radius = random.nextFloat() * headSize * 0.23999999463558197f;
               float spreadAngle = (float)(
                  random.nextDouble()
                     * 3.141592653589793
                     * 2.0
               );
               float speed = 0.5799999833106995f + random.nextFloat() * 0.8999999761581421f;
               ObsidianComponent_67 p = new ObsidianComponent_67();
               p.x = centerX + MathHelper.cos(angle) * radius;
               p.y = centerY + MathHelper.sin(angle) * radius;
               p.vx = MathHelper.cos(spreadAngle) * speed + (p.x - centerX) * 0.02500000037252903f;
               p.vy = MathHelper.sin(spreadAngle) * speed + (p.y - centerY) * 0.02500000037252903f;
               p.size = 3.799999952316284f
                  + random.nextFloat() * 1.399999976158142f;
               p.age = 0.0f;
               p.maxAge = 74.0f
                  + random.nextFloat() * 42.0f;
               this.headParticles.add(p);
            }
         }

         for (int i = this.headParticles.size() - 1; i >= 0; i--) {
            ObsidianComponent_67 p = this.headParticles.get(i);
            p.age += deltaTicks;
            if (p.age >= p.maxAge) {
               this.headParticles.remove(i);
            } else {
               p.x = p.x + p.vx * deltaTicks;
               p.y = p.y + p.vy * deltaTicks;
               p.vx = p.vx * (float)Math.pow(0.9750000238418579, (double)deltaTicks);
               p.vy = p.vy * (float)Math.pow(0.9750000238418579, (double)deltaTicks);
               p.vy = p.vy + 0.0012000000569969416f * deltaTicks;
               float life = 1.0f - p.age / p.maxAge;
               float smoothLife = life
                  * life
                  * (3.0f - 2.0f * life);
               float particleAlpha = alpha * smoothLife;
               if (!(particleAlpha <= 0.019999999552965164f)) {
                  float drawX = p.x - p.size * 0.5f;
                  float drawY = p.y - p.size * 0.5f;
                  int coreColor = ObsidianComponent_olorUtil.applyAlpha(themeColor, particleAlpha * 0.5799999833106995f);
                  RenderUtil.drawRoundedRect(
                     matrices, drawX, drawY, p.size, p.size, p.size * 0.44999998807907104f, coreColor
                  );
               }
            }
         }
      } else {
         this.headParticles.clear();
         this.particleTarget = target;
         this.lastTargetHurtTime = 0;
      }
   }
   public void drawTargetHudItem(EventRender2D eventRender, MatrixStack matrices, ItemStack stack, float slotX, float slotY, float itemScale) {
      if (!stack.isEmpty()) {
         matrices.push();
         matrices.translate(
            slotX + 4.0f,
            slotY + 4.0f,
            0.0f
         );
         matrices.scale(itemScale, itemScale, 1.0f);
         matrices.translate(
            -4.0f,
            -4.0f,
            0.0f
         );
         eventRender.getContext().drawItem(stack, 0, 0);
         matrices.pop();
      }
   }

   @Override
   public void onRender(EventRender2D eventRender) {
      if (!ModuleManager.interfaceModule.style.is("Obs")) {
         this.DefaultStyle(eventRender);
      } else {
         this.WaveStyle(eventRender);
      }

      super.onRender(eventRender);
   }

   public void DefaultStyle(EventRender2D eventRender) {
      if (mc.player == null) {
         this.headParticles.clear();
         this.lastTargetHurtTime = 0;
         this.draggable.setWidth(0.0f);
         this.draggable.setHeight(0.0f);
      } else {
         KillAuraModule aura = ModuleManager.aura;
         boolean chatOpen = mc.currentScreen instanceof ChatScreen;
         LivingEntity auraTarget = aura != null ? aura.getTarget() : null;
         boolean showTargetHud = (!(!chatOpen && auraTarget == null));
         this.alphaAnimation
            .setSpeed(showTargetHud ? 9.0f : 5.0f);
         this.alphaAnimation
            .update(showTargetHud ? 1.0f : 0.0f);
         float alpha = MathHelper.clamp(
            this.alphaAnimation.getValue(), 0.0f, 1.0f
         );
         if (showTargetHud) {
            this.lastTarget = (LivingEntity)(chatOpen ? mc.player : auraTarget);
         }

         LivingEntity target = (LivingEntity)(showTargetHud ? (chatOpen ? mc.player : auraTarget) : this.lastTarget);
         if (target != null && !(alpha <= 0.01F)) {
            float currentAbsorption = target.getAbsorptionAmount();
            boolean hasAbsorption = (currentAbsorption > 0.0F);
            this.goldenAlphaAnimation
               .setSpeed(hasAbsorption ? 9.0f : 5.0f);
            this.goldenAlphaAnimation
               .update(
                  hasAbsorption
                     ? 1.0f
                     : 0.0f
               );
            float goldenAlpha = MathHelper.clamp(
               this.goldenAlphaAnimation.getValue(),
               0.0f,
               1.0f
            );
            float x = this.draggable.getX();
            float y = this.draggable.getY();
            int colorTheme;
            if (!Obsidian.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
               colorTheme = Obsidian.INSTANCE.themeStorage.getThemes().getTheme().color[0];
            } else {
               colorTheme = ObsidianComponent_olorUtil.getThemeColor();
            }

            int colorTheme2;
            if (!Obsidian.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
               colorTheme2 = ObsidianComponent_olorUtil.darken(
                  Obsidian.INSTANCE.themeStorage.getThemes().getTheme().color[0],
                  0.4000000059604645f
               );
            } else {
               colorTheme2 = ObsidianComponent_olorUtil.getThemeColor();
            }

            if (showTargetHud) {
               this.cachedBarThemeColor = colorTheme;
               this.cachedBarThemeColor2 = colorTheme2;
            }

            int barThemeColor = showTargetHud ? colorTheme : this.cachedBarThemeColor;
            int barThemeColor2 = showTargetHud ? colorTheme2 : this.cachedBarThemeColor2;
            float maxHealth = Math.max(1.0F, target.getMaxHealth());
            float maxAB = Math.max(1.0f, this.maxAbsorption);
            float targetHealthForAnim = showTargetHud ? target.getHealth() : 0.0f;
            float targetABForAnim = showTargetHud ? currentAbsorption : 0.0f;
            this.hpValueAnimation.update(targetHealthForAnim);
            this.ABValueAnimation.update(targetABForAnim);
            float animatedHealthValue = MathHelper.clamp(
               this.hpValueAnimation.getValue(), 0.0f, maxHealth
            );
            float animatedABValue = MathHelper.clamp(this.ABValueAnimation.getValue(), 0.0f, maxAB);
            float healthProgress = MathHelper.clamp(targetHealthForAnim / maxHealth, 0.0f, 1.0F);
            this.hpAnimation.update(healthProgress);
            float hpProgressAnimated = MathHelper.clamp(
               this.hpAnimation.getValue(),
               0.0f,
               1.0f
            );
            if (hpProgressAnimated > this.hpTrailAnimation.getValue()) {
               this.hpTrailAnimation
                  .setValue(MathHelper.lerp(0.7799999713897705f, this.hpTrailAnimation.getValue(), hpProgressAnimated));
            } else {
               this.hpTrailAnimation.update(hpProgressAnimated);
            }

            float hpTrailProgressAnimated = MathHelper.clamp(
               this.hpTrailAnimation.getValue(),
               0.0f,
               1.0f
            );
            boolean hidingHud = (!showTargetHud);
            if (hidingHud) {
               hpTrailProgressAnimated = hpProgressAnimated;
            }

            float absorptionProgress = MathHelper.clamp(currentAbsorption / maxAB, 0.0f, 1.0F);
            this.goldenHpAnimation.update(absorptionProgress);
            float goldenProgressAnimated = MathHelper.clamp(this.goldenHpAnimation.getValue(), 0.0F, 1.0F);
            if (goldenProgressAnimated > this.goldenHpTrailAnimation.getValue()) {
               this.goldenHpTrailAnimation
                  .setValue(
                     MathHelper.lerp(0.7799999713897705f, this.goldenHpTrailAnimation.getValue(), goldenProgressAnimated)
                  );
            } else {
               this.goldenHpTrailAnimation.update(goldenProgressAnimated);
            }

            float goldenTrailProgressAnimated = MathHelper.clamp(
               this.goldenHpTrailAnimation.getValue(),
               0.0f,
               1.0f
            );
            if (hidingHud || !hasAbsorption) {
               goldenTrailProgressAnimated = goldenProgressAnimated;
            }

            String name = target.getName().getString();
            Object[] var10001 = new Object[1];
            var10001[0] = animatedHealthValue;
            String hpText = "HP: " + String.format("%.1f", var10001);
            var10001 = new Object[1];
            var10001[0] = animatedABValue;
            String abText = " (" + String.format("%.1f", var10001) + ")";
            float padding = 4.0f;
            float headSize = 27.5F;
            float gap = 5.0f;
            float rightPad = 6.0f;
            float textX = x + padding + headSize + gap - 5.0f;
            float textWidth = Math.max(
               this.issue(13).getWidth(name), this.issue(12).getWidth(hpText)
            );
            float width = Math.max(92.5f, padding + headSize + gap + textWidth + rightPad)
               - 3.5f;
            float height = 32.0f;
            float headX = x + padding;
            float headY = y + 3.5f;
            float textMaxWidth = Math.max(10.0f, width - (textX - x) - rightPad)
               + 2.0f;
            MatrixStack matrices = eventRender.getContext().getMatrices();
            boolean drawSquares = this.isUnusualRectType();
            int drawAlphaInt = (int)(255.0f * alpha);
            matrices.push();
            RenderUtil.drawDefaultHudPanel(
               matrices,
               x,
               y,
               width,
               height,
               4.0f,
               4.5f,
               ObsidianComponent_olorUtil.applyAlpha(
                  ObsidianComponent_olorUtil.rgba(50, 50, 50, 255), alpha
               ),
               ObsidianComponent_olorUtil.applyAlpha(ObsidianComponent_olorUtil.darken(colorTheme, 0.15000000596046448f), alpha),
               ObsidianComponent_olorUtil.applyAlpha(ObsidianComponent_olorUtil.darken(colorTheme, 0.05000000074505806f), alpha)
            );
            if (drawSquares && alpha > 0.05999999865889549f) {
               RenderUtil.drawHudSquarePattern(
                  matrices, x + 8.0f, y, width, height, ObsidianComponent_olorUtil.applyAlpha(barThemeColor, alpha)
               );
            }

            if (this.headParticlesEnabled) {
               this.updateAndRenderHeadParticles(
                  matrices,
                  target,
                  headX - 1.850000023841858f,
                  headY - 1.0f,
                  headSize,
                  alpha,
                  barThemeColor
               );
            } else {
               this.headParticles.clear();
            }

            if (target instanceof PlayerEntity playerEntity) {
               RenderUtil.drawPlayerHead(
                  matrices,
                  playerEntity.getUuid(),
                  headX - 1.850000023841858f,
                  headY - 1.0f,
                  headSize,
                  3.5f,
                  alpha,
                  0.0f
               );
            } else {
               RenderUtil.drawTargetHudDefaultPlaceholder(
                  matrices,
                  headX - 1.850000023841858f,
                  headY - 1.0f,
                  alpha
               );
               this.issue(26)
                  .drawCenteredString(
                     matrices,
                     "?",
                     (double)(headX + headSize / 2.25f),
                     (double)headY + 7.5,
                     ObsidianComponent_olorUtil.rgba(
                        220, 220, 220, drawAlphaInt
                     )
                  );
            }

            this.issue(14)
               .drawStringWithFade(
                  matrices,
                  name,
                  textX + 0.699999988079071f,
                  y + 5.5f,
                  textMaxWidth,
                  ObsidianComponent_olorUtil.rgba(255, 255, 255, drawAlphaInt)
               );
            this.issue(13)
               .drawStringWithFade(
                  matrices,
                  hpText,
                  textX + 1.0f,
                  y + 14.5f,
                  textMaxWidth,
                  ObsidianComponent_olorUtil.rgba(
                     232, 232, 232, drawAlphaInt
                  )
               );
            if (goldenAlpha > 0.009999999776482582f) {
               float hpTextWidth = this.issue(13).getWidth(hpText);
               float abTextX = textX + 1.0f + hpTextWidth;
               int goldenAlphaInt = (int)(255.0f * goldenAlpha * alpha);
               this.issue(13)
                  .drawGradientStringHorizontal(
                     matrices,
                     abText,
                     abTextX,
                     y + 14.5f,
                     ObsidianComponent_olorUtil.rgba(236, 183, 39, goldenAlphaInt),
                     ObsidianComponent_olorUtil.rgba(
                        147,
                        108,
                        16,
                        goldenAlphaInt
                     )
                  );
            }

            float barY = y + height - 10.449999809265137f;
            float barW = Math.max(19.0f, width - rightPad - (textX - x));
            float barH = 3.8499999046325684f;
            int barBgLeft;
            int barBgRight;
            int barTrailLeft;
            int barTrailRight;
            int barFillLeft;
            int barFillRight;
            if (this.healthBarStyleEnabled) {
               int redDarkBg = ObsidianComponent_olorUtil.rgba(
                  70,
                  5,
                  10,
                  (int)(115.0f * alpha)
               );
               int greenDarkBg = ObsidianComponent_olorUtil.rgba(
                  0,
                  70,
                  0,
                  (int)(115.0f * alpha)
               );
               int redBrightBg = ObsidianComponent_olorUtil.rgba(
                  155,
                  5,
                  15,
                  (int)(115.0f * alpha)
               );
               int greenBrightBg = ObsidianComponent_olorUtil.rgba(
                  55,
                  205,
                  15,
                  (int)(115.0f * alpha)
               );
               barBgLeft = this.interpolateColor(redDarkBg, greenDarkBg, healthProgress);
               barBgRight = this.interpolateColor(redBrightBg, greenBrightBg, healthProgress);
               int redDarkTrail = ObsidianComponent_olorUtil.rgba(
                  70,
                  5,
                  10,
                  (int)(200.0f * alpha)
               );
               int greenDarkTrail = ObsidianComponent_olorUtil.rgba(
                  0,
                  70,
                  0,
                  (int)(200.0f * alpha)
               );
               int redBrightTrail = ObsidianComponent_olorUtil.rgba(
                  155,
                  5,
                  15,
                  (int)(200.0f * alpha)
               );
               int greenBrightTrail = ObsidianComponent_olorUtil.rgba(
                  55,
                  205,
                  15,
                  (int)(200.0f * alpha)
               );
               barTrailLeft = this.interpolateColor(redDarkTrail, greenDarkTrail, healthProgress);
               barTrailRight = this.interpolateColor(redBrightTrail, greenBrightTrail, healthProgress);
               int redDarkFill = ObsidianComponent_olorUtil.rgba(
                  70,
                  5,
                  10,
                  (int)(250.0f * alpha)
               );
               int greenDarkFill = ObsidianComponent_olorUtil.rgba(0, 70, 0, (int)(250.0F * alpha));
               int redBrightFill = ObsidianComponent_olorUtil.rgba(
                  155,
                  5,
                  15,
                  (int)(250.0f * alpha)
               );
               int greenBrightFill = ObsidianComponent_olorUtil.rgba(
                  55,
                  205,
                  15,
                  (int)(250.0f * alpha)
               );
               barFillLeft = this.interpolateColor(redDarkFill, greenDarkFill, healthProgress);
               barFillRight = this.interpolateColor(redBrightFill, greenBrightFill, healthProgress);
            } else {
               barBgLeft = ObsidianComponent_olorUtil.applyAlpha(
                  ObsidianComponent_olorUtil.darken(barThemeColor2, 0.7200000286102295f),
                  alpha * 0.25999999046325684f
               );
               barBgRight = ObsidianComponent_olorUtil.applyAlpha(
                  ObsidianComponent_olorUtil.darken(barThemeColor, 0.7200000286102295f),
                  alpha * 0.25999999046325684f
               );
               barTrailLeft = ObsidianComponent_olorUtil.applyAlpha(
                  ObsidianComponent_olorUtil.darken(barThemeColor2, 0.8999999761581421f),
                  alpha * 0.41999998688697815f
               );
               barTrailRight = ObsidianComponent_olorUtil.applyAlpha(ObsidianComponent_olorUtil.darken(barThemeColor, 0.8999999761581421f), alpha * 0.42F);
               barFillLeft = ObsidianComponent_olorUtil.applyAlpha(barThemeColor2, alpha);
               barFillRight = ObsidianComponent_olorUtil.applyAlpha(barThemeColor, alpha);
            }

            if (alpha > 0.02500000037252903f) {
               RenderUtil.drawGradientRect(
                  matrices,
                  textX,
                  barY,
                  barW + 3.0F,
                  barH + 4.25f,
                  1.9500000476837158f,
                  barBgLeft,
                  barBgRight,
                  true
               );
               if (!hidingHud) {
                  float trailProgressDraw = MathHelper.lerp(0.5799999833106995f, hpTrailProgressAnimated, hpProgressAnimated);
                  float trailW = barW * trailProgressDraw;
                  if (trailW > 1.149999976158142f) {
                     RenderUtil.drawGradientRect(
                        matrices,
                        textX,
                        barY,
                        trailW + 3.0f,
                        barH + 4.25f,
                        1.9500000476837158f,
                        barTrailLeft,
                        barTrailRight,
                        true
                     );
                  }
               }

               float filledW = barW * hpProgressAnimated;
               if (filledW > 1.149999976158142f) {
                  RenderUtil.drawGradientRect(
                     matrices,
                     textX,
                     barY,
                     filledW + 3.0f,
                     barH + 4.25f,
                     1.9500000476837158f,
                     barFillLeft,
                     barFillRight,
                     true
                  );
               }

               if (goldenAlpha > 0.009999999776482582f) {
                  float goldenBarAlpha = goldenAlpha * alpha;
                  int goldenBaseLeft = ObsidianComponent_olorUtil.rgba(
                     147,
                     108,
                     16,
                     255
                  );
                  int goldenBaseRight = ObsidianComponent_olorUtil.rgba(236, 183, 39, 255);
                  int goldenTrailLeft = ObsidianComponent_olorUtil.applyAlpha(
                     ObsidianComponent_olorUtil.darken(goldenBaseLeft, 0.699999988079071f),
                     goldenBarAlpha * 0.5f
                  );
                  int goldenTrailRight = ObsidianComponent_olorUtil.applyAlpha(
                     ObsidianComponent_olorUtil.darken(goldenBaseRight, 0.699999988079071f),
                     goldenBarAlpha * 0.5f
                  );
                  int goldenFillLeft = ObsidianComponent_olorUtil.applyAlpha(ObsidianComponent_olorUtil.darken(goldenBaseLeft, 0.7F), goldenBarAlpha);
                  int goldenFillRight = ObsidianComponent_olorUtil.applyAlpha(goldenBaseRight, goldenBarAlpha);
                  if (!hidingHud && hasAbsorption) {
                     float goldenTrailProgressDraw = MathHelper.lerp(
                        0.5799999833106995f, goldenTrailProgressAnimated, goldenProgressAnimated
                     );
                     float goldenTrailW = barW * goldenTrailProgressDraw;
                     if (goldenTrailW > 1.149999976158142f) {
                        RenderUtil.drawGradientRect(
                           matrices,
                           textX,
                           barY,
                           goldenTrailW + 3.0f,
                           barH + 4.25f,
                           1.9500000476837158f,
                           goldenTrailLeft,
                           goldenTrailRight,
                           true
                        );
                     }
                  }

                  float goldenFilledW = barW * goldenProgressAnimated;
                  if (goldenFilledW > 1.149999976158142f) {
                     RenderUtil.drawGradientRect(
                        matrices,
                        textX,
                        barY,
                        goldenFilledW + 3.0F,
                        barH + 4.25f,
                        1.9500000476837158f,
                        goldenFillLeft,
                        goldenFillRight,
                        true
                     );
                  }
               }
            }

            float itemY = y - 11.5f;
            float itemSpacing = 9.0f;
            float itemScale = 0.5f * alpha;
            int totalSlots = this.collectDisplayItems(target);
            float itemX = x + width - (float)totalSlots * itemSpacing - 3.0F;

            for (int itemIndex = 0; itemIndex < totalSlots; itemIndex++) {
               ItemStack stack = this.displayItems[itemIndex];
               if (!stack.isEmpty()) {
                  float slotX = itemX + (float)itemIndex * itemSpacing;
                  this.drawTargetHudItem(eventRender, matrices, stack, slotX, itemY, itemScale);
               }

               this.displayItems[itemIndex] = ItemStack.EMPTY;
            }

            matrices.pop();
            this.draggable.setWidth(width);
            this.draggable.setHeight(height);
         } else {
            this.headParticles.clear();
            this.lastTargetHurtTime = 0;
            this.draggable.setWidth(0.0f);
            this.draggable.setHeight(0.0f);
            this.goldenAlphaAnimation.setValue(0.0f);
            this.ABValueAnimation.setValue(0.0f);
            this.goldenHpAnimation.setValue(0.0f);
            this.goldenHpTrailAnimation.setValue(0.0f);
         }
      }
   }

   public void WaveStyle(EventRender2D eventRender) {
      if (mc.player == null) {
         this.draggable.setWidth(0.0f);
         this.draggable.setHeight(0.0f);
      } else {
         KillAuraModule aura = ModuleManager.aura;
         boolean chatOpen = mc.currentScreen instanceof ChatScreen;
         LivingEntity auraTarget = aura != null ? aura.getTarget() : null;
         boolean showTargetHud = (!(!chatOpen && auraTarget == null));
         this.alphaAnimation
            .update(
               showTargetHud
                  ? 1.0f
                  : 0.0f
            );
         if (showTargetHud && !this.wasVisible) {
            this.scaleAnimation.setValue(0.0f);
            this.hideScaleAnimation.setValue(1.0f);
            this.wasVisible = true;
         } else if (!showTargetHud && this.wasVisible) {
            this.hideScaleAnimation.setValue(1.0f);
            this.wasVisible = false;
         }

         if (showTargetHud) {
            this.scaleAnimation.update(1.0f);
            this.lastTarget = (LivingEntity)(chatOpen ? mc.player : auraTarget);
         } else {
            this.hideScaleAnimation.update(0.0f);
         }

         float scale;
         if (showTargetHud) {
            scale = MathHelper.clamp(
               this.scaleAnimation.getValue(), 0.0f, 1.0f
            );
         } else {
            scale = MathHelper.clamp(
               this.hideScaleAnimation.getValue(), 0.0f, 1.0f
            );
         }

         LivingEntity target = (LivingEntity)(showTargetHud ? (chatOpen ? mc.player : auraTarget) : this.lastTarget);
         if (target != null && !(scale <= 0.009999999776482582f)) {
            float x = this.draggable.getX();
            float y = this.draggable.getY();
            float padding = 3.0f;
            float width = 110.0f;
            float height = 46.0f;
            float health = target.getHealth();
            float maxHealth = Math.max(1.0f, target.getMaxHealth());
            this.hpValueAnimation.update(health);
            float animatedHealthValue = MathHelper.clamp(this.hpValueAnimation.getValue(), 0.0f, maxHealth);
            float healthProgress = MathHelper.clamp(
               health / maxHealth, 0.0f, 1.0f
            );
            this.hpAnimation.update(healthProgress);
            float hpProgressAnimated = MathHelper.clamp(
               this.hpAnimation.getValue(),
               0.0f,
               1.0f
            );
            if (hpProgressAnimated > this.hpTrailAnimation.getValue()) {
               this.hpTrailAnimation.setValue(hpProgressAnimated);
            } else {
               this.hpTrailAnimation.update(hpProgressAnimated);
            }

            MatrixStack matrices = eventRender.getContext().getMatrices();
            matrices.push();
            this.applyHudTransform(matrices, x, y, width, height, scale, scale);
            float visualAlpha = scale;
            int alphaInt = (int)(255.0f * scale);
            float entityBoxSize = height
               - padding * 2.0f
               - 4.0f;
            RenderUtil.drawTargetHudWaveFrame(matrices, x, y, width, height, padding, entityBoxSize, scale);
            matrices.pop();
            int entityX = (int)(x + padding + 3.0f);
            int entityY = (int)(y + padding + 3.0f);
            int entityX2 = (int)(
               x
                  + padding
                  + 3.0f
                  + entityBoxSize
                  - 2.0f
            );
            int entityY2 = (int)(
               y
                  + padding
                  + 3.0f
                  + entityBoxSize
                  - 2.0f
            );
            int entitySize = (int)(15.0f * scale);
            float entityCenterX = x
               + padding
               + 3.0f
               + (entityBoxSize - 2.0f) / 2.0f;
            float entityCenterY = y
               + padding
               + 3.0f
               + (entityBoxSize - 2.0f) / 3.0f;
            float yaw = target.bodyYaw;
            float pitch = target.getPitch();
            double yawRadians = Math.toRadians((double)(yaw + 180.0f));
            float lookX = entityCenterX
               + (float)(Math.sin(yawRadians) * 50.0);
            float lookY = entityCenterY - pitch;
            if (scale > 0.5f) {
               InventoryScreen.drawEntity(
                  eventRender.getContext(),
                  entityX,
                  entityY,
                  entityX2,
                  entityY2,
                  entitySize,
                  0.0f,
                  lookX,
                  lookY,
                  target
               );
            }

            matrices.push();
            this.applyHudTransform(matrices, x, y, width, height, scale, scale);
            String name = target.getName().getString();
            float textX = x + padding + entityBoxSize + 6.0f;
            float waveNameFadeMaxWidth = Math.max(
               8.0f, x + width - padding - 4.0f - textX
            );
            this.issue(14)
               .drawStringWithFade(
                  matrices,
                  name,
                  textX,
                  y + padding + 5.0f,
                  waveNameFadeMaxWidth,
                  ObsidianComponent_olorUtil.rgba(
                     255,
                     255,
                     255,
                     alphaInt
                  )
               );
            FontRenderer var10000 = this.issue(14);
            Object[] var10003 = new Object[1];
            var10003[0] = animatedHealthValue;
            var10000.draw(
               matrices,
               "HP: " + String.format("%.1f", var10003) + " | Dist: " + (int)target.distanceTo(mc.player),
               textX,
               y + padding + 20.0f,
               ObsidianComponent_olorUtil.rgba(255, 255, 255, alphaInt)
            );
            float heartsX = textX;
            float heartsY = y + padding + 15.0f;
            float heartSize = 5.0f;
            float heartSpacing = 0.5f;
            int totalHearts = 10;
            float healthPerHeart = maxHealth / (float)totalHearts;
            float currentHealth = health;
            int heartColor;
            if (health <= maxHealth * 0.25f) {
               heartColor = ObsidianComponent_olorUtil.rgba(255, 50, 50, alphaInt);
            } else if (health <= maxHealth * 0.5f) {
               heartColor = ObsidianComponent_olorUtil.rgba(255, 220, 0, alphaInt);
            } else {
               heartColor = ObsidianComponent_olorUtil.rgba(0, 255, 0, alphaInt);
            }

            int shadowColor = ObsidianComponent_olorUtil.applyAlpha(heartColor, scale * 0.5f);

            for (int i = 0; i < totalHearts; i++) {
               float hx = heartsX + (float)i * (heartSize + heartSpacing);
               RenderUtil.drawTargetHudHeartBase(matrices, hx, heartsY - 3.0f, visualAlpha);
               if (currentHealth > 0.0f) {
                  float fillAmount = MathHelper.clamp(
                     currentHealth / healthPerHeart,
                     0.0f,
                     1.0f
                  );
                  float filledWidth = heartSize * fillAmount;
                  if (filledWidth > 0.0f) {
                     RenderUtil.drawTargetHudHeartFill(
                        matrices, hx, heartsY - 3.0f, filledWidth, heartColor, shadowColor
                     );
                  }

                  currentHealth -= healthPerHeart;
               }
            }

            float itemX = textX - 1.0f;
            float itemY = y + padding + 28.0f;
            float itemSpacing = 10.0f;
            int waveThemeColor = !Obsidian.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")
               ? Obsidian.INSTANCE.themeStorage.getThemes().getTheme().color[0]
               : ObsidianComponent_olorUtil.getThemeColor();
            int waveSlotBorderColor = ObsidianComponent_olorUtil.applyAlpha(
               ObsidianComponent_olorUtil.rgba(
                  50,
                  50,
                  50,
                  255
               ),
               visualAlpha
            );
            int waveSlotTopColor = ObsidianComponent_olorUtil.applyAlpha(
               ObsidianComponent_olorUtil.darken(waveThemeColor, 0.15000000596046448f), visualAlpha
            );
            int waveSlotBottomColor = ObsidianComponent_olorUtil.applyAlpha(ObsidianComponent_olorUtil.darken(waveThemeColor, 0.05000000074505806f), visualAlpha);
            int totalSlots = this.collectDisplayItems(target);
            if (totalSlots > 0) {
               float containerX = itemX - 0.8500000238418579f;
               float containerY = itemY - 0.8500000238418579f;
               float containerW = (float)(totalSlots - (1)) * itemSpacing + 9.800000190734863f;
               float slotX = 9.800000190734863f;
            }

            float itemScale = 0.5f;

            for (int itemIndex = 0; itemIndex < totalSlots; itemIndex++) {
               ItemStack stack = this.displayItems[itemIndex];
               if (!stack.isEmpty()) {
                  float slotX = itemX + (float)itemIndex * itemSpacing;
                  this.drawTargetHudItem(eventRender, matrices, stack, slotX, itemY, itemScale);
               }

               this.displayItems[itemIndex] = ItemStack.EMPTY;
            }

            matrices.pop();
            this.draggable.setWidth(width);
            this.draggable.setHeight(height);
         } else {
            this.draggable.setWidth(0.0f);
            this.draggable.setHeight(0.0f);
         }
      }
   }
}
