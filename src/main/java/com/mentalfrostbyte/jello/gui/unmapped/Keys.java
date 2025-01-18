package com.mentalfrostbyte.jello.gui.unmapped;

public enum Keys {
   GRAVE(0, 63, "`", 96, 1),
   ONE(73, 63, "1", 49, 1),
   TWO(145, 63, "2", 50, 1),
   THREE(218, 63, "3", 51, 1),
   FOUR(290, 63, "4", 52, 1),
   FIVE(363, 63, "5", 53, 1),
   SIX(436, 63, "6", 54, 1),
   SEVEN(508, 63, "7", 55, 1),
   EIGHT(580, 63, "8", 56, 1),
   NINE(653, 63, "9", 57, 1),
   ZERO(726, 63, "0", 48, 1),
   MINUS(798, 63, "-", 45, 1),
   EQUALS(870, 63, "=", 61, 1),
   BACKSPACE(943, 117, "Back", 259, 1),
   TAB(0, 99, "Tab", 258, 2),
   Q(108, 63, "Q", 81, 2),
   W(181, 63, "W", 87, 2),
   E(253, 63, "E", 69, 2),
   R(325, 63, "R", 82, 2),
   T(399, 63, "T", 84, 2),
   Y(471, 63, "Y", 89, 2),
   U(543, 63, "U", 85, 2),
   I(615, 63, "I", 73, 2),
   O(689, 63, "O", 79, 2),
   P(761, 63, "P", 80, 2),
   BRACKET_OPEN(833, 63, "[", 91, 2),
   BRACKET_CLOSE(905, 63, "]", 93, 2),
   BACKSLASH(978, 82, "\\", 92, 2),
   CAPSLOCK(0, 116, "Caps Lock", 280, 3),
   A(127, 63, "A", 65, 3),
   S(199, 63, "S", 83, 3),
   D(271, 63, "D", 68, 3),
   F(343, 63, "F", 70, 3),
   G(417, 63, "G", 71, 3),
   H(489, 63, "H", 72, 3),
   J(561, 63, "J", 74, 3),
   K(633, 63, "K", 75, 3),
   L(707, 63, "L", 76, 3),
   SEMICOLON(779, 63, ";", 59, 3),
   APOSTROPHE(851, 63, "'", 39, 3),
   RETURN(924, 136, "Return", 257, 3),
   LEFT_SHIFT(0, 153, "Shift", 340, 4),
   Z(164, 63, "Z", 90, 4),
   X(236, 63, "X", 88, 4),
   C(308, 63, "C", 67, 4),
   V(381, 63, "V", 86, 4),
   B(454, 63, "B", 66, 4),
   N(526, 63, "N", 78, 4),
   M(598, 63, "M", 77, 4),
   COMMA(671, 63, ",", 44, 4),
   DOT(744, 63, ".", 46, 4),
   SLASH(816, 63, "/", 47, 4),
   RIGHT_SHIFT(888, 172, "Shift", 344, 4),
   LEFT_CONTROL(0, 97, "Ctrl", 341, 5),
   LEFT_SUPER(106, 63, "Meta", 343, 5),
   LEFT_ALT(178, 103, "Alt", 342, 5),
   SPACE(290, 427, "Space", 32, 5),
   ALT(726, 97, "Alt Gr", 346, 5),
   RIGHT_SUPER(833, 63, "Meta", 347, 5),
   MENU(905, 63, "Menu", 348, 5),
   RIGHT_CONTROL(978, 82, "Ctrl", 345, 5);

   public int x;
   public int y = 63;
   public String name;
   public boolean enabled;
   public int keyCode;
   public int row;

   private Keys(int x, int y, String name, int row, int keyCode) {
      this.x = x;
      this.y = y;
      this.enabled = true;
      this.name = name;
      this.row = row;
      this.keyCode = keyCode;
   }

   private Keys(int x, int y, String var5, int var6) {
      this.x = x;
      this.y = y;
      this.enabled = false;
   }

   public int method9026() {
      return 74 * (this.keyCode - 1);
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public int method9029() {
      return 63;
   }
}
