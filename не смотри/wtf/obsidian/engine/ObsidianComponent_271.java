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
























import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.util.Window;

public class ObsidianComponent_271 {
   private static final Map<Character, Character> RU_TO_EN = new HashMap<>();
   private final Map<Module, Float> dotsRotation = new HashMap<>();
   private final Map<Module, AnimationUtil> moduleOpenAnimation = new HashMap<>();
   private final Map<BooleanSetting, AnimationUtil> booleanBackgroundAnimation = new HashMap<>();
   private final Map<BooleanSetting, AnimationUtil> booleanCircleAnimation = new HashMap<>();
   private final Map<SliderSetting, AnimationUtil> sliderAnimation = new HashMap<>();
   private final Map<SliderSetting, Double> sliderDragMouseX = new HashMap<>();
   private final Map<SliderSetting, Double> sliderDragRemainder = new HashMap<>();
   private final Map<String, AnimationUtil> modeAnimation = new HashMap<>();
   private final Map<String, AnimationUtil> listAnimation = new HashMap<>();
   private final Map<String, AnimationUtil> bindAnimation = new HashMap<>();
   private final Map<String, AnimationUtil> textHoverAnimation = new HashMap<>();
   private final Map<String, Float> textScrollPhase = new HashMap<>();
   private final Map<String, Boolean> textScrollFinishing = new HashMap<>();
   private final Map<String, Boolean> textScrollHovered = new HashMap<>();
   private final Map<Category, Float> categoryScrollTarget = new EnumMap<>(Category.class);
   private final Map<Category, AnimationUtil> categoryScrollAnimation = new EnumMap<>(Category.class);
   private final Map<Category, List<Module>> modulesByCategory = new EnumMap<>(Category.class);
   private final List<Module> allModules = new ArrayList<>();
   public float x;
   public float y;
   public ObsidianComponent_261 bindingSetting;
   public ObsidianComponent_341 editingTextSetting;
   public Module bindingModule;
   public float renderOffsetY;
   public boolean searchActive;
   public String searchText = "";
   public String undoSearchText = "";
   public int searchCursor = 0;
   public int searchSelectionAnchor = 0;
   public int searchSelectionCursor = 0;
   public boolean searchDragging;

   public ObsidianComponent_271() {
      this.refreshModules();
   }

   public void refreshModules() {
      this.allModules.clear();
      this.allModules
         .addAll(
            ModuleManager.INSTANCE
               .getObject()
               .stream()
               .filter(
                  module -> (!"AutoBuy".equals(module.getName()) && !"AutoForest".equals(module.getName()))
               )
               .toList()
         );
      Category[] var1 = Category.values();
      int var2 = var1.length;

      for (int var3 = 0; var3 < var2; var3++) {
         Category category = var1[var3];
         this.modulesByCategory
            .put(
               category,
               this.allModules
                  .stream()
                  .filter(module -> (module.getCategory() == category))
                  .toList()
            );
         this.categoryScrollTarget.putIfAbsent(category, 0.0f);
         this.categoryScrollAnimation
            .putIfAbsent(
               category,
               new AnimationUtil(
                  0.0f, 8.0f, ObsidianComponent_57.CUBIC_OUT
               )
            );
      }
   }

   public void updatePosition(Window window, int categoryCount) {
      float totalCategoriesWidth = ObsidianComponent_200.getTotalCategoriesWidth(categoryCount);
      this.x = (float)window.getScaledWidth() / 2.0f
         - totalCategoriesWidth / 2.0f;
      this.y = (float)window.getScaledHeight() / 2.0f
         - 130.0f;
   }

   public float getX() {
      return this.x;
   }

   public float getY() {
      return this.y;
   }

   public float getRenderOffsetY() {
      return this.renderOffsetY;
   }

   public void setRenderOffsetY(float renderOffsetY) {
      this.renderOffsetY = renderOffsetY;
   }

