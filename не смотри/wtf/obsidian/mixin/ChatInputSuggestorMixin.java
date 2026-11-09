package wtf.obsidian.mixin;

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























import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatInputSuggestor.SuggestionWindow;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin({ChatInputSuggestor.class})
public abstract class ChatInputSuggestorMixin {
   @Final
   @Shadow
   public TextFieldWidget textField;
   @Shadow
   public boolean completingSuggestions;
   @Shadow
   private ParseResults<CommandSource> parse;
   @Shadow
   private CompletableFuture<Suggestions> pendingSuggestions;
   @Shadow
   private SuggestionWindow window;

   @Shadow
   public abstract void show(boolean var1);

   @Inject(
      method = {"refresh"},
      at = {@At(
         value = "INVOKE",
         target = "Lcom/mojang/brigadier/StringReader;canRead()Z",
         remap = false
      )},
      cancellable = true
   )
   public void refresh(CallbackInfo ci, @Local StringReader reader) {
      String prefix = Obsidian.INSTANCE.commandStorage.getPrefix();
      if (reader.canRead(prefix.length()) && reader.getString().startsWith(prefix, reader.getCursor())) {
         reader.setCursor(reader.getCursor() + prefix.length());
         CommandDispatcher<CommandSource> dispatcher = Obsidian.INSTANCE.commandStorage.getDispatcher();
         if (this.parse == null) {
            this.parse = dispatcher.parse(reader, Obsidian.INSTANCE.commandStorage.getSource());
         }

         int cursor;
         if ((cursor = this.textField.getCursor()) >= 1 && (this.window == null || !this.completingSuggestions)) {
            this.pendingSuggestions = dispatcher.getCompletionSuggestions(this.parse, cursor);
            this.pendingSuggestions.thenRun(() -> {
               if (this.pendingSuggestions.isDone()) {
                  this.show(false);
               }
            });
         }

         ci.cancel();
      }
   }
}
