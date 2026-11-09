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
























import java.util.List;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.MathHelper;

public class ObsidianComponent_248 implements IInstance {
   public final ObsidianComponent_271 state;
   public final ObsidianComponent_325 themeManager;
   public final ObsidianComponent_383 configManager;

   public ObsidianComponent_248(ObsidianComponent_271 state, ObsidianComponent_325 themeManager) {
      this.state = state;
      this.themeManager = themeManager;
      this.configManager = new ObsidianComponent_383();
   }

   public ObsidianComponent_248(ObsidianComponent_271 state, ObsidianComponent_325 themeManager, ObsidianComponent_383 configManager) {
      this.state = state;
      this.themeManager = themeManager;
      this.configManager = configManager;
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button, Window window) {
      if (window == null) {
         return false;
      } else if (this.configManager.isHovered(mouseX, mouseY, this.state.getRenderOffsetY())) {
         return this.configManager.mouseClicked(mouseX, mouseY, button, this.state.getRenderOffsetY());
      } else if (this.themeManager.isHovered(mouseX, mouseY, this.state.getRenderOffsetY())) {
         return this.themeManager.mouseClicked(mouseX, mouseY, button, this.state.getRenderOffsetY());
      } else {
         int categoryCount = Category.values().length;
         float totalWidth = ObsidianComponent_200.getTotalCategoriesWidth(categoryCount);
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
         if (button == 0) {
            float searchW = this.getSearchWidth();
            float searchX = ObsidianComponent_200.getSearchX(guiX, categoryCount, searchW);
            float searchY = ObsidianComponent_200.getSearchY(guiY + this.state.getRenderOffsetY());
            boolean searchHovered = MathUtil.isHovered(
               mouseX,
               mouseY,
               (double)searchX,
               (double)searchY,
               (double)searchW,
               18.0
            );
            this.state.setSearchActive(searchHovered);
            if (searchHovered) {
               this.state.setEditingTextSetting(null);
               this.state.startSearchSelection(this.getSearchIndexAt(mouseX, searchX));
               return true;
            }
         }

         if (this.state.getBindingModule() != null && button >= (2)) {
            this.state.getBindingModule().setKey(ObsidianComponent_349.createMouseBind(button));
            this.state.setBindingModule(null);
            return true;
         } else if (this.state.getBindingSetting() != null && button >= (2)) {
            this.state.getBindingSetting().setKey(ObsidianComponent_349.createMouseBind(button));
            this.state.setBindingSetting(null);
            return true;
         } else {
            this.state.setEditingTextSetting(null);
            Category[] categories = Category.values();

            for (int i = 0; i < categories.length; i++) {
               Category category = categories[i];
               float panelX = ObsidianComponent_200.getCategoryPanelX(guiX, i);
               float contentY = ObsidianComponent_200.getContentY(guiY + this.state.getRenderOffsetY());
               float contentHeight = ObsidianComponent_200.getCategoryVisibleHeight(this.state, category, (float)window.getScaledHeight(), contentY);
               float panelY = guiY + this.state.getRenderOffsetY();
               if (button == 0
                  && MathUtil.isHovered(
                     mouseX,
                     mouseY,
                     (double)panelX,
                     (double)(panelY - 150.0f),
                     115.0,
                     20.0
                  )) {
                  return false;
               }

               if (MathUtil.isHovered(
                  mouseX,
                  mouseY,
                  (double)panelX,
                  (double)contentY,
                  115.0,
                  (double)contentHeight
               )) {
                  float moduleY = contentY + this.state.getScroll(category);

                  for (Module module : this.state.getModules(category)) {
                     float openProgress = this.state.getOpenProgress(module);
                     float moduleHeight = ObsidianComponent_200.getModuleHeight(module, openProgress);
                     if (MathUtil.isHovered(
                        mouseX,
                        mouseY,
                        (double)(panelX + 0.0f),
                        (double)moduleY,
                        115.0,
                        20.0
                     )) {
                        if (button == 0) {
                           module.toggle();
                           return true;
                        }

                        if (button == 1) {
                           module.setOpen((!module.isOpen()));
                           this.state.clampScroll(category, contentHeight);
                           return true;
                        }

                        if (button == 2) {
                           this.state.setBindingModule(module);
                           return true;
                        }

                        return true;
                     }

                     if (module.isOpen()
                        && openProgress > 0.10000000149011612f
                        && this.handleSettingClick(mouseX, mouseY, button, panelX, moduleY, module, openProgress, (float)window.getScaledHeight())) {
                        return true;
                     }

                     moduleY += 0.0f + moduleHeight;
                  }
               }
            }

            return false;
         }
      }
   }