   public List<Module> getModules(Category category) {
      List<Module> modules = this.modulesByCategory.getOrDefault(category, List.of());
      if (this.searchText.isBlank()) {
         return modules;
      } else {
         String query = this.searchText.toLowerCase(Locale.ROOT);
         return modules.stream()
            .filter(
               module -> (!(!module.getName().toLowerCase(Locale.ROOT).contains(query)
                        && !module.getDisplayName().toLowerCase(Locale.ROOT).contains(query)
                        && !module.getDisplayDescription().toLowerCase(Locale.ROOT).contains(query)))
            )
            .toList();
      }
   }

   public List<Module> getAllModules() {
      return this.allModules;
   }

   public String toEnglish(String text) {
      StringBuilder result = new StringBuilder();
      char[] var3 = text.toCharArray();
      int var4 = var3.length;

      for (int var5 = 0; var5 < var4; var5++) {
         char c = var3[var5];
         result.append(RU_TO_EN.getOrDefault(c, c));
      }

      return result.toString();
   }

   public float getSliderPos(SliderSetting setting) {
      float delta = setting.getMax() - setting.getMin();
      return (setting.get() - setting.getMin()) / delta;
   }

   public float getSliderValue(SliderSetting setting, float posX, double mouseX) {
      float delta = setting.getMax() - setting.getMin();
      float clickedX = (float)mouseX - posX;
      float value = Math.max(
         0.0f,
         Math.min(1.0f, clickedX / 84.0f)
      );
      float outValue = setting.getMin() + delta * value;
      float increment = setting.getIncrement();
      outValue = (float)Math.round(outValue / increment) * increment;
      return Math.max(setting.getMin(), Math.min(setting.getMax(), outValue));
   }

   public void beginSliderDrag(SliderSetting setting, double mouseX) {
      this.sliderDragMouseX.put(setting, mouseX);
      this.sliderDragRemainder.put(setting, 0.0);
   }

   public void endSliderDrag(SliderSetting setting) {
      this.sliderDragMouseX.remove(setting);
      this.sliderDragRemainder.remove(setting);
   }

   public float updateActiveSliderValue(SliderSetting setting, double mouseX) {
      double lastMouseX = this.sliderDragMouseX.getOrDefault(setting, mouseX);
      this.sliderDragMouseX.put(setting, mouseX);
      double deltaX = mouseX - lastMouseX;
      if (Math.abs(deltaX) < 0.0001) {
         return setting.get();
      } else {
         float range = setting.getMax() - setting.getMin();
         float increment = setting.getIncrement();
         if (!(range <= 0.0f)
            && !(increment <= 0.0f)) {
            double steps = (double)(range / increment);
            if (steps <= 0.0) {
               return setting.get();
            } else {
               double pixelsPerStep = 84.0 / steps;
               if (pixelsPerStep <= 0.0) {
                  return setting.get();
               } else {
                  double accumulated = this.sliderDragRemainder
                        .getOrDefault(setting, 0.0)
                     + deltaX;
                  int wholeSteps = (int)(accumulated / pixelsPerStep);
                  if (wholeSteps == 0) {
                     this.sliderDragRemainder.put(setting, accumulated);
                     return setting.get();
                  } else {
                     this.sliderDragRemainder.put(setting, accumulated - (double)wholeSteps * pixelsPerStep);
                     float value = setting.get() + (float)wholeSteps * increment;
                     value = (float)Math.round(value / increment) * increment;
                     return Math.max(setting.getMin(), Math.min(setting.getMax(), value));
                  }
               }
            }
         } else {
            return setting.get();
         }
      }
   }

   public float getScroll(Category category) {
      AnimationUtil animation = this.categoryScrollAnimation
         .computeIfAbsent(
            category,
            key -> new AnimationUtil(
                  0.0f, 8.0f, ObsidianComponent_57.CUBIC_OUT
               )
         );
      animation.update(this.categoryScrollTarget.getOrDefault(category, 0.0f));
      return animation.getValue();
   }

