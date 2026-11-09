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
























import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.util.Formatting;

public class MacroManager implements IInstance {
   private final List<ObsidianComponent_203> macros = new ArrayList<>();
   private final List<String> names = new ArrayList<>();

   public MacroManager() {
      EventManager.register(this);
   }

   public void add(ObsidianComponent_203 macro) {
      if (macro != null && macro.getName() != null && !macro.getName().isBlank() && this.getMacro(macro.getName()) == null) {
         this.macros.add(macro);
         this.names.add(macro.getName());
      }
   }

   public void remove(ObsidianComponent_203 macro) {
      if (macro != null) {
         this.macros.remove(macro);
         this.names.remove(macro.getName());
      }
   }

   public void clear() {
      if (!this.macros.isEmpty()) {
         this.macros.clear();
      }

      if (!this.names.isEmpty()) {
         this.names.clear();
      }
   }

   public boolean isEmpty() {
      return this.macros.isEmpty();
   }

   public ObsidianComponent_203 getMacro(String name) {
      for (ObsidianComponent_203 macro : this.macros) {
         if (macro.getName().equalsIgnoreCase(name)) {
            return macro;
         }
      }

      return null;
   }

   @EventTarget
   public void onKey(InputEvent e) {
      if (mc.player != null && mc.world != null && mc.currentScreen == null && mc.player.networkHandler != null && !this.macros.isEmpty()) {
         for (ObsidianComponent_203 macro : this.macros) {
            if (macro != null && macro.getBind() != null && macro.getBind().getKey() == e.getKey()) {
               this.executeMacro(macro);
            }
         }
      }
   }
   public void executeMacro(ObsidianComponent_203 macro) {
      String command = macro.getCommand();
      if (command != null && !command.isBlank()) {
         if (command.startsWith("/")) {
            mc.player.networkHandler.sendChatCommand(command.substring(1));
         } else {
            String prefix = Obsidian.INSTANCE.commandStorage.getPrefix();
            if (prefix != null && !prefix.isEmpty() && command.startsWith(prefix)) {
               try {
                  Obsidian.INSTANCE.commandStorage.getDispatcher().execute(command.substring(prefix.length()), Obsidian.INSTANCE.commandStorage.getSource());
               } catch (CommandSyntaxException var5) {
                  ObsidianComponent_hatUtil.sendMessage(Formatting.RED + "Ошибка в использовании макроса " + macro.getName() + "!");
               }
            } else {
               mc.player.networkHandler.sendChatMessage(command);
            }
         }
      }
   }

   @Generated
   public List<ObsidianComponent_203> getMacros() {
      return this.macros;
   }

   @Generated
   public List<String> getNames() {
      return this.names;
   }
}
