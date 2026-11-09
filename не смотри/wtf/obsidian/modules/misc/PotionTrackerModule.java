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

























import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.Box;

public class PotionTrackerModule extends Module {
   public static PotionTrackerModule INSTANCE = new PotionTrackerModule();
   public static final double TRACK_RADIUS = 50.0;
   public static final double SPLASH_RADIUS = 4.0;
   public static final double SPLASH_HEIGHT = 2.0;
   public static final int MAX_MESSAGES = 4;
   private static final int GRAY = new Color(
         200, 200, 200
      )
      .getRGB();
   private static final int PLAYER = new Color(
         235, 235, 235
      )
      .getRGB();
   private final Map<Integer, ObsidianComponent_170> trackedPotions = new HashMap<>();
   public ClientWorld lastWorld;

   public PotionTrackerModule() {
      super("PotionTracker", "Показывает попадание выкинутых зелий по игрокам", Category.MISC);
   }

   @Override
   public void onDisable() {
      this.trackedPotions.clear();
      this.lastWorld = null;
      super.onDisable();
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.player != null && mc.world != null) {
         if (this.lastWorld != mc.world) {
            this.trackedPotions.clear();
            this.lastWorld = mc.world;
         }

         Set<Integer> currentPotions = new HashSet<>();
         double trackRadiusSq = 2500.0;
         Box searchBox = mc.player.getBoundingBox().expand(50.0);

         for (PotionEntity potionEntity : mc.world.getEntitiesByClass(PotionEntity.class, searchBox, Entity::isAlive)) {
            if (!(mc.player.squaredDistanceTo(potionEntity) > trackRadiusSq)) {
               ObsidianComponent_208 potionInfo = this.getPotionInfo(potionEntity);
               if (potionInfo != null) {
                  int entityId = potionEntity.getId();
                  currentPotions.add(entityId);
                  ObsidianComponent_170 data = this.trackedPotions.get(entityId);
                  if (data == null) {
                     this.trackedPotions.put(entityId, new ObsidianComponent_170(potionInfo, potionEntity.getX(), potionEntity.getY(), potionEntity.getZ()));
                  } else {
                     data.lastX = potionEntity.getX();
                     data.lastY = potionEntity.getY();
                     data.lastZ = potionEntity.getZ();
                     data.potionInfo = potionInfo;
                  }
               }
            }
         }

         Set<Integer> removedPotions = new HashSet<>(this.trackedPotions.keySet());
         removedPotions.removeAll(currentPotions);

         for (int entityId : removedPotions) {
            ObsidianComponent_170 data = this.trackedPotions.remove(entityId);
            if (data != null) {
               this.printSplash(data);
            }
         }
      } else {
         this.trackedPotions.clear();
         this.lastWorld = null;
      }
   }
   public void printSplash(ObsidianComponent_170 data) {
      Box potionBox = new Box(
         data.lastX - 4.0,
         data.lastY - 2.0,
         data.lastZ - 4.0,
         data.lastX + 4.0,
         data.lastY + 2.0,
         data.lastZ + 4.0
      );
      List<ObsidianComponent_86> hits = new ArrayList<>();

      for (PlayerEntity player : mc.world.getPlayers()) {
         if (player != null && player.isAlive() && potionBox.contains(player.getPos())) {
            double dx = player.getX() - data.lastX;
            double dz = player.getZ() - data.lastZ;
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (!(distance > 4.0)) {
               double proximity = Math.max(
                  0.0,
                  1.0
                     - distance / 4.0
               );
               int percent = Math.max(
                  1,
                  Math.min(
                     100,
                     (int)Math.round(proximity * 100.0)
                  )
               );
               hits.add(new ObsidianComponent_86(player.getName().getString(), percent, distance));
            }
         }
      }

      hits.sort(Comparator.comparingDouble(ObsidianComponent_86::distance));

      for (int i = 0; i < Math.min(4, hits.size()); i++) {
         ObsidianComponent_86 hit = hits.get(i);
         this.sendPotionMessage(hit.playerName(), data.potionInfo, hit.percent());
      }
   }
   public ObsidianComponent_208 getPotionInfo(PotionEntity potionEntity) {
      PotionContentsComponent contents = (PotionContentsComponent)potionEntity.getStack().get(DataComponentTypes.POTION_CONTENTS);
      ObsidianComponent_208 byEffects = this.getPotionInfo(contents);
      return byEffects != null ? byEffects : this.getPotionInfo(potionEntity.getStack().getName().getString());
   }
   public ObsidianComponent_208 getPotionInfo(PotionContentsComponent contents) {
      if (contents != null && contents.hasEffects()) {
         boolean regenerationTwo = this.hasEffect(contents, StatusEffects.REGENERATION, 1);
         boolean strengthFive = this.hasEffect(contents, StatusEffects.STRENGTH, 4);
         boolean healthBoostThree = this.hasEffect(contents, StatusEffects.HEALTH_BOOST, 2);
         boolean strengthFour = this.hasEffect(contents, StatusEffects.STRENGTH, 3);
         boolean speedThree = this.hasEffect(contents, StatusEffects.SPEED, 2);
         if (regenerationTwo) {
            return ObsidianComponent_208.HOLY_WATER;
         } else if (strengthFive) {
            return ObsidianComponent_208.WRATH;
         } else if (healthBoostThree) {
            return ObsidianComponent_208.PALADIN;
         } else if (strengthFour && speedThree) {
            return ObsidianComponent_208.ASSASSIN;
         } else {
            return strengthFour ? ObsidianComponent_208.ASSASSIN : null;
         }
      } else {
         return null;
      }
   }
   public boolean hasEffect(PotionContentsComponent contents, RegistryEntry<StatusEffect> effect, int amplifier) {
      for (StatusEffectInstance instance : contents.getEffects()) {
         if (instance.getEffectType().equals(effect) && instance.getAmplifier() == amplifier) {
            return true;
         }
      }

      return false;
   }
   public ObsidianComponent_208 getPotionInfo(String itemName) {
      String normalizedName = this.normalize(itemName);
      ObsidianComponent_208[] var3 = ObsidianComponent_208.values();
      int var4 = var3.length;

      for (int var5 = 0; var5 < var4; var5++) {
         ObsidianComponent_208 potionInfo = var3[var5];
         if (normalizedName.contains(this.normalize(potionInfo.plainName()))) {
            return potionInfo;
         }
      }

      return null;
   }
   public String normalize(String text) {
      return text.replaceAll("§.", "").replace("[", "").replace("]", "").replace("✦", "").toLowerCase(Locale.ROOT).trim();
   }
   public void sendPotionMessage(String playerName, ObsidianComponent_208 potionInfo, int percent) {
      if (mc.player != null) {
         MutableText text = Text.literal("");
         text.append(
            this.gradientText(
               "obsidian",
               ObsidianComponent_olorUtil.getThemeColor(0),
               ObsidianComponent_olorUtil.getThemeColor(90),
               true
            )
         );
         text.append(Text.literal(" ⇒ ").setStyle(this.grayStyle()));
         text.append(Text.literal(playerName).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(PLAYER))));
         text.append(Text.literal(" получил ").setStyle(this.grayStyle()));
         text.append(this.gradientText(potionInfo.displayName, potionInfo.startColor, potionInfo.endColor, true));
         text.append(Text.literal(" " + percent + "%").setStyle(this.grayStyle()));
         mc.player.sendMessage(text, false);
      }
   }
   public MutableText gradientText(String text, int startColor, int endColor, boolean bold) {
      MutableText result = Text.literal("");

      for (int i = 0; i < text.length(); i++) {
         float progress = text.length() <= (1)
            ? 0.0f
            : (float)i / (float)(text.length() - (1));
         int color = ObsidianComponent_olorUtil.gradient(startColor, endColor, progress);
         result.append(Text.literal(String.valueOf(text.charAt(i))).setStyle(Style.EMPTY.withBold(bold).withColor(TextColor.fromRgb(color))));
      }

      return result;
   }
   public Style grayStyle() {
      return Style.EMPTY.withColor(TextColor.fromRgb(GRAY));
   }
}