   public boolean mouseReleased(int button) {
      this.state.stopSearchSelection();
      if (button == 0) {
         for (Module module : this.state.getAllModules()) {
            List<Setting> settings = module.getSettings();
            if (settings != null) {
               for (Setting setting : settings) {
                  if (setting instanceof SliderSetting floatSetting) {
                     floatSetting.setActive(false);
                     this.state.endSliderDrag(floatSetting);
                  }
               }
            }
         }
      }

      return false;
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button) {
      Window window = mc.getWindow();
      if (window == null) {
         return false;
      } else if (button == 0 && this.state.isSearchActive() && this.state.isSearchDragging()) {
         int categoryCount = Category.values().length;
         float totalWidth = ObsidianComponent_200.getTotalCategoriesWidth(categoryCount);
         float guiX = MathHelper.clamp(
            this.state.getX(),
            10.0f,
            Math.max(
               10.0f,
               (float)window.getScaledWidth() - totalWidth - 10.0f
            )
         );
         float searchX = ObsidianComponent_200.getSearchX(guiX, categoryCount, this.getSearchWidth());
         this.state.updateSearchSelection(this.getSearchIndexAt(mouseX, searchX));
         return true;
      } else {
         return false;
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
      Window window = mc.getWindow();
      if (window == null) {
         return false;
      } else if (this.configManager.mouseScrolled(mouseX, mouseY, verticalAmount, this.state.getRenderOffsetY())) {
         return true;
      } else if (this.themeManager.mouseScrolled(mouseX, mouseY, verticalAmount, this.state.getRenderOffsetY())) {
         return true;
      } else {
         int categoryCount = Category.values().length;
         float totalWidth = ObsidianComponent_200.getTotalCategoriesWidth(categoryCount);
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
         Category[] categories = Category.values();

         for (int i = 0; i < categories.length; i++) {
            Category category = categories[i];
            float panelX = ObsidianComponent_200.getCategoryPanelX(guiX, i);
            float contentY = ObsidianComponent_200.getContentY(guiY + this.state.getRenderOffsetY());
            float contentHeight = ObsidianComponent_200.getCategoryVisibleHeight(this.state, category, (float)window.getScaledHeight(), contentY);
            if (MathUtil.isHovered(
               mouseX,
               mouseY,
               (double)panelX,
               (double)contentY,
               115.0,
               (double)contentHeight
            )) {
               this.state.addScroll(category, verticalAmount, contentHeight);
               return true;
            }
         }

         return false;
      }
   }

   public boolean keyPressed(int keyCode, int modifiers) {
      if (this.configManager.keyPressed(keyCode, modifiers)) {
         return true;
      } else if (this.state.getEditingTextSetting() != null) {
         ObsidianComponent_341 textSetting = this.state.getEditingTextSetting();
         if (keyCode == 256 || keyCode == 257 || keyCode == (335)) {
            this.state.setEditingTextSetting(null);
            return true;
         } else if (keyCode == 259) {
            String current = textSetting.get();
            if (current != null && !current.isEmpty()) {
               textSetting.setText(current.substring(0, current.length() - (1)));
            }

            return true;
         } else {
            return true;
         }
      } else {
         if (this.state.isSearchActive()) {
            if ((modifiers & (2)) != 0) {
               if (keyCode == (65)) {
                  this.state.selectAllSearchText();
                  return true;
               }

               if (keyCode == 67) {
                  if (this.state.hasSearchSelection() && mc != null && mc.keyboard != null) {
                     mc.keyboard.setClipboard(this.state.getSelectedSearchText());
                  }

                  return true;
               }

               if (keyCode == 86) {
                  if (mc != null && mc.keyboard != null) {
                     this.state.replaceSearchSelection(mc.keyboard.getClipboard());
                  }

                  return true;
               }

               if (keyCode == 90) {
                  this.state.restoreSearchUndo();
                  return true;
               }
            }

            if (keyCode == (256) || keyCode == (257) || keyCode == 335) {
               this.state.setSearchActive(false);
               return true;
            }

            if (keyCode == 259) {
               this.state.removeLastSearchChar();
               return true;
            }

            if (keyCode == (261)) {
               this.state.clearSearchText();
               return true;
            }

            if (keyCode == (263)) {
               this.state
                  .setSearchCursor(
                     this.state.getSearchCursor() - (1),
                     ((modifiers & (1)) != 0)
                  );
               return true;
            }

            if (keyCode == (262)) {
               this.state
                  .setSearchCursor(
                     this.state.getSearchCursor() + (1),
                     ((modifiers & 1) != 0)
                  );
               return true;
            }
         }

         if (this.state.getBindingModule() != null) {
            if (keyCode == 256) {
               this.state.setBindingModule(null);
            } else if (keyCode != 261 && keyCode != 259) {
               this.state.getBindingModule().setKey(keyCode);
               this.state.setBindingModule(null);
            } else {
               this.state.getBindingModule().setKey(-1);
               this.state.setBindingModule(null);
            }

            return true;
         } else if (this.state.getBindingSetting() == null) {
            return false;
         } else {
            if (keyCode == 256) {
               this.state.setBindingSetting(null);
            } else if (keyCode != (261) && keyCode != (259)) {
               this.state.getBindingSetting().setKey(keyCode);
               this.state.setBindingSetting(null);
            } else {
               this.state.getBindingSetting().setKey(-1);
               this.state.setBindingSetting(null);
            }

            return true;
         }
      }
   }

   public boolean charTyped(char chr) {
      if (this.configManager.charTyped(chr)) {
         return true;
      } else if (this.state.getEditingTextSetting() != null) {
         if (!Character.isISOControl(chr)) {
            ObsidianComponent_341 textSetting = this.state.getEditingTextSetting();
            textSetting.setText(textSetting.get() + chr);
         }

         return true;
      } else if (!this.state.isSearchActive()) {
         return false;
      } else {
         this.state.appendSearchChar(chr);
         return true;
      }
   }
   public int getSearchIndexAt(double mouseX, float searchX) {
      String text = this.state.getSearchText();
      float textX = searchX + 19.0f;
      float localX = (float)mouseX - textX;
      if (!(localX <= 0.0f) && !text.isEmpty()) {
         for (int i = 1; i <= text.length(); i++) {
            float previousWidth = this.issue(14)
               .getWidth(text.substring(0, i - (1)));
            float currentWidth = this.issue(14).getWidth(text.substring(0, i));
            float midpoint = previousWidth + (currentWidth - previousWidth) * 0.5f;
            if (localX < midpoint) {
               return i - 1;
            }
         }

         return text.length();
      } else {
         return 0;
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
   public boolean handleSettingClick(
      double mouseX, double mouseY, int button, float panelX, float moduleY, Module module, float openProgress, float windowHeight
   ) {
      List<Setting> settings = module.getSettings();
      if (settings != null && !settings.isEmpty() && !(openProgress <= 0.009999999776482582f)) {
         float maxSettingHeight = ObsidianComponent_200.calculateSettingsHeight(module);
         float settingsClipY = moduleY + 20.0f;
         float settingsClipHeight = maxSettingHeight * openProgress;
         float settingYoffset = 20.0f;

         for (Setting setting : settings) {
            if (setting != null && setting.visible()) {
               float settingY = moduleY + settingYoffset + 4.0f;
               if (settingY < settingsClipY || settingY > settingsClipY + settingsClipHeight) {
                  settingYoffset += this.getSettingHeight(setting);
               } else if (setting instanceof BooleanSetting booleanSetting) {
                  float x = panelX + 10.0f - 7.0f;
                  float width = panelX + 111.0f - x;
                  if (button == 0
                     && MathUtil.isHovered(
                        mouseX,
                        mouseY,
                        (double)x,
                        (double)(settingY - 2.0f),
                        (double)width,
                        12.0
                     )) {
                     booleanSetting.setState(
                        (!booleanSetting.isState())
                     );
                     return true;
                  }

                  settingYoffset += 12.0f;
               } else if (setting instanceof ObsidianComponent_341 textSetting) {
                  float boxWidth = 42.0f;
                  float boxX = panelX + 49.0f;
                  if (button == 0
                     && MathUtil.isHovered(
                        mouseX,
                        mouseY,
                        (double)boxX,
                        (double)(settingY - 2.5f),
                        (double)boxWidth,
                        9.0
                     )) {
                     this.state.setSearchActive(false);
                     this.state.stopSearchSelection();
                     this.state.setEditingTextSetting(textSetting);
                     return true;
                  }

                  settingYoffset += 22.0f;
               } else if (setting instanceof SliderSetting floatSetting) {
                  float sliderX = panelX + 10.0f - 6.0f;
                  float sliderWidth = 104.0f;
                  if (button == 0
                     && MathUtil.isHovered(
                        mouseX,
                        mouseY,
                        (double)sliderX,
                        (double)(settingY + 6.0f),
                        (double)sliderWidth,
                        10.0
                     )) {
                     floatSetting.setActive(true);
                     floatSetting.setValue(this.state.getSliderValue(floatSetting, sliderX, mouseX));
                     this.state.beginSliderDrag(floatSetting, mouseX);
                     return true;
                  }

                  settingYoffset += 22.0f;
               } else if (setting instanceof ModeSetting modeSetting) {
                  float modeY = settingY + 10.0f;

                  for (String mode : modeSetting.getMods()) {
                     float x = panelX + 10.0f - 6.0f;
                     float width = panelX + 115.0f - x;
                     if (button == 0
                        && MathUtil.isHovered(
                           mouseX,
                           mouseY,
                           (double)x,
                           (double)(modeY - 2.0f),
                           (double)width,
                           10.0
                        )) {
                        modeSetting.set(mode);
                        return true;
                     }

                     modeY += 10.0f;
                  }

                  settingYoffset += ObsidianComponent_200.calculateModeSettingHeight(modeSetting);
               } else if (!(setting instanceof MultiBooleanSetting listSetting)) {
                  if (setting instanceof ObsidianComponent_261 bindSetting) {
                     String bindString = this.state.getBindingSetting() == bindSetting
                        ? "..."
                        : this.state.toEnglish(ObsidianComponent_349.getBindName(bindSetting.getKey()));
                     float bindWidth = this.issue(12).getWidth(bindString)
                        + 6.0f;
                     float bindX = panelX + 89.0f - bindWidth;
                     if (button == 0
                        && MathUtil.isHovered(
                           mouseX,
                           mouseY,
                           (double)bindX,
                           (double)(settingY - 2.5f),
                           (double)bindWidth,
                           9.0
                        )) {
                        this.state.setBindingSetting(bindSetting);
                        return true;
                     }

                     settingYoffset += 12.0f;
                  }
               } else {
                  float listY = settingY + 10.0f;

                  for (BooleanSetting entry : listSetting.getSettings()) {
                     if (entry.visible()) {
                        float x = panelX
                           + 10.0f
                           - 6.0f;
                        float width = panelX + 115.0f - x;
                        if (button == 0 && MathUtil.isHovered(mouseX, mouseY, (double)x, (double)(listY - 2.0F), (double)width, 10.0)) {
                           entry.setState((!entry.isState()));
                           return true;
                        }

                        listY += 10.0f;
                     }
                  }

                  settingYoffset += ObsidianComponent_200.calculateListSettingHeight(listSetting);
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }
   public float getSettingHeight(Setting setting) {
      if (setting instanceof BooleanSetting || setting instanceof ObsidianComponent_261) {
         return 12.0f;
      } else if (setting instanceof ObsidianComponent_341 || setting instanceof SliderSetting) {
         return 22.0f;
      } else if (setting instanceof ModeSetting modeSetting) {
         return ObsidianComponent_200.calculateModeSettingHeight(modeSetting);
      } else {
         return setting instanceof MultiBooleanSetting listSetting
            ? ObsidianComponent_200.calculateListSettingHeight(listSetting)
            : 0.0f;
      }
   }
   public FontRenderer issue(int size) {
      return ObsidianComponent_201.getFont("suisse", size);
   }
}
