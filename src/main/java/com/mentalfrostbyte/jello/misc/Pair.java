package com.mentalfrostbyte.jello.misc;

public class Pair<key, value> {
    private key key;
    private value value;

    public Pair(key key, value value) {
        this.key = key;
        this.value = value;
    }

    public void setKey(key key) {
        this.key = key;
    }

    public void setValue(value value) {
        this.value = value;
    }

    public key getKey() {
        return this.key;
    }

    public value getValue() {
        return this.value;
    }
}
