package io.spokestack.spokestack.dialogue;

import io.spokestack.spokestack.nlu.NLUResult;

/**
 * Spokestack's built-in dialoge policy.
 *
 * <p>
 * This dialogue policy loads a dialogue configuration from a JSON file and
 * determines transitions for the app based on the rules and scenes listed in
 * that file.
 * </p>
 *
 * <p>
 * See {@code URL TODO} for more information on the Spokestack dialogue format.
 * </p>
 */
public class SpokestackDialoguePolicy implements DialoguePolicy {

    /**
     * Creates a dialogue policy from the supplied file.
     * @param configFile Path to a JSON configuration for the dialogue.
     */
    public SpokestackDialoguePolicy(String configFile) {
        // TODO load JSON config
    }

    @Override
    public DialogueResponse handleTurn(NLUResult userTurn,
                                       ConversationData conversationData) {
        return null;
    }
}
