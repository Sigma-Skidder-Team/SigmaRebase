package com.mentalfrostbyte.jello.gui.unmapped;

import com.thizzer.jtouchbar.item.view.TouchBarButton;
import com.thizzer.jtouchbar.item.view.TouchBarView;
import com.thizzer.jtouchbar.item.view.action.TouchBarViewAction;

public class Class2544 implements TouchBarViewAction {
   private static String[] field16766;
   public final Bound field16767;
   public final MacOSTouchBar field16768;

   public Class2544(MacOSTouchBar var1, Bound var2) {
      this.field16768 = var1;
      this.field16767 = var2;
   }

   @Override
   public void onCall(TouchBarView var1) {
      this.field16767.getModuleTarget().toggle();
      ((TouchBarButton)var1).setBezelColor(this.field16768.method13740(this.field16767.getModuleTarget()));
   }
}