   public void clampScroll(Category category, float contentHeight) {
      float totalHeight = this.getTotalModulesHeight(category);
      float maxScroll = Math.min(0.0f, contentHeight - totalHeight);
      float currentTarget = this.categoryScrollTarget.getOrDefault(category, 0.0f);
      if (currentTarget < maxScroll || currentTarget > 0.0f) {
         this.categoryScrollTarget.put(category, Math.max(maxScroll, Math.min(0.0f, currentTarget)));
      }
   }

   public void addScroll(Category category, double verticalAmount, float contentHeight) {
      float totalHeight = this.getTotalModulesHeight(category);
      float maxScroll = Math.min(0.0f, contentHeight - totalHeight);
      float currentTarget = this.categoryScrollTarget.getOrDefault(category, 0.0f);
      float newTarget = currentTarget + (float)(verticalAmount * 20.0);
      this.categoryScrollTarget.put(category, Math.max(maxScroll, Math.min(0.0f, newTarget)));
   }

   public float getTotalModulesHeight(Category category) {
      float totalHeight = 0.0f;

      for (Module module : this.getModules(category)) {
         totalHeight += 0.0f + ObsidianComponent_200.getModuleHeight(module, this.getOpenProgress(module));
      }

      return totalHeight;
   }

   public float getOpenProgress(Module module) {
      AnimationUtil animation = this.moduleOpenAnimation
         .computeIfAbsent(
            module,
            key -> new AnimationUtil(
                  module.isOpen() ? 1.0f : 0.0f,
                  14.0f,
                  ObsidianComponent_57.CUBIC_OUT
               )
         );
      animation.update(
         module.isOpen() ? 1.0f : 0.0f
      );
      return animation.getValue();
   }

   public float updateDotsRotation(Module module, float targetAngle) {
      float currentAngle = this.dotsRotation.getOrDefault(module, targetAngle);
      currentAngle += (targetAngle - currentAngle) * 0.05999999865889549f;
      if (Math.abs(targetAngle - currentAngle) < 0.0010000000474974513f) {
         currentAngle = targetAngle;
      }

      this.dotsRotation.put(module, currentAngle);
      return currentAngle;
   }

   public AnimationUtil getBooleanBackgroundAnimation(BooleanSetting setting) {
      return this.booleanBackgroundAnimation
         .computeIfAbsent(
            setting,
            key -> new AnimationUtil(
                  setting.isState() ? 1.0f : 0.0f,
                  15.0f,
                  ObsidianComponent_57.CUBIC_OUT
               )
         );
   }

   public AnimationUtil getBooleanCircleAnimation(BooleanSetting setting) {
      return this.booleanCircleAnimation
         .computeIfAbsent(
            setting,
            key -> new AnimationUtil(
                  setting.isState() ? 1.0f : 0.0f,
                  8.199999809265137f,
                  ObsidianComponent_57.BACK_OUT
               )
         );
   }

   public AnimationUtil getSliderAnimation(SliderSetting setting) {
      return this.sliderAnimation
         .computeIfAbsent(
            setting, key -> new AnimationUtil(this.getSliderPos(setting), 12.0f, ObsidianComponent_57.CUBIC_OUT)
         );
   }

   public AnimationUtil getModeAnimation(String key, boolean selected) {
      return this.modeAnimation
         .computeIfAbsent(
            key,
            unused -> new AnimationUtil(
                  selected ? 1.0f : 0.0f,
                  10.0f,
                  ObsidianComponent_57.CUBIC_OUT
               )
         );
   }

   public AnimationUtil getListAnimation(String key, boolean selected) {
      return this.listAnimation
         .computeIfAbsent(
            key,
            unused -> new AnimationUtil(
                  selected ? 1.0f : 0.0f,
                  10.0f,
                  ObsidianComponent_57.CUBIC_OUT
               )
         );
   }

