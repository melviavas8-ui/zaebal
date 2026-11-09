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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class ShulkerPreviewModule extends Module {
   public static ShulkerPreviewModule INSTANCE = new ShulkerPreviewModule();
   public static final float RECT_RADIUS = 5.0F;
   public static final int SLOT_SIZE = 18;
   public static final int PADDING = 7;
   public static final int ROWS = 3;
   public static final int COLS = 9;
   public static final int TITLE_HEIGHT = 14;
   public static final int SLOT_BG_COLOR = -7631989;
   public Field guiLeftField;
   public Field guiTopField;
   public static ShulkerPreviewModule instance;

   public ShulkerPreviewModule() {
      super("ShulkerPreview", "Показывает содержимое шалкера при наведении + CTRL", Category.RENDER);
      this.initReflection();
      instance = this;
   }

   public static ShulkerPreviewModule getInstance() {
      return instance;
   }
   public void initReflection() {
      try {
         Field[] var1 = HandledScreen.class.getDeclaredFields();
         int var2 = var1.length;

         for (int var3 = 0; var3 < var2; var3++) {
            Field field = var1[var3];
            if (field.getType() == int.class) {
               field.setAccessible(true);
               String name = field.getName();
               if (name.equals("x") || name.equals("field_2776") || name.contains("Left") || name.contains("guiLeft")) {
                  this.guiLeftField = field;
               } else if (name.equals("y") || name.equals("field_2800") || name.contains("Top") || name.contains("guiTop")) {
                  this.guiTopField = field;
               }
            }
         }
      } catch (Exception var6) {
      }
   }
   public int getGuiLeft(HandledScreen<?> screen) {
      try {
         if (this.guiLeftField != null) {
            return this.guiLeftField.getInt(screen);
         }
      } catch (Exception var3) {
      }

      return (mc.getWindow().getScaledWidth() - 176) / (2);
   }
   public int getGuiTop(HandledScreen<?> screen) {
      try {
         if (this.guiTopField != null) {
            return this.guiTopField.getInt(screen);
         }
      } catch (Exception var3) {
      }

      return (mc.getWindow().getScaledHeight() - (166)) / 2;
   }

   public void renderFromMixin(DrawContext context, int mouseX, int mouseY) {
      if (this.isEnable()) {
         if (mc != null && mc.player != null && mc.currentScreen != null) {
            if (mc.currentScreen instanceof HandledScreen<?> handledScreen) {
               long handle = mc.getWindow().getHandle();
               boolean isCtrlPressed = (GLFW.glfwGetKey(handle, 341) == 1);
               if (isCtrlPressed) {
                  Slot hoveredSlot = this.getHoveredSlot(handledScreen);
                  if (hoveredSlot != null) {
                     ItemStack stack = hoveredSlot.getStack();
                     if (this.isShulkerBox(stack)) {
                        ContainerComponent container = (ContainerComponent)stack.get(DataComponentTypes.CONTAINER);
                        if (container != null) {
                           this.renderShulkerPreview(context, stack, container, (float)mouseX, (float)mouseY);
                        }
                     }
                  }
               }
            }
         }
      }
   }
   public Slot getHoveredSlot(HandledScreen<?> screen) {
      try {
         ScreenHandler handler = screen.getScreenHandler();
         if (handler == null || handler.slots == null) {
            return null;
         }

         double mouseX = mc.mouse.getX() * (double)mc.getWindow().getScaledWidth() / (double)mc.getWindow().getWidth();
         double mouseY = mc.mouse.getY() * (double)mc.getWindow().getScaledHeight() / (double)mc.getWindow().getHeight();
         int guiLeft = this.getGuiLeft(screen);
         int guiTop = this.getGuiTop(screen);

         for (Slot slot : handler.slots) {
            int slotX = guiLeft + slot.x;
            int slotY = guiTop + slot.y;
            if (mouseX >= (double)slotX
               && mouseX < (double)(slotX + (16))
               && mouseY >= (double)slotY
               && mouseY < (double)(slotY + 16)) {
               return slot;
            }
         }
      } catch (Exception var13) {
      }

      return null;
   }
   public boolean isShulkerBox(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         return (!(stack.getItem() != Items.SHULKER_BOX
               && stack.getItem() != Items.WHITE_SHULKER_BOX
               && stack.getItem() != Items.ORANGE_SHULKER_BOX
               && stack.getItem() != Items.MAGENTA_SHULKER_BOX
               && stack.getItem() != Items.LIGHT_BLUE_SHULKER_BOX
               && stack.getItem() != Items.YELLOW_SHULKER_BOX
               && stack.getItem() != Items.LIME_SHULKER_BOX
               && stack.getItem() != Items.PINK_SHULKER_BOX
               && stack.getItem() != Items.GRAY_SHULKER_BOX
               && stack.getItem() != Items.LIGHT_GRAY_SHULKER_BOX
               && stack.getItem() != Items.CYAN_SHULKER_BOX
               && stack.getItem() != Items.PURPLE_SHULKER_BOX
               && stack.getItem() != Items.BLUE_SHULKER_BOX
               && stack.getItem() != Items.BROWN_SHULKER_BOX
               && stack.getItem() != Items.GREEN_SHULKER_BOX
               && stack.getItem() != Items.RED_SHULKER_BOX
               && stack.getItem() != Items.BLACK_SHULKER_BOX));
      } else {
         return false;
      }
   }
   public int getShulkerColor(ItemStack stack) {
      if (stack.getItem() == Items.SHULKER_BOX) {
         return -6394435;
      } else if (stack.getItem() == Items.WHITE_SHULKER_BOX) {
         return -1;
      } else if (stack.getItem() == Items.ORANGE_SHULKER_BOX) {
         return -425955;
      } else if (stack.getItem() == Items.MAGENTA_SHULKER_BOX) {
         return -3715395;
      } else if (stack.getItem() == Items.LIGHT_BLUE_SHULKER_BOX) {
         return -12930086;
      } else if (stack.getItem() == Items.YELLOW_SHULKER_BOX) {
         return -75715;
      } else if (stack.getItem() == Items.LIME_SHULKER_BOX) {
         return -8337633;
      } else if (stack.getItem() == Items.PINK_SHULKER_BOX) {
         return -816214;
      } else if (stack.getItem() == Items.GRAY_SHULKER_BOX) {
         return -12103854;
      } else if (stack.getItem() == Items.LIGHT_GRAY_SHULKER_BOX) {
         return -6447721;
      } else if (stack.getItem() == Items.CYAN_SHULKER_BOX) {
         return -15295332;
      } else if (stack.getItem() == Items.PURPLE_SHULKER_BOX) {
         return -7785800;
      } else if (stack.getItem() == Items.BLUE_SHULKER_BOX) {
         return -12827478;
      } else if (stack.getItem() == Items.BROWN_SHULKER_BOX) {
         return -8170446;
      } else if (stack.getItem() == Items.GREEN_SHULKER_BOX) {
         return -10585066;
      } else if (stack.getItem() == Items.RED_SHULKER_BOX) {
         return -5231066;
      } else {
         return stack.getItem() == Items.BLACK_SHULKER_BOX ? -14869215 : -6394435;
      }
   }
   public void renderShulkerPreview(DrawContext context, ItemStack shulkerItem, ContainerComponent container, float mouseX, float mouseY) {
      MatrixStack matrices = context.getMatrices();
      int screenWidth = context.getScaledWindowWidth();
      int screenHeight = context.getScaledWindowHeight();
      float contentWidth = 162.0f;
      float contentHeight = 54.0f;
      float totalWidth = contentWidth + 14.0f;
      float totalHeight = contentHeight
         + 14.0f
         + 14.0f;
      float x = mouseX + 12.0f;
      float y = mouseY - 12.0f;
      if (x + totalWidth > (float)screenWidth) {
         x = mouseX - totalWidth - 4.0f;
      }

      if (y + totalHeight > (float)screenHeight) {
         y = (float)screenHeight - totalHeight - 4.0f;
      }

      if (y < 4.0f) {
         y = 4.0F;
      }

      if (x < 4.0f) {
         x = 4.0F;
      }

      int shulkerColor = this.getShulkerColor(shulkerItem);
      int bgColor = ObsidianComponent_olorUtil.applyAlpha(shulkerColor, 0.8500000238418579f);
      int darkerColor = this.darkenColor(shulkerColor, 0.6000000238418579f);
      int lighterColor = this.lightenColor(shulkerColor, 1.2999999523162842f);
      matrices.push();
      GL11.glClear(256);
      RenderSystem.disableDepthTest();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      matrices.translate(
         0.0f,
         0.0f,
         500.0f
      );
      RenderUtil.drawBlur(
         matrices,
         x - 2.0f,
         y - 2.0f,
         totalWidth + 4.0f,
         totalHeight + 4.0f,
         7.0f,
         8.0f,
         -1
      );
      context.fill((int)x, (int)y, (int)(x + totalWidth), (int)(y + totalHeight), bgColor);
      context.fill((int)x, (int)y, (int)(x + totalWidth), (int)(y + 2.0f), lighterColor);
      context.fill(
         (int)x, (int)(y + totalHeight - 2.0f), (int)(x + totalWidth), (int)(y + totalHeight), darkerColor
      );
      context.fill((int)x, (int)y, (int)(x + 2.0f), (int)(y + totalHeight), lighterColor);
      context.fill(
         (int)(x + totalWidth - 2.0f), (int)y, (int)(x + totalWidth), (int)(y + totalHeight), darkerColor
      );
      FontRenderer font = ObsidianComponent_201.getFont("sf_regular", 12);
      if (font != null) {
         String title = shulkerItem.getName().getString();
         float titleX = x + 7.0f;
         float titleY = y + 7.0f - 1.0f;
         int textColor = this.isColorDark(shulkerColor) ? -1 : -15066598;
         font.drawString(matrices, title, titleX, titleY, textColor);
      }

      float slotsX = x + 7.0f;
      float slotsY = y
         + 7.0f
         + 14.0f
         - 2.0f;
      int slotAreaBg = this.darkenColor(shulkerColor, 0.5f);
      context.fill(
         (int)(slotsX - 1.0f),
         (int)(slotsY - 1.0f),
         (int)(slotsX + contentWidth + 1.0f),
         (int)(slotsY + contentHeight + 1.0f),
         slotAreaBg
      );
      List<ItemStack> items = new ArrayList<>();
      container.stream().forEach(items::add);

      for (int i = 0; i < 27; i++) {
         int row = i / 9;
         int col = i % (9);
         int slotX = (int)(slotsX + (float)(col * (18)));
         int slotY = (int)(slotsY + (float)(row * (18)));
         context.fill(
            slotX,
            slotY,
            slotX + 18 - (2),
            slotY + (18) - (2),
            -7631989
         );
         context.fill(
            slotX,
            slotY,
            slotX + 18 - 2,
            slotY + 1,
            -11184811
         );
         context.fill(
            slotX,
            slotY,
            slotX + (1),
            slotY + (18) - 2,
            -11184811
         );
         context.fill(
            slotX,
            slotY + 18 - 3,
            slotX + 18 - (2),
            slotY + (18) - 2,
            -1
         );
         context.fill(
            slotX + (18) - (3),
            slotY,
            slotX + (18) - (2),
            slotY + 18 - 2,
            -1
         );
         if (i < items.size()) {
            ItemStack itemStack = items.get(i);
            if (!itemStack.isEmpty()) {
               context.drawItem(itemStack, slotX, slotY);
               context.drawStackOverlay(mc.textRenderer, itemStack, slotX, slotY);
            }
         }
      }

      RenderSystem.enableDepthTest();
      RenderSystem.disableBlend();
      matrices.pop();
   }
   public int darkenColor(int color, float factor) {
      int a = color >> 24 & (255);
      int r = (int)((float)(color >> 16 & 255) * factor);
      int g = (int)((float)(color >> (8) & (255)) * factor);
      int b = (int)((float)(color & (255)) * factor);
      return a << 24
         | Math.min(255, r) << (16)
         | Math.min(255, g) << (8)
         | Math.min(255, b);
   }
   public int lightenColor(int color, float factor) {
      int a = color >> (24) & (255);
      int r = (int)Math.min(
         255.0f, (float)(color >> (16) & (255)) * factor
      );
      int g = (int)Math.min(
         255.0f,
         (float)(color >> 8 & (255)) * factor
      );
      int b = (int)Math.min(255.0f, (float)(color & (255)) * factor);
      return a << (24) | r << 16 | g << (8) | b;
   }
   public boolean isColorDark(int color) {
      int r = color >> (16) & (255);
      int g = color >> (8) & (255);
      int b = color & 255;
      double luminance = (
            0.299 * (double)r
               + 0.587 * (double)g
               + 0.114 * (double)b
         )
         / 255.0;
      return (luminance < 0.5);
   }
}
