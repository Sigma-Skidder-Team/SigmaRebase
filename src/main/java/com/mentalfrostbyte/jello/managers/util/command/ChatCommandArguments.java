package com.mentalfrostbyte.jello.managers.util.command;

public class ChatCommandArguments {
    private final String arguments;

    public ChatCommandArguments(String args) {
        this.arguments = args;
    }

    public CommandType getCommandType() {
        try {
            double var3 = Double.parseDouble(this.arguments);
        } catch (NullPointerException | NumberFormatException var6) {
            return CommandType.TEXT;
        }

        return CommandType.NUMBER;
    }

    public double getDouble() {
        try {
            return Double.parseDouble(this.arguments);
        } catch (NullPointerException | NumberFormatException var4) {
            return 0.0;
        }
    }

    public float getFloat() {
        try {
            return (float)Double.parseDouble(this.arguments);
        } catch (NullPointerException | NumberFormatException var4) {
            return 0.0F;
        }
    }

    public int getInt() {
        try {
            return (int)Double.parseDouble(this.arguments);
        } catch (NullPointerException | NumberFormatException var4) {
            return 0;
        }
    }

    public String getArguments() {
        return this.arguments;
    }
}
