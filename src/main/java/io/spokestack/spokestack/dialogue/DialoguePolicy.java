package io.spokestack.spokestack.dialogue;

import io.spokestack.spokestack.nlu.NLUResult;

/**
 * <p>
 * The API for dialogue policies used by Spokestack's {@link DialogueManager}
 * component.
 * </p>
 *
 * <p>
 * A dialogue policy must have a no-argument constructor to be used by the
 * dialogue management system.
 * </p>
 */
public interface DialoguePolicy {

    /**
     * Process a user turn and return a relevant response.
     *
     * @param userTurn         The user input as determined by the NLU
     *                         component.
     * @param conversationData Conversation state and data used to resolve and
     *                         prepare a response.
     * @return The system response.
     */
    DialogueResponse handleTurn(NLUResult userTurn,
                                ConversationData conversationData);
}
