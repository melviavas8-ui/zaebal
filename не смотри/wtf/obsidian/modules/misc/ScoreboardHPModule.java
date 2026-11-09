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

























import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;

public class ScoreboardHPModule extends Module {
   public static final ScoreboardHPModule INSTANCE = new ScoreboardHPModule();

   public ScoreboardHPModule() {
      super("ScoreboardHP", "Обход показа HP для серверов", Category.MISC);
   }

   public static float getHealth(LivingEntity entity) {
      if (entity == null) {
         return 0.0f;
      } else if (!INSTANCE.isEnable()) {
         return entity.getHealth();
      } else if (entity instanceof ClientPlayerEntity) {
         return entity.getHealth();
      } else {
         if (entity instanceof PlayerEntity player && mc.getCurrentServerEntry() != null) {
            try {
               Scoreboard scoreboard = player.getScoreboard();
               ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME);
               if (objective == null) {
                  objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.LIST);
               }

               if (objective == null) {
                  return entity.getHealth();
               }

               ReadableScoreboardScore score = scoreboard.getScore(player, objective);
               if (score == null) {
                  return entity.getHealth();
               }

               return (float)score.getScore();
            } catch (Exception var5) {
               return entity.getHealth();
            }
         }

         return entity.getHealth();
      }
   }

   public static float getHealthWithAbsorption(LivingEntity entity) {
      return Math.max(0.0f, getHealth(entity) + entity.getAbsorptionAmount());
   }
}