   public AnimationUtil getBindAnimation(String key, boolean binding) {
      return this.bindAnimation
         .computeIfAbsent(
            key,
            unused -> new AnimationUtil(
                  binding ? 1.0f : 0.0f,
                  10.0f,
                  ObsidianComponent_57.CUBIC_OUT
               )
         );
   }

   public AnimationUtil getTextHoverAnimation(String key, boolean hovered) {
      return this.textHoverAnimation
         .computeIfAbsent(
            key,
            unused -> new AnimationUtil(
                  hovered ? 1.0f : 0.0f,
                  9.0f,
                  ObsidianComponent_57.CUBIC_OUT
               )
         );
   }

   public float advanceTextScrollPhase(String key, boolean hovered) {
      float phase = this.textScrollPhase.getOrDefault(key, 0.0f);
      boolean wasHovered = this.textScrollHovered.getOrDefault(key, Boolean.valueOf(false));
      boolean finishing = this.textScrollFinishing.getOrDefault(key, Boolean.valueOf(false));
      if (hovered) {
         phase += 0.004000000189989805f;
         if (phase > 1.0f) {
            phase -= 1.0f;
         }

         finishing = false;
      } else {
         if (wasHovered && phase > 0.0f) {
            finishing = true;
         }

         if (finishing) {
            phase += 0.004000000189989805f;
            if (phase >= 1.0f) {
               phase = 0.0f;
               finishing = false;
            }
         }
      }

      this.textScrollHovered.put(key, hovered);
      this.textScrollFinishing.put(key, finishing);
      this.textScrollPhase.put(key, phase);
      return phase;
   }

   public boolean isTextScrollActive(String key, boolean hovered) {
      return (!(!hovered && !this.textScrollFinishing.getOrDefault(key, Boolean.valueOf(false))));
   }

   public ObsidianComponent_261 getBindingSetting() {
      return this.bindingSetting;
   }

   public void setBindingSetting(ObsidianComponent_261 bindingSetting) {
      this.bindingSetting = bindingSetting;
   }

   public Module getBindingModule() {
      return this.bindingModule;
   }

   public void setBindingModule(Module bindingModule) {
      this.bindingModule = bindingModule;
   }

   public ObsidianComponent_341 getEditingTextSetting() {
      return this.editingTextSetting;
   }

   public void setEditingTextSetting(ObsidianComponent_341 editingTextSetting) {
      this.editingTextSetting = editingTextSetting;
   }

   public boolean isSearchActive() {
      return this.searchActive;
   }

   public void setSearchActive(boolean searchActive) {
      this.searchActive = searchActive;
   }

   public String getSearchText() {
      return this.searchText;
   }

   public void appendSearchChar(char chr) {
      if (!Character.isISOControl(chr) && (this.searchText.length() < 24 || this.hasSearchSelection())) {
         this.replaceSearchSelection(String.valueOf(chr));
      }
   }

   public void removeLastSearchChar() {
      if (this.hasSearchSelection()) {
         this.replaceSearchSelection("");
      } else {
         if (this.searchCursor > 0) {
            this.rememberSearchUndo();
            this.searchText = this.searchText.substring(0, this.searchCursor - 1)
               + this.searchText.substring(this.searchCursor);
            this.searchCursor -= 1;
            this.clearSearchSelection();
         }
      }
   }

   public void clearSearchText() {
      this.rememberSearchUndo();
      this.searchText = "";
      this.searchCursor = 0;
      this.clearSearchSelection();
   }

   public void setSearchText(String searchText) {
      this.rememberSearchUndo();
      this.searchText = this.sanitizeSearchText(searchText);
      this.searchCursor = this.searchText.length();
      this.clearSearchSelection();
   }

   public void restoreSearchUndo() {
      String current = this.searchText;
      this.searchText = this.undoSearchText == null ? "" : this.undoSearchText;
      this.undoSearchText = current;
      this.searchCursor = this.searchText.length();
      this.clearSearchSelection();
   }

