package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.jello.managers.impl.account.microsoft.Account;

import java.util.Comparator;

public final class Class3573 implements Comparator<Account> {
   private static String[] field19512;

   public int compare(Account var1, Account var2) {
      return var1.getDateAdded() >= var2.getDateAdded() ? (var1.getDateAdded() <= var2.getDateAdded() ? 0 : -1) : 1;
   }
}
