package com.mentalfrostbyte.jello.event;

import com.mentalfrostbyte.jello.event.interfaces.Class8733;
import com.mentalfrostbyte.jello.event.priority.Priority;
import com.mentalfrostbyte.jello.module.impl.movement.phase.NCPPhase;
import com.mentalfrostbyte.jello.unmapped.Class8733;
import com.mentalfrostbyte.jello.utilities.ReflectionUtils;
import mapped.ReflectionUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class MethodWrapper {
    private final Object field35681;
    private final Class<?> field35682;
    private final Method field35683;
    private final Class8733 field35684;
    private MethodHandle field35685;
    private final Priority field35686;
    private final boolean field35687;

    public MethodWrapper(Object var1, Class<?> var2, Method var3, Priority var4) {
        this.field35681 = var1;
        this.field35682 = var2;
        this.field35683 = var3;
        this.field35684 = ReflectionUtils.method31586(var3);

        try {
            this.field35685 = MethodHandles.lookup().unreflect(var3);
        } catch (IllegalAccessException var8) {
            var8.printStackTrace();
        }

        this.field35686 = var4;
        this.field35687 = var1.getClass().getSuperclass() == NCPPhase.class.getSuperclass();
    }

    public Object getOBjecct() {
        return this.field35681;
    }

    public MethodHandle method29023() {
        return this.field35685;
    }

    public Class8733 getReflection() {
        return this.field35684;
    }

    public Method method29025() {
        return this.field35683;
    }

    public Priority method29026() {
        return this.field35686;
    }

    public boolean method29027() {
        return this.field35687;
    }

    @Override
    public boolean equals(Object var1) {
        if (!(var1 instanceof MethodWrapper)) {
            return false;
        } else {
            MethodWrapper var4 = (MethodWrapper) var1;
            return this.field35681 == var4.field35681 && this.field35683 == var4.field35683 && this.field35686 == var4.field35686;
        }
    }
}
