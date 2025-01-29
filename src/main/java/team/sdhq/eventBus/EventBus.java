package team.sdhq.eventBus;

import com.mentalfrostbyte.Client;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.HigherPriority;
import team.sdhq.eventBus.annotations.priority.HighestPriority;
import team.sdhq.eventBus.annotations.priority.LowerPriority;
import team.sdhq.eventBus.annotations.priority.LowestPriority;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Made by Jan (raca@sdhq)
 */
public final class EventBus {

    /**
     * This just holds all event and methods registered with them
     */
    private static final HashMap<Class<? extends Event> /* Event */, HashMap<Method /* Called method */, Object /* Class instance */>> REGISTERED_METHODS = new HashMap<>();

    /**
     * Registers all method of a class to be called by the <code>call</code> method
     *
     * @param o The instance of the class that contains the method(s)
     */
    public static void register(final Object o) {
        for (Method method : o.getClass().getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers())) continue;
            if (!method.isAnnotationPresent(EventTarget.class)) continue;

            final Class<?>[] params = method.getParameterTypes();

            if (params.length != 1) continue;

            final Class<?> arg = params[0];

            if (!instanceofEvent(arg)) continue;

            /* Ignore the warning here, the above line prevents this from breaking */
            ensureHashmap((Class<? extends Event>) arg);

            REGISTERED_METHODS.get(arg).put(method, o);
        }

        sortEventReceivers();
    }

    /**
     * Register but backwards
     */
    public static void unregister(final Object o) {
        for (Method method : o.getClass().getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers())) continue;
            if (!method.isAnnotationPresent(EventTarget.class)) continue;

            REGISTERED_METHODS.values().forEach(hm -> hm.remove(method));
        }
    }

    /**
     * Calls all registered method that need that event
     *
     * @param e Event that's being passed as an argument
     */
    public static void call(final Event e) {
        final HashMap<Method, Object> map = REGISTERED_METHODS.get(e.getClass());

        if (map == null) return; // No registered methods

        try {
            for (Method m : map.keySet()) {
                Object instance = map.get(m);
                try {
                    m.invoke(instance, e);
                } catch (IllegalAccessException ex) {
                    Client.getInstance().getLogger().error("!!! PRIVATE EVENT LISTENER: " + instance.getClass().getName() + "#" + m.getName());
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
        } catch (ConcurrentModificationException ex) {
            ex.printStackTrace();
            Client
                    .getInstance()
                    .getLogger()
                    .warn("Ignored concurrent modification exception because those are gay");
        }
    }

    /* Utilities */

    private static boolean instanceofEvent(Class<?> arg) {
        while (!arg.isAssignableFrom(Event.class)) {
            if (arg.isAssignableFrom(Object.class)) return false;

            arg = arg.getSuperclass();
        }

        return true;
    }

    /**
     * Nothing too special, just prevents NullPointerException from being thrown
     *
     * @param arg Event
     */
    private static void ensureHashmap(final Class<? extends Event> arg) {
        if (REGISTERED_METHODS.containsKey(arg)) return;

        REGISTERED_METHODS.put(arg, new HashMap<>());
    }

    private static int getPriority(final Method method) {
        if (method.isAnnotationPresent(HighestPriority.class)) {
            return 1;
        } else if (method.isAnnotationPresent(HigherPriority.class)) {
            return 2;
        } else if (method.isAnnotationPresent(LowerPriority.class)) {
            return 4;
        } else if (method.isAnnotationPresent(LowestPriority.class)) {
            return 5;
        } else {
            return 3; // No annotation = normal priority
        }
    }

    /**
     * Oh god, what have I done
     */
    public static void sortEventReceivers() {
        for (Class<? extends Event> eventClass : REGISTERED_METHODS.keySet()) {
            REGISTERED_METHODS.replace(eventClass, REGISTERED_METHODS.get(eventClass).entrySet()
                    .stream()
                    .sorted(Comparator.comparingInt(e -> getPriority(e.getKey())))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    )));
        }
    }

}