   public int getSearchCursor() {
      return this.searchCursor;
   }

   public int getSearchSelectionStart() {
      return Math.min(this.searchSelectionAnchor, this.searchSelectionCursor);
   }

   public int getSearchSelectionEnd() {
      return Math.max(this.searchSelectionAnchor, this.searchSelectionCursor);
   }

   public boolean hasSearchSelection() {
      return (this.getSearchSelectionStart() != this.getSearchSelectionEnd());
   }

   public String getSelectedSearchText() {
      return !this.hasSearchSelection() ? "" : this.searchText.substring(this.getSearchSelectionStart(), this.getSearchSelectionEnd());
   }

   public void selectAllSearchText() {
      this.searchSelectionAnchor = 0;
      this.searchSelectionCursor = this.searchText.length();
      this.searchCursor = this.searchText.length();
   }

   public void setSearchCursor(int cursor, boolean keepSelection) {
      this.searchCursor = this.clampSearchIndex(cursor);
      if (keepSelection) {
         this.searchSelectionCursor = this.searchCursor;
      } else {
         this.searchSelectionAnchor = this.searchCursor;
         this.searchSelectionCursor = this.searchCursor;
      }
   }

   public void startSearchSelection(int index) {
      this.searchCursor = this.clampSearchIndex(index);
      this.searchSelectionAnchor = this.searchCursor;
      this.searchSelectionCursor = this.searchCursor;
      this.searchDragging = true;
   }

   public void updateSearchSelection(int index) {
      if (this.searchDragging) {
         this.searchCursor = this.clampSearchIndex(index);
         this.searchSelectionCursor = this.searchCursor;
      }
   }

   public void stopSearchSelection() {
      this.searchDragging = false;
   }

   public boolean isSearchDragging() {
      return this.searchDragging;
   }

   public void replaceSearchSelection(String text) {
      this.rememberSearchUndo();
      String insert = this.sanitizeSearchText(text);
      int selectionStart = this.getSearchSelectionStart();
      int selectionEnd = this.getSearchSelectionEnd();
      if (!this.hasSearchSelection()) {
         selectionStart = this.searchCursor;
         selectionEnd = this.searchCursor;
      }

      int available = Math.max(
         0, (24) - (this.searchText.length() - (selectionEnd - selectionStart))
      );
      if (insert.length() > available) {
         insert = insert.substring(0, available);
      }

      this.searchText = this.searchText.substring(0, selectionStart)
         + insert
         + this.searchText.substring(selectionEnd);
      this.searchCursor = selectionStart + insert.length();
      this.clearSearchSelection();
   }
   public void clearSearchSelection() {
      this.searchSelectionAnchor = this.searchCursor;
      this.searchSelectionCursor = this.searchCursor;
      this.searchDragging = false;
   }
   public int clampSearchIndex(int index) {
      return Math.max(0, Math.min(this.searchText.length(), index));
   }
   public void rememberSearchUndo() {
      this.undoSearchText = this.searchText;
   }
   public String sanitizeSearchText(String text) {
      if (text != null && !text.isEmpty()) {
         StringBuilder builder = new StringBuilder();

         for (int i = 0; i < text.length() && builder.length() < (24); i++) {
            char chr = text.charAt(i);
            if (!Character.isISOControl(chr)) {
               builder.append(chr);
            }
         }

         return builder.toString();
      } else {
         return "";
      }
   }

   static {
      String ru = "йцукенгшщзхъфывапролджэячсмитьбюЙЦУКЕНГШЩЗХЪФЫВАПРОЛДЖЭЯЧСМИТЬБЮ";
      String en = "qwertyuiop[]asdfghjkl;'zxcvbnm,.QWERTYUIOP[]ASDFGHJKL;'ZXCVBNM,.";
      int length = Math.min(ru.length(), en.length());

      for (int i = 0; i < length; i++) {
         RU_TO_EN.put(ru.charAt(i), en.charAt(i));
      }
   }
}
