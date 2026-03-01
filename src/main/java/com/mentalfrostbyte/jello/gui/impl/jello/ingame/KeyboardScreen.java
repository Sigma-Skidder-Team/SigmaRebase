package com.mentalfrostbyte.jello.gui.impl.jello.ingame;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.elements.impl.Keyboard;
import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.util.system.math.smoothing.EasingFunctions;
import com.mentalfrostbyte.jello.gui.base.elements.impl.critical.Screen;
import com.mentalfrostbyte.jello.gui.impl.jello.ingame.buttons.keybind.*;
import com.mentalfrostbyte.jello.gui.impl.jello.ingame.panels.ModsPanel;
import com.mentalfrostbyte.jello.managers.GuiManager;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil2;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import org.newdawn.slick.opengl.Texture;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;

public class KeyboardScreen extends Screen {
   private static final Minecraft mc = Minecraft.getInstance();
   private Texture texture;
   public Date date;
   public PopOver popOver;
   public Keyboard keyboard;
   public boolean field20958 = false;
   public boolean field20959;
   public ModsPanel modsPanel;
   public int keyCode;
   public boolean field20962;
   private boolean field20963;
   private boolean field20964;

   public KeyboardScreen() {
      super("KeybindManager");
      this.date = new Date();
      this.addToList(this.keyboard = new Keyboard(this, "keyboard", (this.widthA - 1060) / 2, (this.heightA - 357) / 2));
      this.keyboard.method13279(0.4F, 0.4F);
      this.keyboard
         .onPress(
            var2 -> {
               boolean var5 = false;

               for (CustomGuiScreen var7 : this.getChildren()) {
				   if (var7 instanceof PopOver) {
					   var5 = true;
					   break;
				   }
               }

               if (this.keyboard.keyCode == this.keyCode && var5) {
                  this.method13333();
               } else {
                  int[] var8 = this.keyboard.method13105(this.keyboard.keyCode);
                  String var9 = RenderUtil.getKeyName(this.keyboard.keyCode);
                  this.popOver = new PopOver(
                     this, "popover", this.keyboard.getXA() + var8[0], this.keyboard.getYA() + var8[1], this.keyboard.keyCode, var9
                  );
                  this.popOver.onPress(var1x -> this.method13329(this.keyboard));
                  this.popOver.method13713(var1x -> {
                     var1x.setReAddChildren(false);
                     this.method13331();
                  });
                  this.keyCode = this.keyboard.keyCode;
               }
            }
         );
      RenderUtil2.blur();
   }

   public static ArrayList<Class6984> method13328() {
      ArrayList var2 = new ArrayList();

      for (Module var4 : Client.getInstance().moduleManager.getModuleMap().values()) {
         var2.add(new Class6984(var4));
      }

      for (Entry var6 : GuiManager.screenToScreenName.entrySet()) {
         var2.add(new Class6984((Class<? extends net.minecraft.client.gui.screen.Screen>)var6.getKey()));
      }

      return var2;
   }

   private void method13329(Keyboard var1) {
      this.runThisOnDimensionUpdate(new Class635(this, var1));
   }

   private void method13330() {
      this.runThisOnDimensionUpdate(new Class544(this, this));
   }

   private void method13331() {
      this.runThisOnDimensionUpdate(new Class1533(this, this));
   }

   private void method13332() {
      this.runThisOnDimensionUpdate(new Class543(this, this));
   }

   private void method13333() {
      this.runThisOnDimensionUpdate(new Class1376(this, this));
   }

   @Override
   public void updatePanelDimensions(int newHeight, int newWidth) {
      if (this.method13212()) {
         this.keyboard.method13242();
         this.clearChildren();
         this.keyCode = 0;
         this.popOver = null;
      }

      if (this.popOver != null) {
         this.keyboard.method13242();
         this.clearChildren();
         this.addToList(this.popOver);
         this.popOver = null;
      }

      super.updatePanelDimensions(newHeight, newWidth);
      this.setListening(false);
   }

   @Override
   public int getFPS() {
      return 60;
   }

   @Override
   public void keyPressed(int keyCode) {
      super.keyPressed(keyCode);
      if (keyCode == 256) {
         RenderUtil2.resetShaders();
         mc.displayGuiScreen(null);
      }
   }

   @Override
   public void draw(float partialTicks) {
      partialTicks = (float)Math.min(200L, new Date().getTime() - this.date.getTime()) / 200.0F;
      float var4 = EasingFunctions.easeOutBack(partialTicks, 0.0F, 1.0F, 1.0F);
      this.method13279(0.8F + var4 * 0.2F, 0.8F + var4 * 0.2F);
      float var5 = 0.25F * partialTicks;
      RenderUtil.drawRoundedRect(
         (float)this.xA,
         (float)this.yA,
         (float)(this.xA + this.widthA),
         (float)(this.yA + this.heightA),
              RenderUtil2.applyAlpha(ClientColors.DEEP_TEAL.getColor(), var5)
      );
      super.method13224();
      RenderUtil.drawString(
         ResourceRegistry.JelloMediumFont40,
         (float)((this.widthA - 1060) / 2),
         (float)((this.heightA - 357) / 2 - 90),
         "Keybind Manager",
         ClientColors.LIGHT_GREYISH_BLUE.getColor()
      );
      super.draw(partialTicks);
   }

   // $VF: synthetic method
   public static int method13337(KeyboardScreen var0) {
      return var0.widthA;
   }

   // $VF: synthetic method
   public static int method13338(KeyboardScreen var0) {
      return var0.heightA;
   }

   // $VF: synthetic method
   public static void method13339(KeyboardScreen var0) {
      var0.method13332();
   }
}
