package wtf.obsidian.utils;

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
























import net.minecraft.text.Text;

public class ScoreboardEntryData {
   public final Text name;
   public final Text score;
   public final int scoreWidth;

   public ScoreboardEntryData(Text name, Text score, int scoreWidth) {
      this.name = name;
      this.score = score;
      this.scoreWidth = scoreWidth;
   }

   public Text name() {
      return this.name;
   }

   public Text score() {
      return this.score;
   }

   public int scoreWidth() {
      return this.scoreWidth;
   }
}
