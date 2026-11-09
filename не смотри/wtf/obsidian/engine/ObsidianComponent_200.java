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

public final class ObsidianComponent_200 {
   public static final float WIDTH = 115.0F;
   public static final float HEIGHT = 260.0F;
   public static final float CATEGORY_PANEL_STEP = 124.0F;
   public static final float CATEGORY_HEADER_HEIGHT = 20.0F;
   public static final float THEME_PANEL_Y = 0.0F;
   public static final float THEME_PANEL_H = 15.0F;
   public static final float THEME_BOX_SIZE = 8.0F;
   public static final float THEME_BOX_GAP = 4.0F;
   public static final float THEME_BOX_RADIUS = 2.0F;
   public static final float THEME_SIDE_PADDING = 4.0F;
   public static final float MODULE_PADDING = 0.0F;
   public static final float MODULE_GAP = 0.0F;
   public static final float MODULE_HEADER_HEIGHT = 20.0F;
   public static final float MODULE_INNER_WIDTH = 115.0F;
   public static final float SETTING_START_Y = 20.0F;
   public static final float SETTING_PADDING = 4.0F;
   public static final float SETTING_BOTTOM_PADDING = 3.0F;
   public static final float SETTING_LEFT = 10.0F;
   public static final float SETTING_RIGHT = 89.0F;
   public static final float SLIDER_WIDTH = 84.0F;
   public static final float TEXT_SETTING_WIDTH = 42.0F;
   public static final float CLICKABLE_WIDTH = 79.0F;
   public static final int SEARCH_MAX_CHARS = 24;
   public static final float SEARCH_WIDTH = 75.0F;
   public static final float SEARCH_HEIGHT = 18.0F;
   public static final float SEARCH_GAP = 8.0F;
   public static final float SEARCH_ICON_X = 3.5F;
   public static final float SEARCH_TEXT_X = 19.0F;
   public static final float SEARCH_RIGHT_PADDING = 8.0F;

   public ObsidianComponent_200() {
   }

   public static float getTotalCategoriesWidth(int categoryCount) {
      return 115.0f * (float)categoryCount
         + 8.0f * (float)(categoryCount - (1));
   }

   public static float getCategoryPanelX(float x, int index) {
      return x + (float)index * 124.0f;
   }

   public static float getCenteredY(float windowHeight) {
      float totalGuiHeight = 286.0f;
      return windowHeight / 2.0f
         - totalGuiHeight / 2.0f;
   }

   /** @deprecated */
   public static float getContentY(float y) {
      return y;
   }

   public static float getSearchX(float x, int categoryCount) {
      return x
         + getTotalCategoriesWidth(categoryCount) / 2.0f
         - 37.5f;
   }

   public static float getSearchX(float x, int categoryCount, float searchWidth) {
      return x
         + getTotalCategoriesWidth(categoryCount) / 2.0f
         - searchWidth / 2.0f;
   }

   public static float getSearchY(float panelY) {
      return panelY + 260.0f + 8.0f;
   }

   public static boolean hasVisibleSettings(List<Setting> settings) {
      for (Setting setting : settings) {
         if (setting != null && setting.visible()) {
            return true;
         }
      }

      return false;
   }

   public static float calculateModeSettingHeight(ModeSetting modeSetting) {
      return (float)(modeSetting.getMods().size() * 10 + 12);
   }

   public static float calculateListSettingHeight(MultiBooleanSetting listSetting) {
      int visibleCount = 0;

      for (BooleanSetting entry : listSetting.getSettings()) {
         if (entry.visible()) {
            visibleCount++;
         }
      }

      return (float)(visibleCount * 10 + 12);
   }

   public static float calculateSettingsHeight(Module module) {
      float height = 0.0f;
      List<Setting> settings = module.getSettings();
      if (settings != null && !settings.isEmpty()) {
         boolean hasVisibleSetting = false;

         for (Setting setting : settings) {
            if (setting != null && setting.visible()) {
               hasVisibleSetting = true;
               if (setting instanceof BooleanSetting || setting instanceof ObsidianComponent_261) {
                  height += 12.0f;
               } else if (setting instanceof ObsidianComponent_341) {
                  height += 12.0f;
               } else if (setting instanceof SliderSetting) {
                  height += 22.0f;
               } else if (setting instanceof ModeSetting modeSetting) {
                  height += calculateModeSettingHeight(modeSetting);
               } else if (setting instanceof MultiBooleanSetting listSetting) {
                  height += calculateListSettingHeight(listSetting);
               }
            }
         }

         if (hasVisibleSetting) {
            height += 3.0f;
         }

         return height;
      } else {
         return 0.0f;
      }
   }

   public static float getModuleHeight(Module module, float openProgress) {
      return 20.0f + calculateSettingsHeight(module) * openProgress;
   }

   public static float getCategoryComputedContentHeight(ObsidianComponent_271 state, Category category) {
      float modulesHeight = 0.0f;

      for (Module module : state.getModules(category)) {
         float openProgress = state.getOpenProgress(module);
         modulesHeight += 0.0F + getModuleHeight(module, openProgress);
      }

      return Math.max(60.0f, modulesHeight + 10.0f);
   }

   public static float getCategoryVisibleHeight(ObsidianComponent_271 state, Category category, float windowHeight, float contentY) {
      return 260.0f;
   }
}
