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























import java.util.Locale;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Identifier.class})
public abstract class IdentifierMixin {
   private static final String SAFE_JOIN_PATH = "invalid_join_id";

   @Inject(
      method = {"validatePath"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void obsidian$sanitizeJoinPath(String namespace, String path, CallbackInfoReturnable<String> cir) {
      if (shouldSanitizePath(path)) {
         cir.setReturnValue(sanitizePath(path));
      }
   }

   @Inject(
      method = {"validateNamespace"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void obsidian$sanitizeJoinNamespace(String namespace, String path, CallbackInfoReturnable<String> cir) {
      if (shouldSanitizeNamespace(namespace)) {
         cir.setReturnValue(sanitizeNamespace(namespace));
      }
   }

   private static boolean shouldSanitizeNamespace(String value) {
      if (value != null && !value.isEmpty()) {
         for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!isAllowedNamespaceChar(c)) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   private static boolean shouldSanitizePath(String value) {
      if (value != null && !value.isEmpty()) {
         for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!isAllowedPathChar(c)) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   private static boolean isAllowedNamespaceChar(char c) {
      return c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_' || c == '.' || c == '-';
   }

   private static boolean isAllowedPathChar(char c) {
      return isAllowedNamespaceChar(c) || c == '/';
   }

   private static String sanitizeNamespace(String namespace) {
      StringBuilder builder = new StringBuilder(namespace.length());
      String lower = namespace.toLowerCase(Locale.ROOT);

      for (int i = 0; i < lower.length(); i++) {
         char c = lower.charAt(i);
         if (isAllowedNamespaceChar(c)) {
            builder.append(c);
         } else {
            builder.append('_');
         }
      }

      String sanitized = builder.toString();
      return sanitized.isBlank() ? "minecraft" : sanitized;
   }

   private static String sanitizePath(String path) {
      StringBuilder builder = new StringBuilder(path.length());
      String lower = path.toLowerCase(Locale.ROOT);

      for (int i = 0; i < lower.length(); i++) {
         char c = lower.charAt(i);
         if (isAllowedPathChar(c)) {
            builder.append(c);
         } else {
            builder.append('_');
         }
      }

      String sanitized = builder.toString();
      return sanitized.isBlank() ? "invalid_join_id" : sanitized;
   }
}
