package net.minecraft.util.text.event;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ClickEventAction {
    OPEN_URL("open_url", true),
    OPEN_FILE("open_file", false),
    RUN_COMMAND("run_command", true),
    SUGGEST_COMMAND("suggest_command", true),
    CHANGE_PAGE("change_page", true),
    COPY_TO_CLIPBOARD("copy_to_clipboard", true);

    private static final Map<String, ClickEventAction> NAME_MAPPING = Arrays.<ClickEventAction>stream(values())
            .collect(Collectors.toMap(ClickEventAction::getCanonicalName, p_199851_0_ -> (ClickEventAction)p_199851_0_));
    private final boolean allowedInChat;
    private final String canonicalName;
    private static final ClickEventAction[] $VALUES = new ClickEventAction[]{
            OPEN_URL, OPEN_FILE, RUN_COMMAND, SUGGEST_COMMAND, CHANGE_PAGE, COPY_TO_CLIPBOARD
    };

    private ClickEventAction(String canonicalNameIn, boolean allowedInChatIn) {
        this.canonicalName = canonicalNameIn;
        this.allowedInChat = allowedInChatIn;
    }

    public boolean shouldAllowInChat() {
        return this.allowedInChat;
    }

    public String getCanonicalName() {
        return this.canonicalName;
    }

    public static ClickEventAction getValueByCanonicalName(String canonicalNameIn) {
        return NAME_MAPPING.get(canonicalNameIn);
    }
}
