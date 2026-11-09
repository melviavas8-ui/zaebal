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
























import java.util.function.Supplier;
import lombok.Generated;

public class ObsidianComponent_341 extends Setting {
   public String text;
   public final int maxLength;

   public ObsidianComponent_341(String name, String text) {
      this(name, text, 32);
   }

   public ObsidianComponent_341(String name, String text, int maxLength) {
      super(name);
      this.maxLength = Math.max(1, maxLength);
      this.setText(text);
   }

   public void setText(String text) {
      if (text == null) {
         this.text = "";
      } else {
         StringBuilder builder = new StringBuilder();

         for (int i = 0; i < text.length() && builder.length() < this.maxLength; i++) {
            char chr = text.charAt(i);
            if (!Character.isISOControl(chr)) {
               builder.append(chr);
            }
         }

         this.text = builder.toString();
      }
   }

   public String get() {
      return this.text;
   }

   public ObsidianComponent_341 visible(Supplier<Boolean> state) {
      this.visible = state;
      return this;
   }

   @Generated
   public String getText() {
      return this.text;
   }

   @Generated
   public int getMaxLength() {
      return this.maxLength;
   }
}
