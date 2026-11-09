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

























import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

public class ArrayListHudElement extends DraggableHudElement {
   public static final int STATUS_VANISH_COLOR = -47526;
   public static final int STATUS_GM3_COLOR = -9146;
   public static final int STATUS_ONLINE_COLOR = -10158216;
   private final MinecraftClient mc = MinecraftClient.getInstance();
   private final Map<String, ObsidianComponent_99> staffDataCache = new LinkedHashMap<>();
   private final Map<String, Float> staffAnimations = new HashMap<>();
   private final Set<String> activeStaff = new HashSet<>();
   private final Pattern namePattern = Pattern.compile("^\\w{3,16}$");
   private final Set<String> validStaffPrefixes = new HashSet<>();
   private final AnimationUtil widthAnimation = new AnimationUtil(
      60.0f, 10.5f, ObsidianComponent_57.QUAD_OUT
   );
   public float staffAnimatedHeight = 18.0f;
   public long lastStaffUpdate = 0L;
   private final List<String> visiblePlayers = new ArrayList<>();
   private final Set<String> animationScratch = new HashSet<>();
   public FontRenderer font10;
   public FontRenderer font12;
   public FontRenderer font14;
   public FontRenderer font16;
   public FontRenderer iconFont;

   public ArrayListHudElement(ObsidianComponent_280 draggable) {
      super(draggable);
      Set var10000 = this.validStaffPrefixes;
      String[] var10001 = new String[22];
      var10001[0] = "supp";
      var10001[1] = "ꜱupp";
      var10001[2] = "mod";
      var10001[3] = "der";
      var10001[4] = "adm";
      var10001[5] = "wne";
      var10001[6] = "мод";
      var10001[7] = "помо";
      var10001[8] = "адм";
      var10001[9] = "владе";
      var10001[10] = "отри";
      var10001[11] = "таф";
      var10001[12] = "taf";
      var10001[13] = "curat";
      var10001[14] = "курато";
      var10001[15] = "dev";
      var10001[16] = "раз";
      var10001[17] = "сапп";
      var10001[18] = "yt";
      var10001[19] = "ютуб";
      var10001[20] = "стажер";
      var10001[21] = "сотрудник";
      var10000.addAll(Arrays.asList(var10001));
   }
   public void initFonts() {
      if (this.font10 == null) {
         this.font10 = ObsidianComponent_201.getFont("suisse", 10);
         this.font12 = ObsidianComponent_201.getFont("suisse", 12);
         this.font14 = ObsidianComponent_201.getFont("suisse", 14);
         this.font16 = ObsidianComponent_201.getFont("suisse", 16);
         this.iconFont = ObsidianComponent_201.getFont("icon", 13);
      }
   }

