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
























import java.io.Serializable;

public class ObsidianComponent_394 implements Serializable {
   public final String url;
   public final String label;

   public static ObsidianComponent_394 create(String label, String url) {
      label = label.substring(0, Math.min(label.length(), 31));
      return new ObsidianComponent_394(label, url);
   }

   protected ObsidianComponent_394(String label, String url) {
      this.label = label;
      this.url = url;
   }

   public String getUrl() {
      return this.url;
   }

   public String getLabel() {
      return this.label;
   }
}
