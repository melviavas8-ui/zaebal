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
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class HudInterfaceModule extends Module {
   public static HudInterfaceModule INSTANCE = new HudInterfaceModule();
   private static final ConcurrentHashMap<String, Long> PERF_WARNINGS = new ConcurrentHashMap<>();
   private static final boolean PERF_DEBUG = Boolean.parseBoolean(System.getProperty("Obsidian.perf.debug", "false"));
   private static final long SLOW_HUD_ELEMENT_NANOS = Long.getLong("Obsidian.perf.hudMs", 5L)
      * (1000000L);
   private static final long PERF_WARN_COOLDOWN_NANOS = Long.getLong(
         "Obsidian.perf.cooldownMs", 1000L
      )
      * (1000000L);
   public final KeybindsHudElement2 waterMark;
   public final ArrayListHudElement3 arrayListHud;
   public final KeybindsHudElement keyBinds;
   public final KeybindsHudElement3 helperBinds;
   public final ArrayListHudElement2 potions;
   public final KeyStrokesHudElement keyStrokes;
   public final ObsidianComponent_94 notifications;
   public final TargetHudElement targetHud;
   public final SessionHudElement session;
   public final ObsidianComponent_95 information;
   public final ArrayListHudElement staffList;
   public boolean targetHudMenuOpen;
   public float targetHudMenuX;
   public float targetHudMenuY;
   public DraggableHudElement hudContextElement;
   public DraggableHudElement pendingHudContextElement;
   public float pendingTargetHudMenuX;
   public float pendingTargetHudMenuY;
   private final AnimationUtil targetHudMenuAnimation = new AnimationUtil(
      0.0f, 12.5f, ObsidianComponent_57.CUBIC_OUT
   );
   private final AnimationUtil targetHudParticlesBgAnimation = new AnimationUtil(
      1.0f, 15.0f, ObsidianComponent_57.CUBIC_OUT
   );
   private final AnimationUtil targetHudParticlesCircleAnimation = new AnimationUtil(
      1.0f, 8.199999809265137f, ObsidianComponent_57.BACK_OUT
   );
   private final AnimationUtil targetHudBarSwitchAnimation = new AnimationUtil(0.0f, 7.0F, ObsidianComponent_57.CUBIC_OUT);
   private final AnimationUtil waterMarkFpsBgAnimation = new AnimationUtil(
      1.0f, 15.0f, ObsidianComponent_57.CUBIC_OUT
   );
   private final AnimationUtil waterMarkFpsCircleAnimation = new AnimationUtil(
      1.0f, 8.199999809265137f, ObsidianComponent_57.BACK_OUT
   );
   private final AnimationUtil waterMarkMsBgAnimation = new AnimationUtil(
      1.0f, 15.0f, ObsidianComponent_57.CUBIC_OUT
   );
   private final AnimationUtil waterMarkMsCircleAnimation = new AnimationUtil(
      1.0f, 8.199999809265137f, ObsidianComponent_57.BACK_OUT
   );
   private final AnimationUtil waterMarkServerBgAnimation = new AnimationUtil(
      1.0f, 15.0f, ObsidianComponent_57.CUBIC_OUT
   );
   private final AnimationUtil waterMarkServerCircleAnimation = new AnimationUtil(
      1.0f, 8.199999809265137f, ObsidianComponent_57.BACK_OUT
   );
   private final AnimationUtil waterMarkTpsBgAnimation = new AnimationUtil(
      1.0f, 15.0f, ObsidianComponent_57.CUBIC_OUT
   );
   private final AnimationUtil waterMarkTpsCircleAnimation = new AnimationUtil(
      1.0f, 8.199999809265137f, ObsidianComponent_57.BACK_OUT
   );
   private final AnimationUtil hudRectTypeSwitchAnimation = new AnimationUtil(
      1.0f, 7.0f, ObsidianComponent_57.CUBIC_OUT
   );
   public static final String HUD_HINT_TEXT = "ПКМ - по элементу для открытия настроек";
   public ModeSetting style;
   public final MultiBooleanSetting hudModules;

   public HudInterfaceModule() {
      super("Interface", "Интерфейс клиента", Category.RENDER);
      String[] var10005 = new String[2];
      var10005[0] = "Obs";
      var10005[1] = "Обычный";
      this.style = new ModeSetting("Стиль", "Obs", var10005);
      BooleanSetting[] var10004 = new BooleanSetting[11];
      var10004[0] = new BooleanSetting("Ватермарка", true);
      var10004[1] = new BooleanSetting("Аррай лист", true);
      var10004[2] = new BooleanSetting("Горячие клавиши", true);
      var10004[3] = new BooleanSetting("Серверные бинды", true);
      var10004[4] = new BooleanSetting("Зелья", true);
      var10004[5] = new BooleanSetting("Таргет худ", true);
      var10004[6] = new BooleanSetting("Уведомления", true);
      var10004[7] = new BooleanSetting("Стафф", true);
      var10004[8] = new BooleanSetting("Сессия", true)
         .visible(() -> this.style.is("Wave"));
      var10004[9] = new BooleanSetting("КейСтроки", true)
         .visible(() -> this.style.is("Wave"));
      var10004[10] = new BooleanSetting("Информация", true);
      this.hudModules = new MultiBooleanSetting("Элементы", var10004);
      Setting[] var10001 = new Setting[2];
      var10001[0] = this.hudModules;
      var10001[1] = this.style;
      this.addSettings(var10001);
      this.waterMark = new KeybindsHudElement2(
         Obsidian.draggable(
            this,
            "WaterMark",
            10.0f,
            10.0f
         )
      );
      this.arrayListHud = new ArrayListHudElement3(
         Obsidian.draggable(
            this, "ArrayList", 5.0f, 24.0f
         )
      );
      this.keyBinds = new KeybindsHudElement(
         Obsidian.draggable(
            this, "KeyBinds", 30.0f, 30.0f
         )
      );
      this.helperBinds = new KeybindsHudElement3(
         Obsidian.draggable(this, "HelperBinds", 90.0f, 30.0f)
      );
      this.potions = new ArrayListHudElement2(
         Obsidian.draggable(
            this, "Potions", 30.0f, 60.0f
         )
      );
      this.staffList = new ArrayListHudElement(
         Obsidian.draggable(
            this, "StaffList", 60.0f, 100.0f
         )
      );
      this.session = new SessionHudElement(
         Obsidian.draggable(
            this, "Session", 70.0f, 30.0f
         )
      );
      this.keyStrokes = new KeyStrokesHudElement(
         Obsidian.draggable(
            this,
            "KeyStrokes",
            150.0f,
            120.0f
         )
      );
      this.information = new ObsidianComponent_95(
         Obsidian.draggable(
            this, "Information", 50.0f, 100.0f
         )
      );
      this.notifications = new ObsidianComponent_94(
         Obsidian.draggable(this, "Notifications", 0.0f, 0.0f)
      );
      this.targetHud = new TargetHudElement(
         Obsidian.draggable(
            this, "TargetHud", 30.0f, 90.0f
         )
      );
   }
   public FontRenderer issue(int size) {
      return ObsidianComponent_201.getFont("suisse", size);
   }
   public int fadeColorSafe(int color, float progress, int minAlpha) {
      int faded = ObsidianComponent_olorUtil.applyAlpha(color, progress);
      int a = ObsidianComponent_olorUtil.getAlpha(faded);
      return a == 0 && progress > 0.0010000000474974513f ? ObsidianComponent_olorUtil.setAlphaColor(faded, minAlpha) : faded;
   }
   public int fadeTextAlphaSafe(float progress, int maxAlpha, int minAlpha) {
      int alpha = MathHelper.clamp((int)((float)maxAlpha * progress), 0, maxAlpha);
      return alpha == 0 && progress > 0.0010000000474974513f ? minAlpha : alpha;
   }
   public int getThemeColor() {
      return !Obsidian.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")
         ? Obsidian.INSTANCE.themeStorage.getThemes().getTheme().color[0]
         : ObsidianComponent_olorUtil.getThemeColor();
   }
   public boolean isHudElementHovered(DraggableHudElement element, double mouseX, double mouseY) {
      float width = element.draggable.getWidth();
      float height = element.draggable.getHeight();
      return (!(width <= 1.0f)
            && !(height <= 1.0f)
         ? MathUtil.isHovered(mouseX, mouseY, (double)element.draggable.getX(), (double)element.draggable.getY(), (double)width, (double)height)
         : false);
   }
   public boolean isHudElementEnabled(DraggableHudElement element) {
      return (element != null
            && element.draggable.getWidth() > 1.0f
            && element.draggable.getHeight() > 1.0f);
   }
   public DraggableHudElement getHoveredHudElement(double mouseX, double mouseY) {
      if (this.isHudElementEnabled(this.targetHud) && this.isHudElementHovered(this.targetHud, mouseX, mouseY)) {
         return this.targetHud;
      } else if (this.isHudElementEnabled(this.waterMark) && this.isHudElementHovered(this.waterMark, mouseX, mouseY)) {
         return this.waterMark;
      } else if (this.isHudElementEnabled(this.arrayListHud) && this.isHudElementHovered(this.arrayListHud, mouseX, mouseY)) {
         return this.arrayListHud;
      } else if (this.isHudElementEnabled(this.keyBinds) && this.isHudElementHovered(this.keyBinds, mouseX, mouseY)) {
         return this.keyBinds;
      } else if (this.isHudElementEnabled(this.helperBinds) && this.isHudElementHovered(this.helperBinds, mouseX, mouseY)) {
         return this.helperBinds;
      } else if (this.isHudElementEnabled(this.potions) && this.isHudElementHovered(this.potions, mouseX, mouseY)) {
         return this.potions;
      } else if (this.isHudElementEnabled(this.keyStrokes) && this.isHudElementHovered(this.keyStrokes, mouseX, mouseY)) {
         return this.keyStrokes;
      } else if (this.isHudElementEnabled(this.information) && this.isHudElementHovered(this.information, mouseX, mouseY)) {
         return this.information;
      } else if (this.isHudElementEnabled(this.staffList) && this.isHudElementHovered(this.staffList, mouseX, mouseY)) {
         return this.staffList;
      } else if (this.isHudElementEnabled(this.session) && this.isHudElementHovered(this.session, mouseX, mouseY)) {
         return this.session;
      } else {
         return this.isHudElementEnabled(this.notifications) && this.isHudElementHovered(this.notifications, mouseX, mouseY) ? this.notifications : null;
      }
   }
   public float getTargetHudMenuWidth() {
      return 100.0f;
   }
   public float getMenuHeightForElement(DraggableHudElement element) {
      if (element == this.targetHud) {
         return 59.0f;
      } else {
         return element == this.waterMark
            ? 69.0f
            : 29.0f;
      }
   }
   public float getTargetHudMenuHeight() {
      return this.getMenuHeightForElement(this.hudContextElement);
   }
   public void clampTargetHudMenuToWindow(float menuWidth, float menuHeight) {
      if (mc != null && mc.getWindow() != null) {
         float maxX = Math.max(
            2.0f,
            (float)mc.getWindow().getScaledWidth() - menuWidth - 2.0f
         );
         float maxY = Math.max(
            2.0f,
            (float)mc.getWindow().getScaledHeight() - menuHeight - 2.0f
         );
         this.targetHudMenuX = MathHelper.clamp(this.targetHudMenuX, 2.0f, maxX);
         this.targetHudMenuY = MathHelper.clamp(this.targetHudMenuY, 2.0f, maxY);
      }
   }

   public boolean handleHudContextClick(double mouseX, double mouseY, int button) {
      DraggableHudElement hoveredElement = this.getHoveredHudElement(mouseX, mouseY);
      if (button == 1 && hoveredElement != null) {
         if (this.targetHudMenuOpen && this.hudContextElement == hoveredElement) {
            this.targetHudMenuOpen = false;
            this.pendingHudContextElement = null;
         } else if (this.targetHudMenuOpen && this.hudContextElement != null && this.hudContextElement != hoveredElement) {
            this.pendingHudContextElement = hoveredElement;
            float menuWidth = this.getTargetHudMenuWidth();
            float menuHeight = this.getMenuHeightForElement(hoveredElement);
            this.pendingTargetHudMenuX = hoveredElement.draggable.getX()
               + hoveredElement.draggable.getWidth()
               + 4.0f;
            this.pendingTargetHudMenuY = hoveredElement.draggable.getY() + 1.5f;
            float saveX = this.targetHudMenuX;
            float saveY = this.targetHudMenuY;
            this.targetHudMenuX = this.pendingTargetHudMenuX;
            this.targetHudMenuY = this.pendingTargetHudMenuY;
            this.clampTargetHudMenuToWindow(menuWidth, menuHeight);
            this.pendingTargetHudMenuX = this.targetHudMenuX;
            this.pendingTargetHudMenuY = this.targetHudMenuY;
            this.targetHudMenuX = saveX;
            this.targetHudMenuY = saveY;
            this.targetHudMenuOpen = false;
         } else {
            this.hudContextElement = hoveredElement;
            this.pendingHudContextElement = null;
            this.targetHudMenuOpen = true;
            float menuWidth = this.getTargetHudMenuWidth();
            float menuHeight = this.getTargetHudMenuHeight();
            this.targetHudMenuX = hoveredElement.draggable.getX()
               + hoveredElement.draggable.getWidth()
               + 4.0f;
            this.targetHudMenuY = hoveredElement.draggable.getY() + 1.5F;
            this.clampTargetHudMenuToWindow(menuWidth, menuHeight);
         }

         return true;
      } else if (this.targetHudMenuOpen && this.hudContextElement != null) {
         float menuWidth = this.getTargetHudMenuWidth();
         float menuHeight = this.getTargetHudMenuHeight();
         this.clampTargetHudMenuToWindow(menuWidth, menuHeight);
         float buttonGap = 3.0f;
         float buttonX = this.targetHudMenuX + 5.0f;
         float buttonW = (menuWidth - 10.0f - buttonGap)
            / 2.0f;
         float buttonH = 10.0f;
         float unusualButtonX = buttonX + buttonW + buttonGap;
         float rectButtonY = this.hudContextElement == this.targetHud
            ? this.targetHudMenuY + 46.0f
            : (
               this.hudContextElement == this.waterMark
                  ? this.targetHudMenuY + 56.0f
                  : this.targetHudMenuY + 14.0f
            );
         boolean menuHovered = MathUtil.isHovered(
            mouseX, mouseY, (double)this.targetHudMenuX, (double)this.targetHudMenuY, (double)menuWidth, (double)menuHeight
         );
         boolean rectNormalHovered = MathUtil.isHovered(mouseX, mouseY, (double)buttonX, (double)rectButtonY, (double)buttonW, (double)buttonH);
         boolean rectUnusualHovered = MathUtil.isHovered(mouseX, mouseY, (double)unusualButtonX, (double)rectButtonY, (double)buttonW, (double)buttonH);
         if (button == 0 && !menuHovered && hoveredElement == this.hudContextElement) {
            this.targetHudMenuOpen = false;
            this.pendingHudContextElement = null;
            return false;
         } else if (button == 0 && rectNormalHovered) {
            this.hudContextElement.setUnusualRectType(false);
            return true;
         } else if (button == 0 && rectUnusualHovered) {
            this.hudContextElement.setUnusualRectType(true);
            return true;
         } else {
            if (this.hudContextElement == this.targetHud) {
               float buttonY = this.targetHudMenuY + 25.0f;
               float particlesToggleX = this.targetHudMenuX + menuWidth - 21.0f;
               float particlesToggleY = this.targetHudMenuY + 4.0f;
               boolean normalHovered = MathUtil.isHovered(mouseX, mouseY, (double)buttonX, (double)buttonY, (double)buttonW, (double)buttonH);
               boolean unusualHovered = MathUtil.isHovered(mouseX, mouseY, (double)unusualButtonX, (double)buttonY, (double)buttonW, (double)buttonH);
               boolean particlesHovered = MathUtil.isHovered(
                  mouseX,
                  mouseY,
                  (double)particlesToggleX,
                  (double)particlesToggleY,
                  16.0,
                  9.0
               );
               if (button == 0 && normalHovered) {
                  this.targetHud.setHealthBarStyleEnabled(false);
                  return true;
               }

               if (button == 0 && unusualHovered) {
                  this.targetHud.setHealthBarStyleEnabled(true);
                  return true;
               }

               if (button == 0 && particlesHovered) {
                  this.targetHud
                     .setHeadParticlesEnabled(
                        (!this.targetHud.isHeadParticlesEnabled())
                     );
                  return true;
               }
            } else if (this.hudContextElement == this.waterMark) {
               float baseY = this.targetHudMenuY + 4.5f;
               float toggleX = this.targetHudMenuX + menuWidth - 21.0f;
               boolean fpsHovered = MathUtil.isHovered(
                  mouseX,
                  mouseY,
                  (double)toggleX,
                  (double)baseY,
                  16.0,
                  9.0
               );
               boolean msHovered = MathUtil.isHovered(
                  mouseX,
                  mouseY,
                  (double)toggleX,
                  (double)(baseY + 10.0f),
                  16.0,
                  9.0
               );
               boolean serverHovered = MathUtil.isHovered(
                  mouseX,
                  mouseY,
                  (double)toggleX,
                  (double)(baseY + 20.0f),
                  16.0,
                  9.0
               );
               boolean tpsHovered = MathUtil.isHovered(
                  mouseX,
                  mouseY,
                  (double)toggleX,
                  (double)(baseY + 30.0f),
                  16.0,
                  9.0
               );
               if (button == 0 && fpsHovered) {
                  this.waterMark
                     .setShowFps((!this.waterMark.isShowFps()));
                  return true;
               }

               if (button == 0 && msHovered) {
                  this.waterMark.setShowMs((!this.waterMark.isShowMs()));
                  return true;
               }

               if (button == 0 && serverHovered) {
                  this.waterMark
                     .setShowServer((!this.waterMark.isShowServer()));
                  return true;
               }

               if (button == 0 && tpsHovered) {
                  this.waterMark.setShowTps((!this.waterMark.isShowTps()));
                  return true;
               }
            }

            if (button == 0 || button == 1) {
               if (menuHovered) {
                  return true;
               }

               if (hoveredElement != this.hudContextElement) {
                  this.targetHudMenuOpen = false;
                  this.pendingHudContextElement = null;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   public void renderHudContextMenu(DrawContext context, int mouseX, int mouseY) {
      if (this.hudContextElement != null && !this.isHudElementEnabled(this.hudContextElement)) {
         this.targetHudMenuOpen = false;
         this.hudContextElement = null;
         this.pendingHudContextElement = null;
      }

      this.targetHudMenuAnimation
         .update(this.targetHudMenuOpen ? 1.0f : 0.0f);
      float targetMenuProgress = MathHelper.clamp(
         this.targetHudMenuAnimation.getValue(), 0.0f, 1.0f
      );
      if (!this.targetHudMenuOpen && targetMenuProgress <= 0.009999999776482582f) {
         if (this.pendingHudContextElement != null) {
            this.hudContextElement = this.pendingHudContextElement;
            this.pendingHudContextElement = null;
            this.targetHudMenuX = this.pendingTargetHudMenuX;
            this.targetHudMenuY = this.pendingTargetHudMenuY;
            this.targetHudMenuOpen = true;
         } else {
            this.hudContextElement = null;
         }
      }

      if (!this.targetHudMenuOpen && targetMenuProgress <= 0.009999999776482582f && this.hudContextElement == null) {
         this.hudContextElement = null;
      } else if (this.hudContextElement != null) {
         boolean targetContext = (this.hudContextElement == this.targetHud);
         boolean waterMarkContext = (this.hudContextElement == this.waterMark);
         boolean notificationsContext = (this.hudContextElement == this.notifications);
         float menuWidth = this.getTargetHudMenuWidth();
         float menuHeight = this.getTargetHudMenuHeight();
         this.clampTargetHudMenuToWindow(menuWidth, menuHeight);
         float x = this.targetHudMenuX;
         float y = this.targetHudMenuY;
         int themeColor = this.getThemeColor();
         float contentProgress = MathHelper.clamp(
            (targetMenuProgress - 0.05999999865889549f) / 0.9399999976158142f,
            0.0f,
            1.0f
         );
         int textAlpha = this.fadeTextAlphaSafe(contentProgress, 255, 2);
         MatrixStack matrices = context.getMatrices();
         matrices.push();
         RenderUtil.drawDefaultHudPanel(
            matrices,
            x,
            y,
            menuWidth,
            menuHeight,
            3.0f,
            3.5f,
            ObsidianComponent_olorUtil.applyAlpha(
               ObsidianComponent_olorUtil.rgba(
                  50,
                  50,
                  50,
                  255
               ),
               targetMenuProgress
            ),
            ObsidianComponent_olorUtil.applyAlpha(ObsidianComponent_olorUtil.darken(themeColor, 0.15000000596046448f), targetMenuProgress),
            ObsidianComponent_olorUtil.applyAlpha(ObsidianComponent_olorUtil.darken(themeColor, 0.05000000074505806f), targetMenuProgress)
         );
         if (contentProgress <= 0.019999999552965164f) {
            matrices.pop();
         } else {
            float buttonGap = 3.0f;
            float buttonX = x + 5.0f;
            float buttonW = (menuWidth - 10.0f - buttonGap)
               / 2.0f;
            float buttonH = 10.0f;
            float unusualX = buttonX + buttonW + buttonGap;
            int inactiveColor = ObsidianComponent_olorUtil.applyAlpha(
               ObsidianComponent_olorUtil.rgba(
                  70,
                  70,
                  70,
                  255
               ),
               contentProgress
            );
            int activeLeftColor = this.fadeColorSafe(
               ObsidianComponent_olorUtil.darken(themeColor, 0.4000000059604645f),
               contentProgress,
               2
            );
            int activeRightColor = this.fadeColorSafe(themeColor, contentProgress, 2);
            if (targetContext) {
               this.issue(12)
                  .drawStringWithFade(
                     matrices,
                     "Партиклы с головы",
                     x + 4.699999809265137f,
                     y + 7.5f,
                     menuWidth - 28.0f,
                     ObsidianComponent_olorUtil.rgba(
                        255, 255, 255, textAlpha
                     )
                  );
               this.targetHudParticlesBgAnimation
                  .update(
                     this.targetHud.isHeadParticlesEnabled()
                        ? 1.0f
                        : 0.0f
                  );
               this.targetHudParticlesCircleAnimation
                  .update(
                     this.targetHud.isHeadParticlesEnabled()
                        ? 1.0f
                        : 0.0f
                  );
               float bgProgress = this.targetHudParticlesBgAnimation.getValue();
               float circleProgress = this.targetHudParticlesCircleAnimation.getValue();
               int particlesOffColor = ObsidianComponent_olorUtil.darken(themeColor, 0.05000000074505806f);
               int particlesColor = ObsidianComponent_olorUtil.interpolateColor(particlesOffColor, themeColor, bgProgress);
               float particlesToggleX = x + menuWidth - 21.0f;
               float particlesToggleY = y + 4.5f;
               RenderUtil.drawGradientRect(
                  matrices,
                  particlesToggleX,
                  particlesToggleY,
                  16.0f,
                  9.0f,
                  3.0f,
                  this.fadeColorSafe(particlesColor, contentProgress, 2),
                  this.fadeColorSafe(
                     ObsidianComponent_olorUtil.darken(particlesColor, 0.6499999761581421f),
                     contentProgress,
                     2
                  )
               );
               float particlesCircleX = particlesToggleX
                  + 4.5f
                  + circleProgress * 6.199999809265137f;
               RenderUtil.drawRoundCircle(
                  matrices,
                  particlesCircleX + 0.5f,
                  particlesToggleY + 4.5f,
                  6.849999904632568f,
                  ObsidianComponent_olorUtil.rgba(255, 255, 255, textAlpha)
               );
               this.issue(12)
                  .draw(
                     matrices,
                     "Вид полоски",
                     x + 4.7F,
                     y + 18.0F,
                     ObsidianComponent_olorUtil.rgba(
                        255,
                        255,
                        255,
                        this.fadeTextAlphaSafe(contentProgress, 225, 2)
                     )
                  );
               float buttonY = y + 25.0f;
               boolean healthBarStyle = this.targetHud.isHealthBarStyleEnabled();
               this.targetHudBarSwitchAnimation
                  .update(
                     healthBarStyle
                        ? 1.0f
                        : 0.0f
                  );
               float typeSwitchProgress = MathHelper.clamp(
                  this.targetHudBarSwitchAnimation.getValue(),
                  0.0f,
                  1.0f
               );
               RenderUtil.drawRoundedRect(
                  matrices, buttonX, buttonY, buttonW, buttonH, 1.5f, inactiveColor
               );
               RenderUtil.drawRoundedRect(
                  matrices, unusualX, buttonY, buttonW, buttonH, 1.5f, inactiveColor
               );
               float activeX = MathHelper.lerp(typeSwitchProgress, buttonX, unusualX);
               RenderUtil.drawGradientRect(
                  matrices, activeX, buttonY, buttonW, buttonH, 1.5F, activeLeftColor, activeRightColor, true
               );
               String normalText = "Клиентский";
               String unusualText = "Здоровье";
               float normalTextX = buttonX
                  + (buttonW - this.issue(12).getWidth(normalText))
                     * 0.5f;
               float unusualTextX = unusualX
                  + (buttonW - this.issue(12).getWidth(unusualText)) * 0.550000011920929f;
               int normalTextAlpha = MathHelper.clamp(
                  (int)(
                     (float)textAlpha
                        * (
                           0.6499999761581421f
                              + 0.3499999940395355f
                                 * (1.0f - typeSwitchProgress)
                        )
                  ),
                  0,
                  255
               );
               int unusualTextAlpha = MathHelper.clamp(
                  (int)(
                     (float)textAlpha
                        * (
                           0.6499999761581421f
                              + 0.3499999940395355f * typeSwitchProgress
                        )
                  ),
                  0,
                  255
               );
               this.issue(12)
                  .draw(
                     matrices,
                     normalText,
                     normalTextX,
                     buttonY + 3.799999952316284f,
                     ObsidianComponent_olorUtil.rgba(
                        255,
                        255,
                        255,
                        normalTextAlpha
                     )
                  );
               this.issue(12)
                  .draw(
                     matrices,
                     unusualText,
                     unusualTextX,
                     buttonY + 3.799999952316284f,
                     ObsidianComponent_olorUtil.rgba(255, 255, 255, unusualTextAlpha)
                  );
            } else if (waterMarkContext) {
               float wmToggleX = x + menuWidth - 21.0f;
               float wmBaseY = y + 3.5f;
               float wmLabelX = x + 5.0f;
               this.drawWaterMarkToggle(
                  matrices,
                  "Отображать фпс",
                  wmLabelX,
                  wmToggleX,
                  wmBaseY,
                  this.waterMark.isShowFps(),
                  this.waterMarkFpsBgAnimation,
                  this.waterMarkFpsCircleAnimation,
                  themeColor,
                  contentProgress,
                  textAlpha
               );
               this.drawWaterMarkToggle(
                  matrices,
                  "Отображать пинг",
                  wmLabelX,
                  wmToggleX,
                  wmBaseY + 10.0f,
                  this.waterMark.isShowMs(),
                  this.waterMarkMsBgAnimation,
                  this.waterMarkMsCircleAnimation,
                  themeColor,
                  contentProgress,
                  textAlpha
               );
               this.drawWaterMarkToggle(
                  matrices,
                  "Отображать сервер",
                  wmLabelX,
                  wmToggleX,
                  wmBaseY + 20.0F,
                  this.waterMark.isShowServer(),
                  this.waterMarkServerBgAnimation,
                  this.waterMarkServerCircleAnimation,
                  themeColor,
                  contentProgress,
                  textAlpha
               );
               this.drawWaterMarkToggle(
                  matrices,
                  "Отображать тпс",
                  wmLabelX,
                  wmToggleX,
                  wmBaseY + 30.0f,
                  this.waterMark.isShowTps(),
                  this.waterMarkTpsBgAnimation,
                  this.waterMarkTpsCircleAnimation,
                  themeColor,
                  contentProgress,
                  textAlpha
               );
            } else if (notificationsContext) {
               float ntToggleX = x + menuWidth - 21.0f;
               float ntBaseY = y + 3.5f;
               float var49 = x + 5.0f;
            }

            this.issue(12)
               .draw(
                  matrices,
                  "Тип ректа",
                  x + 4.7F,
                  targetContext
                     ? y + 39.5f
                     : (
                        waterMarkContext
                           ? y + 49.5f
                           : y + 7.5f
                     ),
                  ObsidianComponent_olorUtil.rgba(
                     255,
                     255,
                     255,
                     this.fadeTextAlphaSafe(contentProgress, 225, 2)
                  )
               );
            float rectButtonY = targetContext
               ? y + 46.0f
               : (
                  waterMarkContext
                     ? y + 56.0f
                     : y + 14.0f
               );
            boolean unusualRect = this.hudContextElement.isUnusualRectType();
            this.hudRectTypeSwitchAnimation
               .update(
                  unusualRect
                     ? 1.0f
                     : 0.0f
               );
            float rectSwitchProgress = MathHelper.clamp(
               this.hudRectTypeSwitchAnimation.getValue(),
               0.0f,
               1.0f
            );
            RenderUtil.drawRoundedRect(matrices, buttonX, rectButtonY, buttonW, buttonH, 1.5f, inactiveColor);
            RenderUtil.drawRoundedRect(matrices, unusualX, rectButtonY, buttonW, buttonH, 1.5f, inactiveColor);
            float rectActiveX = MathHelper.lerp(rectSwitchProgress, buttonX, unusualX);
            RenderUtil.drawGradientRect(
               matrices,
               rectActiveX,
               rectButtonY,
               buttonW,
               buttonH,
               1.5f,
               activeLeftColor,
               activeRightColor,
               true
            );
            String normalRectText = "Обычный";
            String unusualRectText = "Необычный";
            float normalRectTextX = buttonX
               + (buttonW - this.issue(12).getWidth(normalRectText))
                  * 0.5f;
            float unusualRectTextX = unusualX
               + (buttonW - this.issue(12).getWidth(unusualRectText)) * 0.5f;
            int normalRectAlpha = MathHelper.clamp(
               (int)(
                  (float)textAlpha
                     * (
                        0.6499999761581421f
                           + 0.3499999940395355f
                              * (1.0f - rectSwitchProgress)
                     )
               ),
               0,
               255
            );
            int unusualRectAlpha = MathHelper.clamp(
               (int)(
                  (float)textAlpha
                     * (0.6499999761581421f + 0.3499999940395355f * rectSwitchProgress)
               ),
               0,
               255
            );
            this.issue(12)
               .draw(
                  matrices,
                  normalRectText,
                  normalRectTextX,
                  rectButtonY + 3.799999952316284f,
                  ObsidianComponent_olorUtil.rgba(255, 255, 255, normalRectAlpha)
               );
            this.issue(12)
               .draw(
                  matrices,
                  unusualRectText,
                  unusualRectTextX,
                  rectButtonY + 3.799999952316284f,
                  ObsidianComponent_olorUtil.rgba(255, 255, 255, unusualRectAlpha)
               );
            matrices.pop();
         }
      }
   }
   public void renderHudElement(DraggableHudElement element, EventRender2D event) {
      long start = PERF_DEBUG ? System.nanoTime() : 0L;
      element.draggable.beginRenderTilt(event.getContext().getMatrices());

      try {
         element.onRender(event);
      } finally {
         element.draggable.endRenderTilt(event.getContext().getMatrices());
         if (PERF_DEBUG) {
            long elapsed = System.nanoTime() - start;
            if (elapsed >= SLOW_HUD_ELEMENT_NANOS) {
               this.logSlowHudElement(element, elapsed);
            }
         }
      }
   }
   public void logSlowHudElement(DraggableHudElement element, long elapsedNanos) {
      String name = element.getClass().getSimpleName();
      long now = System.nanoTime();
      Long lastWarn = PERF_WARNINGS.get(name);
      if (lastWarn == null || now - lastWarn >= PERF_WARN_COOLDOWN_NANOS) {
         PERF_WARNINGS.put(name, now);
         PrintStream var10000 = System.out;
         Locale var10001 = Locale.ROOT;
         Object[] var10003 = new Object[2];
         var10003[0] = name;
         var10003[1] = (double)elapsedNanos
            / 1000000.0;
         var10000.println(String.format(var10001, "[PerfDebug] Slow HUD element: Interface -> %s took %.2f ms", var10003));
      }
   }
   public void drawWaterMarkToggle(
      MatrixStack matrices,
      String label,
      float labelX,
      float toggleX,
      float toggleY,
      boolean enabled,
      AnimationUtil bgAnimation,
      AnimationUtil circleAnimation,
      int themeColor,
      float contentProgress,
      int textAlpha
   ) {
      this.issue(12)
         .draw(
            matrices,
            label,
            labelX,
            toggleY + 3.0f,
            ObsidianComponent_olorUtil.rgba(255, 255, 255, textAlpha)
         );
      bgAnimation.update(enabled ? 1.0f : 0.0f);
      circleAnimation.update(enabled ? 1.0f : 0.0f);
      float bgProgress = bgAnimation.getValue();
      float circleProgress = circleAnimation.getValue();
      int offColor = ObsidianComponent_olorUtil.darken(themeColor, 0.05000000074505806f);
      int toggleColor = ObsidianComponent_olorUtil.interpolateColor(offColor, themeColor, bgProgress);
      RenderUtil.drawGradientRect(
         matrices,
         toggleX,
         toggleY,
         16.0f,
         9.0f,
         3.0f,
         this.fadeColorSafe(toggleColor, contentProgress, 2),
         this.fadeColorSafe(
            ObsidianComponent_olorUtil.darken(toggleColor, 0.6499999761581421f), contentProgress, 2
         )
      );
      float circleX = toggleX
         + 4.5f
         + circleProgress * 6.199999809265137f;
      RenderUtil.drawRoundCircle(
         matrices,
         circleX + 0.5f,
         toggleY + 4.5f,
         6.849999904632568f,
         ObsidianComponent_olorUtil.rgba(255, 255, 255, textAlpha)
      );
   }

   public Map<String, DraggableHudElement> getConfigurableHudElements() {
      Map<String, DraggableHudElement> elements = new LinkedHashMap<>();
      elements.put("waterMark", this.waterMark);
      elements.put("arrayList", this.arrayListHud);
      elements.put("keyBinds", this.keyBinds);
      elements.put("helperBinds", this.helperBinds);
      elements.put("potions", this.potions);
      elements.put("keyStrokes", this.keyStrokes);
      elements.put("notifications", this.notifications);
      elements.put("targetHud", this.targetHud);
      elements.put("session", this.session);
      elements.put("information", this.information);
      elements.put("staffList", this.staffList);
      return elements;
   }

         @EventTarget(
      priority = -200
   )
   public void onEvent(EventRender2D event) {
      boolean waveStyle = this.style.is("Obs");
      boolean showWaterMark = this.hudModules.is("Ватермарка");
      boolean showArrayList = this.hudModules.is("Аррай лист");
      boolean showKeyBinds = this.hudModules.is("Горячие клавиши");
      boolean showHelperBinds = this.hudModules.is("Серверные бинды");
      boolean showPotions = this.hudModules.is("Зелья");
      boolean showKeyStrokes = this.hudModules.is("КейСтроки");
      boolean showInformation = this.hudModules.is("Информация");
      boolean showStaff = this.hudModules.is("Стафф");
      boolean showSession = this.hudModules.is("Сессия");
      boolean showNotifications = this.hudModules.is("Уведомления");
      boolean showTargetHud = this.hudModules.is("Таргет худ");
      if (mc != null && mc.getWindow() != null && mc.currentScreen instanceof ChatScreen) {
         FontRenderer hintFont = this.issue(18);
         float x = (float)mc.getWindow().getScaledWidth() * 0.5f
            - hintFont.getWidth("ПКМ - по элементу для открытия настроек") * 0.5f;
         hintFont.draw(
            event.getContext().getMatrices(),
            "ПКМ - по элементу для открытия настроек",
            x,
            40.0f,
            -1
         );
      }

      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      boolean var18 = false ;

      try {
         var18 = true;
         if (showWaterMark) {
            this.renderHudElement(this.waterMark, event);
         }

         if (showArrayList && waveStyle) {
            this.renderHudElement(this.arrayListHud, event);
         }

         if (showKeyBinds) {
            this.renderHudElement(this.keyBinds, event);
         }

         if (showHelperBinds) {
            this.renderHudElement(this.helperBinds, event);
         }

         if (showPotions) {
            this.renderHudElement(this.potions, event);
         }

         if (showKeyStrokes && waveStyle) {
            this.renderHudElement(this.keyStrokes, event);
         }

         if (showInformation) {
            this.renderHudElement(this.information, event);
         }

         if (showStaff) {
            this.renderHudElement(this.staffList, event);
         }

         if (showSession && waveStyle) {
            this.renderHudElement(this.session, event);
         }

         if (showNotifications) {
            this.renderHudElement(this.notifications, event);
         }

         if (showTargetHud) {
            this.renderHudElement(this.targetHud, event);
            var18 = false;
         } else {
            var18 = false;
         }
      } finally {
         if (var18) {
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
         }
      }

      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      if (!(mc.currentScreen instanceof ChatScreen)) {
         this.targetHudMenuOpen = false;
         this.pendingHudContextElement = null;
      }
   }
}
