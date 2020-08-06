package io.spokestack.spokestack.dialogue;

/**
 * An enumeration of generic intents collected here to avoid the use of bare
 * strings as default values in the dialogue system.
 */
enum Intent {

    NEXT("navigate.next"),
    BACK("navigate.back"),
    RESET("navigate.reset"),
    YES("accept"),
    NO("reject"),
    INFORM("inform");

    private final String intentName;

    Intent(String name) {
        this.intentName = name;
    }

    /**
     * Get the name of the intent as mentioned in the conversation.
     * @return The name of the intent.
     */
    public String getIntentName() {
        return intentName;
    }
}