   @Override
   public void onRender(EventRender2D eventRender) {
      if (this.mc.player != null && this.mc.world != null) {
         this.initFonts();
         long currentTime = System.currentTimeMillis();
         if (currentTime - this.lastStaffUpdate > (500L)) {
            this.updateStaffCache();
            this.lastStaffUpdate = currentTime;
         }

         this.updateAnimations();
         if (ModuleManager.interfaceModule.style.is("Обычный")) {
            this.renderDefaultStyle(eventRender);
         } else {
            this.renderWaveStyle(eventRender);
         }

         super.onRender(eventRender);
      }
   }
   public boolean matchesStaffPrefix(String prefix) {
      String lower = prefix.toLowerCase(Locale.ROOT);

      for (String p : this.validStaffPrefixes) {
         if (lower.contains(p)) {
            return true;
         }
      }

      return false;
   }
   public List<ObsidianComponent_432> parsePrefix(Text prefix) {
      List<ObsidianComponent_432> segments = new ArrayList<>();
      prefix.visit((style, string) -> {
         if (string != null && !string.isEmpty()) {
            this.appendPrefixSegments(segments, string, style.getColor() != null ? style.getColor().getRgb() : 16777215);
            return Optional.empty();
         } else {
            return Optional.empty();
         }
      }, Style.EMPTY);
      return segments;
   }
   public void appendPrefixSegments(List<ObsidianComponent_432> segments, String text, int baseColor) {
      int currentColor = baseColor;
      StringBuilder chunk = new StringBuilder();
      int chunkColor = baseColor;
      int offset = 0;

      while (offset < text.length()) {
         int codePoint = text.codePointAt(offset);
         int charCount = Character.charCount(codePoint);
         if (codePoint == 167 && offset + charCount < text.length()) {
            this.flushPrefixSegment(segments, chunk, chunkColor);
            char code = Character.toLowerCase(text.charAt(offset + charCount));
            Integer mappedColor = this.sectionColorToRgb(code);
            currentColor = mappedColor != null ? mappedColor : (code == 114 ? baseColor : currentColor);
            chunkColor = currentColor;
            offset += charCount + (1);
         } else {
            String replacement = ObsidianComponent_404.replaceCodePoint(codePoint);
            if (replacement == null) {
               if (chunk.length() > 0 && chunkColor != currentColor) {
                  this.flushPrefixSegment(segments, chunk, chunkColor);
               }

               chunkColor = currentColor;
               chunk.appendCodePoint(codePoint);
               offset += charCount;
            } else {
               this.flushPrefixSegment(segments, chunk, chunkColor);
               int totalChars = Math.max(1, replacement.length());

               for (int i = 0; i < replacement.length(); i++) {
                  int replacementColor = ObsidianComponent_404.getGradientColorForReplacement(
                     codePoint, i, totalChars, 1.0f, currentColor
                  );
                  if (chunk.length() > 0 && chunkColor != replacementColor) {
                     this.flushPrefixSegment(segments, chunk, chunkColor);
                  }

                  chunkColor = replacementColor;
                  chunk.append(replacement.charAt(i));
               }

               offset += charCount;
            }
         }
      }

      this.flushPrefixSegment(segments, chunk, chunkColor);
   }
   public void flushPrefixSegment(List<ObsidianComponent_432> segments, StringBuilder chunk, int color) {
      if (!chunk.isEmpty()) {
         String text = chunk.toString();
         ObsidianComponent_432 seg = new ObsidianComponent_432(text, color);
         seg.width12 = this.font12.getWidth(text);
         seg.width14 = this.font14.getWidth(text);
         segments.add(seg);
         chunk.setLength(0);
      }
   }
   public Integer sectionColorToRgb(char code) {
      return switch (code) {
         case '0' -> 0;
         case '1' -> 170;
         case '2' -> 43520;
         case '3' -> 43690;
         case '4' -> 11141120;
         case '5' -> 11141290;
         case '6' -> 16755200;
         case '7' -> 11184810;
         case '8' -> 5592405;
         case '9' -> 5592575;
         default -> null;
         case 'a' -> 5635925;
         case 'b' -> 5636095;
         case 'c' -> 16733525;
         case 'd' -> 16733695;
         case 'e' -> 16777045;
         case 'f' -> 16777215;
      };
   }
   public void updateStaffCache() {
      this.activeStaff.clear();
      String selfName = this.mc.player.getName().getString();

      for (Team team : this.mc.world.getScoreboard().getTeams()) {
         Collection<String> players = team.getPlayerList();
         if (players.size() == (1)) {
            String name = players.iterator().next();
            if (this.namePattern.matcher(name).matches() && !name.equals(selfName)) {
               PlayerListEntry info = this.mc.getNetworkHandler().getPlayerListEntry(name);
               boolean vanish = (info == null);
               boolean isGM3 = (info != null && info.getGameMode() == GameMode.SPECTATOR);
               Text prefixText = team.getPrefix();
               String prefixStr = prefixText.getString();
               boolean matchesPrefix = this.matchesStaffPrefix(prefixStr);
               boolean isInStaffList = Obsidian.INSTANCE.staffStorage.isStaff(name);
               if (matchesPrefix || vanish || isGM3 || isInStaffList) {
                  this.activeStaff.add(name);
                  String status;
                  if (vanish) {
                     status = "VANISH";
                  } else if (isGM3) {
                     status = "GM3";
                  } else {
                     status = "ONLINE";
                  }

                  ObsidianComponent_99 existing = this.staffDataCache.get(name);
                  if (existing == null) {
                     existing = new ObsidianComponent_99(status);
                     this.staffDataCache.put(name, existing);
                  }

                  existing.status = status;
                  existing.segments = this.parsePrefix(prefixText);
                  this.calculateWidths(existing, name);
               }
            }
         }
      }

      for (String staffName : Obsidian.INSTANCE.staffStorage.getStaffs()) {
         if (!staffName.equals(selfName) && this.namePattern.matcher(staffName).matches() && !this.activeStaff.contains(staffName)) {
            this.activeStaff.add(staffName);
            PlayerListEntry info = this.mc.getNetworkHandler().getPlayerListEntry(staffName);
            String statusx;
            if (info == null) {
               statusx = "VANISH";
            } else if (info.getGameMode() == GameMode.SPECTATOR) {
               statusx = "GM3";
            } else {
               statusx = "ONLINE";
            }

            ObsidianComponent_99 existing = this.staffDataCache.get(staffName);
            if (existing == null) {
               existing = new ObsidianComponent_99(statusx);
               existing.segments = new ArrayList<>();
               this.calculateWidths(existing, staffName);
               this.staffDataCache.put(staffName, existing);
            } else {
               existing.status = statusx;
            }
         }
      }
   }
   public void calculateWidths(ObsidianComponent_99 data, String name) {
      data.prefixWidth12 = 0.0f;
      data.prefixWidth14 = 0.0f;

      for (ObsidianComponent_432 seg : data.segments) {
         data.prefixWidth12 = data.prefixWidth12 + seg.width12;
         data.prefixWidth14 = data.prefixWidth14 + seg.width14;
      }

      data.nameWidth12 = this.font12.getWidth(name);
      data.nameWidth14 = this.font14.getWidth(name + " >> ");
   }
   public void updateAnimations() {
      float lerpSpeed = 0.10000000149011612f;
      this.animationScratch.clear();
      this.animationScratch.addAll(this.staffAnimations.keySet());
      this.animationScratch.addAll(this.activeStaff);

      for (String playerName : this.animationScratch) {
         boolean isActive = this.activeStaff.contains(playerName);
         float targetAnim = isActive ? 1.0f : 0.0f;
         float currentAnim = this.staffAnimations.getOrDefault(playerName, 0.0f);
         currentAnim += (targetAnim - currentAnim) * lerpSpeed;
         this.staffAnimations.put(playerName, currentAnim);
      }

      Iterator<Entry<String, Float>> animIt = this.staffAnimations.entrySet().iterator();

      while (animIt.hasNext()) {
         Entry<String, Float> entry = animIt.next();
         if (entry.getValue() < 0.009999999776482582f && !this.activeStaff.contains(entry.getKey())) {
            animIt.remove();
            this.staffDataCache.remove(entry.getKey());
         }
      }
   }
   public List<String> getVisiblePlayers() {
      this.visiblePlayers.clear();

      for (Entry<String, Float> entry : this.staffAnimations.entrySet()) {
         if (entry.getValue() > 0.009999999776482582f) {
            this.visiblePlayers.add(entry.getKey());
         }
      }

      Collections.sort(this.visiblePlayers);
      return this.visiblePlayers;
   }
   public int getStatusColor(String status) {
      int var3 = -1;
      switch (status.hashCode()) {
         case -1770434949:
            if (status.equals("VANISH")) {
               var3 = 0;
            }
            break;
         case 70669:
            if (status.equals("GM3")) {
               var3 = 1;
            }
      }
      return switch (var3) {
         case 0 -> -47526;
         case 1 -> -9146;
         default -> -10158216;
      };
   }
   public float getStatusBoxWidth(String status) {
      return 12.0f;
   }
   public void renderDefaultStyle(EventRender2D eventRender) {
      float x = this.draggable.getX();
      float y = this.draggable.getY();
      MatrixStack matrices = eventRender.getContext().getMatrices();
      int colorTheme;
      if (!Obsidian.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
         colorTheme = Obsidian.INSTANCE.themeStorage.getThemes().getTheme().color[0];
      } else {
         colorTheme = ObsidianComponent_olorUtil.getThemeColor();
      }

      List<String> visiblePlayers = this.getVisiblePlayers();
      float maxWidth = 60.0f;
      float headerHeight = 16.0f;
      float itemHeight = 12.0f;
      float padding = 5.0f;
      float statusPadding = 4.0f;

      for (String playerName : visiblePlayers) {
         ObsidianComponent_99 data = this.staffDataCache.get(playerName);
         if (data != null) {
            float statusBoxW = this.getStatusBoxWidth(data.status);
            float totalW = padding + data.prefixWidth12 + data.nameWidth12 + statusPadding + statusBoxW + padding;
            if (totalW > maxWidth) {
               maxWidth = totalW;
            }
         }
      }

      this.widthAnimation.update(maxWidth);
      float width = this.widthAnimation.getValue();
      float contentHeight = 0.0f;

      for (String playerNamex : visiblePlayers) {
         contentHeight += itemHeight * this.staffAnimations.getOrDefault(playerNamex, 0.0f);
      }

      float targetHeight = visiblePlayers.isEmpty() ? headerHeight : headerHeight + contentHeight + 2.0f;
      this.staffAnimatedHeight = this.staffAnimatedHeight + (targetHeight - this.staffAnimatedHeight) * 0.11999999731779099f;
      float height = this.staffAnimatedHeight;
      RenderUtil.drawDefaultHudElementRects(matrices, x, y, width, height, colorTheme, this.isUnusualRectType());
      this.font14
         .draw(
            matrices,
            "Staff",
            x + 5.0f,
            y + 6.0f,
            -1
         );
      this.iconFont
         .draw(
            matrices,
            "y",
            x + width - 13.0f,
            y + 7.5f,
            colorTheme
         );
      float offsetY = 18.0f;
      ObsidianComponent_343.push();
      ObsidianComponent_343.setFromComponentCoordinates((double)x, (double)y, (double)width, (double)height);

      for (String playerNamex : visiblePlayers) {
         float anim = this.staffAnimations.getOrDefault(playerNamex, 0.0f);
         if (!(anim <= 0.009999999776482582f)) {
            ObsidianComponent_99 data = this.staffDataCache.get(playerNamex);
            if (data != null) {
               int alpha = (int)(255.0f * anim);
               float yOffset = -5.0f * (1.0f - anim);
               float currentX = x + padding;

               for (int i = 0; i < data.segments.size(); i++) {
                  ObsidianComponent_432 seg = data.segments.get(i);
                  int color = ObsidianComponent_olorUtil.setAlphaColor(seg.color, alpha);
                  this.font12.draw(matrices, seg.text, currentX, y + offsetY + 2.0f + yOffset, color);
                  currentX += seg.width12;
               }

               this.font12
                  .draw(
                     matrices,
                     playerNamex,
                     currentX,
                     y + offsetY + 2.0f + yOffset,
                     ObsidianComponent_olorUtil.rgba(
                        255,
                        255,
                        255,
                        alpha
                     )
                  );
               float statusBoxWidth = this.getStatusBoxWidth(data.status);
               float statusBoxX = x + width - statusBoxWidth - padding;
               float statusBoxY = y + offsetY + 1.0f + yOffset;
               int statusRectColor = ObsidianComponent_olorUtil.setAlphaColor(this.getStatusColor(data.status), alpha);
               RenderUtil.drawRoundedRect(
                  matrices,
                  statusBoxX + 4.0f,
                  statusBoxY + 1.5f,
                  statusBoxWidth - 4.5f,
                  3.450000047683716f,
                  0.550000011920929f,
                  statusRectColor
               );
               offsetY += itemHeight * anim;
            }
         }
      }

      ObsidianComponent_343.pop();
      ObsidianComponent_343.unset();
      this.draggable.setWidth(width);
      this.draggable.setHeight(height);
   }
   public void renderWaveStyle(EventRender2D eventRender) {
      float x = this.draggable.getX();
      float y = this.draggable.getY();
      MatrixStack matrices = eventRender.getContext().getMatrices();
      int time = (int)(
         (float)(System.currentTimeMillis() % 2000L)
            / 2000.0f
            * 360.0f
      );
      int leftTop = ObsidianComponent_olorUtil.getThemeColor(time);
      int leftBottom = ObsidianComponent_olorUtil.getThemeColor(time + 30);
      int centerTop = ObsidianComponent_olorUtil.getThemeColor(time + 90);
      int centerBottom = ObsidianComponent_olorUtil.getThemeColor(time + 120);
      int rightTop = ObsidianComponent_olorUtil.getThemeColor(time + 180);
      int rightBottom = ObsidianComponent_olorUtil.getThemeColor(time + 210);
      List<String> visiblePlayers = this.getVisiblePlayers();
      float maxWidth = 80.0f;
      float headerHeight = 18.0f;
      float itemHeight = 10.0f;
      float padding = 5.0f;

      for (String playerName : visiblePlayers) {
         ObsidianComponent_99 data = this.staffDataCache.get(playerName);
         if (data != null) {
            float statusW = this.font12.getWidth(data.status);
            float totalW = padding + data.prefixWidth14 + data.nameWidth14 + statusW + padding;
            if (totalW > maxWidth) {
               maxWidth = totalW;
            }
         }
      }

      float contentHeight = 0.0f;

      for (String playerNamex : visiblePlayers) {
         contentHeight += itemHeight * this.staffAnimations.getOrDefault(playerNamex, 0.0f);
      }

      float height = visiblePlayers.isEmpty() ? headerHeight : headerHeight + contentHeight + 4.0f;
      if (visiblePlayers.isEmpty()) {
         RenderUtil.drawWaveHudHeader(
            matrices,
            x,
            y,
            maxWidth,
            15.0f,
            0.0f,
            10.0f,
            10.0f,
            leftTop,
            leftBottom,
            centerTop,
            centerBottom,
            rightTop,
            rightBottom
         );
         float titleX = x + (maxWidth - this.font16.getWidth("stafflist")) / 2.0f;
         this.font16
            .drawStringWithShadow(
               matrices, "stafflist", titleX, y + 5.0f, -1
            );
         this.draggable.setWidth(maxWidth);
         this.draggable.setHeight(headerHeight);
      } else {
         RenderUtil.drawWaveHudPanel(
            matrices,
            x,
            y,
            maxWidth,
            height,
            ObsidianComponent_olorUtil.rgba(25, 25, 25, 150),
            15.0f,
            0.0f,
            10.0f,
            10.0f,
            leftTop,
            leftBottom,
            centerTop,
            centerBottom,
            rightTop,
            rightBottom
         );
         float titleX = x + (maxWidth - this.font16.getWidth("stafflist")) / 1.899999976158142f;
         this.font16.drawStringWithShadow(matrices, "stafflist", titleX, y + 5.0f, -1);
         float yOffsetPos = 20.0f;
         ObsidianComponent_343.push();
         ObsidianComponent_343.setFromComponentCoordinates((double)x, (double)y, (double)maxWidth, (double)height);

         for (String playerNamex : visiblePlayers) {
            float anim = this.staffAnimations.getOrDefault(playerNamex, 0.0f);
            if (!(anim <= 0.009999999776482582f)) {
               ObsidianComponent_99 data = this.staffDataCache.get(playerNamex);
               if (data != null) {
                  int alpha = (int)(255.0F * anim);
                  float yOffset = -5.0f * (1.0f - anim);
                  float textX = x + padding;

                  for (int i = 0; i < data.segments.size(); i++) {
                     ObsidianComponent_432 seg = data.segments.get(i);
                     int color = ObsidianComponent_olorUtil.setAlphaColor(seg.color, alpha);
                     this.font14
                        .draw(matrices, seg.text, textX, y + yOffsetPos + 1.5f + yOffset, color);
                     textX += seg.width14;
                  }

                  this.font14
                     .draw(
                        matrices,
                        playerNamex + " >> ",
                        textX,
                        y + yOffsetPos + 1.5f + yOffset,
                        ObsidianComponent_olorUtil.rgba(
                           255,
                           255,
                           255,
                           alpha
                        )
                     );
                  float nameArrowWidth = this.font14.getWidth(playerNamex + " >> ");
                  this.font12
                     .draw(
                        matrices,
                        data.status,
                        textX + nameArrowWidth,
                        y + yOffsetPos + 2.5f + yOffset,
                        ObsidianComponent_olorUtil.setAlphaColor(this.getStatusColor(data.status), alpha)
                     );
                  yOffsetPos += itemHeight * anim;
               }
            }
         }

         ObsidianComponent_343.pop();
         ObsidianComponent_343.unset();
         this.draggable.setWidth(maxWidth);
         this.draggable.setHeight(height);
      }
   }
}
