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























import java.lang.reflect.InvocationTargetException;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;





@Mixin({Camera.class})
public abstract class CameraMixin {
   @Redirect(
      method = {"update"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"
      )
   )
   private void redirectSetRotation(
      Camera instance, float yaw, float pitch, BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta
   ) throws InvocationTargetException, IllegalAccessException, InstantiationException {
      Event_222 event = new Event_222(yaw, pitch, tickDelta);
      EventManager.invoke(event);
      float newYaw = event.getYaw();
      float newPitch = event.getPitch();
      if (thirdPerson && inverseView) {
         newYaw += 180.0F;
         newPitch = -newPitch;
      }

      ((ICameraMixin)instance).setCustomRotation(newYaw, newPitch);
   }

   @Redirect(
      method = {"update"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/Camera;clipToSpace(F)F"
      )
   )
   private float redirectClipToSpace(
      Camera instance, float distance, BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta
   ) {
      if (!thirdPerson) {
         return ((ICameraMixin)instance).setClipToSpace(distance);
      } else {
         CinematicCameraModule module = ModuleManager.INSTANCE != null ? ModuleManager.interpolateF5 : null;
         return module != null && module.isEnable()
            ? ((ICameraMixin)instance).setClipToSpace(module.getInterpolatedDistance(tickDelta))
            : ((ICameraMixin)instance).setClipToSpace(distance);
      }
   }

   @Redirect(
      method = {"update"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/Camera;moveBy(FFF)V"
      )
   )
   private void redirectMoveBy(
      Camera instance, float x, float y, float z, BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta
   ) {
      float newY = y;
      if (thirdPerson) {
         CinematicCameraModule module = ModuleManager.INSTANCE != null ? ModuleManager.interpolateF5 : null;
         if (module != null && module.isEnable()) {
            newY = y + module.getInterpolatedHeightOffset(tickDelta);
         }
      }

      ((ICameraMixin)instance).setCustomMoveBy(x, newY, z);
   }
}
