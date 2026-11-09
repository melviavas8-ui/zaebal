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
























import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Generated;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public class ObsidianComponent_280 implements IInstance {
   @Expose
   @SerializedName("x")
   public float xPos;
   @Expose
   @SerializedName("y")
   public float yPos;
   public float initialXVal;
   public float initialYVal;
   public float startX;
   public float startY;
   public boolean dragging;
   public float width;
   public float height;
   @Expose
   @SerializedName("name")
   public String name;
   public final Module module;
   public float targetXPos;
   public float targetYPos;
   public static final float CENTER_LINE_WIDTH = 1.0F;
   public static final float SNAP_THRESHOLD = 10.0F;
   public float lineAlpha = 0.0f;
   public long lastUpdateTime;
   public boolean snapToCenter;
   public boolean snapToCenterx;
   public boolean snapToCenter2x;
   public boolean snapToCenter3x;
   public boolean snapToCenter4x;
   public boolean snapToCenter5x;
   public boolean snapToCenter2;
   public boolean snapToCenter3;
   public boolean snapToCenter4;
   public boolean snapToCenter5;
   public static final float LERP_SPEED = 0.19F;
   public static final float MAX_TILT_DEGREES = 25.0F;
   public static final float TILT_FROM_MOUSE_DELTA = 4.0F;
   public static final float DRAG_TILT_LERP = 0.14F;
   public static final float RELEASE_TILT_LERP = 0.1F;
   public static final float TILT_DELTA_SMOOTHING = 0.18F;
   public static final float TILT_TARGET_SMOOTHING = 0.22F;
   public static final float TILT_DEADZONE = 0.18F;
   public static final float DRAG_SCALE_MULTIPLIER = 1.01F;
   public static final float DRAG_SCALE_LERP = 0.1F;
   public static final float RELEASE_SCALE_LERP = 0.02F;
   public float dragTiltDegrees;
   public float targetTiltDegrees;
   public float smoothedMouseDeltaX;
   public float lastDragMouseX;
   public boolean hasLastDragMouseX;
   public boolean tiltMatrixPushed;
   public float dragScale = 1.0f;
   public float targetScale = 1.0f;

   public ObsidianComponent_280(Module module, String name, float initialXVal, float initialYVal) {
      this.module = module;
      this.name = name;
      this.xPos = initialXVal;
      this.yPos = initialYVal;
      this.initialXVal = initialXVal;
      this.initialYVal = initialYVal;
   }

   public float getX() {
      return this.xPos;
   }

   public void setX(float x) {
      this.xPos = x;
   }

   public float getY() {
      return this.yPos;
   }

   public void setY(float y) {
      this.yPos = y;
   }
   public ObsidianComponent_90 getMouse(int mouseX, int mouseY) {
      MinecraftClient client = MinecraftClient.getInstance();
      Window window = client == null ? null : client.getWindow();
      double scaleFactor = window == null
         ? 1.0
         : window.getScaleFactor();
      return new ObsidianComponent_90(
         (int)((double)mouseX * scaleFactor / 2.0),
         (int)((double)mouseY * scaleFactor / 2.0)
      );
   }

   public final void onDraw(int mouseX, int mouseY, Window res, MatrixStack ms) {
      ObsidianComponent_90 fixed = this.getMouse(mouseX, mouseY);
      mouseX = fixed.getX();
      mouseY = fixed.getY();
      float centerX = (float)res.getScaledWidth() / 2.0f;
      float centerY = (float)res.getScaledHeight() / 2.0f;
      float centerX2 = (float)res.getScaledWidth() / 4.0f;
      float centerY2 = (float)res.getScaledHeight() / 4.0f;
      float centerX3 = (float)res.getScaledWidth() / 8.0f;
      float centerY3 = (float)res.getScaledHeight() / 8.0f;
      float centerX4 = (float)res.getScaledWidth() / 1.149999976158142f;
      float centerY4 = (float)res.getScaledHeight() / 1.149999976158142f;
      float centerX5 = (float)res.getScaledWidth() / 1.350000023841858f;
      float centerY5 = (float)res.getScaledHeight() / 1.350000023841858f;
      this.snapToCenter = this.snapToCenterx = this.snapToCenter2x = this.snapToCenter3x = this.snapToCenter4x = this.snapToCenter5x = this.snapToCenter2 = this.snapToCenter3 = this.snapToCenter4 = this.snapToCenter5 = false;
      if (this.dragging) {
         this.targetScale = 1.0099999904632568f;
         if (this.hasLastDragMouseX) {
            float mouseDeltaX = (float)mouseX - this.lastDragMouseX;
            if (Math.abs(mouseDeltaX) < 0.18000000715255737f) {
               mouseDeltaX = 0.0f;
            }

            this.smoothedMouseDeltaX = MathLerpUtil.lerp(this.smoothedMouseDeltaX, mouseDeltaX, 0.18000000715255737f);
            float desiredTilt = Math.max(
               -25.0f,
               Math.min(25.0f, this.smoothedMouseDeltaX * 4.0f)
            );
            this.targetTiltDegrees = MathLerpUtil.lerp(this.targetTiltDegrees, desiredTilt, 0.2199999988079071f);
         }

         this.lastDragMouseX = (float)mouseX;
         this.hasLastDragMouseX = true;
         this.targetXPos = (float)mouseX - this.startX;
         this.targetYPos = (float)mouseY - this.startY;
         boolean snapped = false;
         if (Math.abs(this.targetXPos + this.width / 2.0f - centerX)
            < 10.0f) {
            this.targetXPos = centerX - this.width / 2.0f;
            this.snapToCenterx = true;
            snapped = true;
         }

         if (Math.abs(this.targetYPos + this.height / 2.0f - centerY)
            < 10.0f) {
            this.targetYPos = centerY - this.height / 2.0f;
            this.snapToCenter = true;
            snapped = true;
         }

         if (Math.abs(this.targetXPos + this.width / 2.0f - centerX2)
            < 10.0f) {
            this.targetXPos = centerX2 - this.width / 2.0f;
            this.snapToCenter2x = true;
            snapped = true;
         }

         if (Math.abs(this.targetYPos + this.height / 2.0f - centerY2)
            < 10.0f) {
            this.targetYPos = centerY2 - this.height / 2.0f;
            this.snapToCenter2 = true;
            snapped = true;
         }

         if (Math.abs(this.targetXPos + this.width / 2.0f - centerX3)
            < 10.0f) {
            this.targetXPos = centerX3 - this.width / 2.0f;
            this.snapToCenter3x = true;
            snapped = true;
         }

         if (Math.abs(this.targetYPos + this.height / 2.0f - centerY3)
            < 10.0f) {
            this.targetYPos = centerY3 - this.height / 2.0f;
            this.snapToCenter3 = true;
            snapped = true;
         }

         if (Math.abs(this.targetXPos + this.width / 2.0f - centerX4)
            < 10.0f) {
            this.targetXPos = centerX4 - this.width / 2.0f;
            this.snapToCenter4x = true;
            snapped = true;
         }

         if (Math.abs(this.targetYPos + this.height / 2.0f - centerY4)
            < 10.0f) {
            this.targetYPos = centerY4 - this.height / 2.0f;
            this.snapToCenter4 = true;
            snapped = true;
         }

         if (Math.abs(this.targetXPos + this.width / 2.0f - centerX5)
            < 10.0f) {
            this.targetXPos = centerX5 - this.width / 2.0f;
            this.snapToCenter5x = true;
            snapped = true;
         }

         if (Math.abs(this.targetYPos + this.height / 2.0f - centerY5)
            < 10.0f) {
            this.targetYPos = centerY5 - this.height / 2.0f;
            this.snapToCenter5 = true;
            snapped = true;
         }

         if (this.targetXPos + this.width > (float)res.getScaledWidth()) {
            this.targetXPos = (float)res.getScaledWidth() - this.width;
         }

         if (this.targetYPos + this.height > (float)res.getScaledHeight()) {
            this.targetYPos = (float)res.getScaledHeight() - this.height;
         }

         if (this.targetXPos < 0.0F) {
            this.targetXPos = 0.0f;
         }

         if (this.targetYPos < 0.0f) {
            this.targetYPos = 0.0f;
         }

         this.xPos = MathLerpUtil.lerp(this.xPos, this.targetXPos, 0.1899999976158142f);
         this.yPos = MathLerpUtil.lerp(this.yPos, this.targetYPos, 0.1899999976158142f);
         this.updateLineAlpha(snapped);
      } else {
         this.targetScale = 1.0f;
         this.targetTiltDegrees = 0.0f;
         this.smoothedMouseDeltaX = MathLerpUtil.lerp(
            this.smoothedMouseDeltaX, 0.0f, 0.18000000715255737f
         );
         this.hasLastDragMouseX = false;
         this.updateLineAlpha(false);
      }

      this.updateTilt();
      this.drawCenterLines(ms, res);
   }
   public void updateTilt() {
      float lerp = this.dragging ? 0.14000000059604645f : 0.10000000149011612f;
      this.dragTiltDegrees = MathLerpUtil.lerp(this.dragTiltDegrees, this.targetTiltDegrees, lerp);
      if (!this.dragging && Math.abs(this.dragTiltDegrees) < 0.019999999552965164f) {
         this.dragTiltDegrees = 0.0f;
      }

      float scaleLerp = this.dragging ? 0.10000000149011612f : 0.019999999552965164f;
      this.dragScale = MathLerpUtil.lerp(this.dragScale, this.targetScale, scaleLerp);
      if (!this.dragging
         && Math.abs(this.dragScale - 1.0f)
            < 0.0020000000949949026f) {
         this.dragScale = 1.0f;
      }
   }

   public void beginRenderTilt(MatrixStack ms) {
      this.updateTilt();
      this.tiltMatrixPushed = false;
      if (!(Math.abs(this.dragTiltDegrees) < 0.05000000074505806f)
         || !(Math.abs(this.dragScale - 1.0f) < 0.0020000000949949026f)) {
         float centerX = this.xPos + this.width / 2.0f;
         float centerY = this.yPos + this.height / 2.0f;
         ms.push();
         ms.translate(centerX, centerY, 0.0f);
         ms.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(this.dragTiltDegrees));
         ms.scale(this.dragScale, this.dragScale, 1.0f);
         ms.translate(-centerX, -centerY, 0.0f);
         this.tiltMatrixPushed = true;
      }
   }

   public void endRenderTilt(MatrixStack ms) {
      if (this.tiltMatrixPushed) {
         ms.pop();
         this.tiltMatrixPushed = false;
      }
   }
   public void updateLineAlpha(boolean active) {
      long currentTime = System.currentTimeMillis();
      float deltaTime = (float)(currentTime - this.lastUpdateTime) / 1000.0f;
      this.lastUpdateTime = currentTime;
      float fadeSpeed = 2.0f;
      float fadeOutSpeed = 2.0f;
      if (active) {
         this.lineAlpha += deltaTime * fadeSpeed;
         if (this.lineAlpha > 1.0f) {
            this.lineAlpha = 1.0f;
         }
      } else {
         this.lineAlpha -= deltaTime * fadeOutSpeed;
         if (this.lineAlpha < 0.0f) {
            this.lineAlpha = 0.0f;
         }
      }
   }
   public void drawCenterLines(MatrixStack ms, Window res) {
      if (this.lineAlpha > 0.0f) {
         float centerX = (float)res.getScaledWidth() / 2.0f;
         float centerY = (float)res.getScaledHeight() / 2.0f;
         float centerX2 = (float)res.getScaledWidth() / 4.0f;
         float centerY2 = (float)res.getScaledHeight() / 4.0f;
         float centerX3 = (float)res.getScaledWidth() / 8.0f;
         float centerY3 = (float)res.getScaledHeight() / 8.0f;
         float centerX4 = (float)res.getScaledWidth() / 1.149999976158142f;
         float centerY4 = (float)res.getScaledHeight() / 1.149999976158142f;
         float centerX5 = (float)res.getScaledWidth() / 1.350000023841858f;
         float centerY5 = (float)res.getScaledHeight() / 1.350000023841858f;
         int color = (int)(this.lineAlpha * 255.0f) << (24)
            | 16777215;
         if (this.snapToCenterx) {
            RenderUtil.drawRoundedRect(
               ms,
               centerX - 0.3333333432674408f,
               0.0f,
               1.0f,
               (float)res.getScaledHeight(),
               1.0f,
               color
            );
         }

         if (this.snapToCenter) {
            RenderUtil.drawRoundedRect(
               ms,
               0.0f,
               centerY - 0.3333333432674408f,
               (float)res.getScaledWidth(),
               1.0f,
               1.0f,
               color
            );
         }

         if (this.snapToCenter2x) {
            RenderUtil.drawRoundedRect(
               ms,
               centerX2 - 0.3333333432674408f,
               0.0f,
               1.0f,
               (float)res.getScaledHeight(),
               1.0f,
               color
            );
         }

         if (this.snapToCenter2) {
            RenderUtil.drawRoundedRect(
               ms,
               0.0f,
               centerY2 - 0.3333333432674408f,
               (float)res.getScaledWidth(),
               1.0f,
               1.0f,
               color
            );
         }

         if (this.snapToCenter3x) {
            RenderUtil.drawRoundedRect(
               ms,
               centerX3 - 0.3333333432674408f,
               0.0f,
               1.0f,
               (float)res.getScaledHeight(),
               1.0f,
               color
            );
         }

         if (this.snapToCenter3) {
            RenderUtil.drawRoundedRect(
               ms,
               0.0f,
               centerY3 - 0.3333333432674408f,
               (float)res.getScaledWidth(),
               1.0f,
               1.0f,
               color
            );
         }

         if (this.snapToCenter4x) {
            RenderUtil.drawRoundedRect(
               ms,
               centerX4 - 0.3333333432674408f,
               0.0f,
               1.0f,
               (float)res.getScaledHeight(),
               1.0f,
               color
            );
         }

         if (this.snapToCenter4) {
            RenderUtil.drawRoundedRect(
               ms,
               0.0f,
               centerY4 - 0.3333333432674408f,
               (float)res.getScaledWidth(),
               1.0f,
               1.0f,
               color
            );
         }

         if (this.snapToCenter5x) {
            RenderUtil.drawRoundedRect(
               ms,
               centerX5 - 0.3333333432674408f,
               0.0f,
               1.0f,
               (float)res.getScaledHeight(),
               1.0f,
               color
            );
         }

         if (this.snapToCenter5) {
            RenderUtil.drawRoundedRect(
               ms,
               0.0f,
               centerY5 - 0.3333333432674408f,
               (float)res.getScaledWidth(),
               1.0f,
               1.0f,
               color
            );
         }
      }
   }

   public final boolean onClick(double mouseX, double mouseY, int button) {
      if (button == 0 && MathUtil.isInRegion(mouseX, mouseY, this.xPos, this.yPos, this.width, this.height)) {
         this.dragging = true;
         this.targetScale = 1.0099999904632568f;
         this.startX = (float)((int)(mouseX - (double)this.xPos));
         this.startY = (float)((int)(mouseY - (double)this.yPos));
         this.smoothedMouseDeltaX = 0.0f;
         this.hasLastDragMouseX = false;
         this.lastUpdateTime = System.currentTimeMillis();
         return true;
      } else {
         return false;
      }
   }

   public final void onRelease(int button) {
      if (button == 0) {
         this.dragging = false;
         this.targetScale = 1.0f;
         this.targetTiltDegrees = 0.0f;
         this.smoothedMouseDeltaX = 0.0f;
         this.hasLastDragMouseX = false;
      }
   }

   @Generated
   public void setWidth(float width) {
      this.width = width;
   }

   @Generated
   public float getWidth() {
      return this.width;
   }

   @Generated
   public void setHeight(float height) {
      this.height = height;
   }

   @Generated
   public float getHeight() {
      return this.height;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public Module getModule() {
      return this.module;
   }
}
