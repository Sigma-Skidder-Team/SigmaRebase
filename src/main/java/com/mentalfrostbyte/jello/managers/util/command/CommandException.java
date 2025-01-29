package com.mentalfrostbyte.jello.managers.util.command;

public class CommandException extends Exception {
    public String reason;

    public CommandException() {
        this.reason = "";
    }

    public CommandException(String reason) {
        this.reason = reason;
    }
}
