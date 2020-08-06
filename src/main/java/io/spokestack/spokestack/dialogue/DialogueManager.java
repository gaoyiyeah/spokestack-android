package io.spokestack.spokestack.dialogue;

import io.spokestack.spokestack.nlu.NLUResult;
import io.spokestack.spokestack.nlu.TraceListener;
import io.spokestack.spokestack.util.Callback;
import io.spokestack.spokestack.util.EventTracer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Spokestack's dialogue manager.
 * </p>
 *
 * <p>
 * This component transforms intent and slot results from an {@link
 * io.spokestack.spokestack.nlu.NLUService} into actions to be performed by the
 * app, including both scene transitions and external requests for data. A
 * system response to the user's request is also included; it can be synthesized
 * and read to the user or printed in a text stream.
 * </p>
 *
 * <p>
 * In order to perform that transformation, the manager relies on a <i>dialogue
 * policy</i>. An app may use Spokestack's custom policy by providing a JSON
 * file describing the policy's actions at build time, or a custom policy may be
 * provided by implementing {@link DialoguePolicy} and passing the name of that
 * class to the manager's builder.
 * </p>
 */
public final class DialogueManager implements Callback<NLUResult> {

    private final DialoguePolicy policy;
    private final ConversationData dataStore;
    private final EventTracer tracer;
    private final List<TraceListener> traceListeners;

    private DialogueManager(Builder builder) {
        if (builder.policyFile != null) {
            this.policy = new SpokestackDialoguePolicy(builder.policyFile);
        } else {
            this.policy = builder.dialoguePolicy;
        }
        if (builder.conversationData != null) {
            this.dataStore = builder.conversationData;
        } else {
            this.dataStore = new InMemoryConversationData();
        }
        this.tracer = new EventTracer(builder.traceLevel);
        this.traceListeners = builder.listeners;
    }

    /**
     * Create a response to a user request using the manager's dialogue policy.
     *
     * @param userTurn The user's request as classified by an NLU system.
     * @return A system response to {@code userTurn}, including textual replies
     * for TTS synthesis and visual output as well as cues for what the app
     * should display.
     */
    public DialogueResponse respond(NLUResult userTurn) {
        DialogueResponse response =
              this.policy.handleTurn(userTurn, this.dataStore);
        updateState(userTurn, response);
        return response;
    }

    private void updateState(NLUResult userTurn, DialogueResponse response) {
        // TODO use dataStore to update conversation state & slot values
    }

    @Override
    public void call(@NotNull NLUResult arg) {
        respond(arg);
    }

    @Override
    public void onError(@NotNull Throwable err) {
        dispatchTrace(EventTracer.Level.WARN, "NLU classification error: "
              + err.getLocalizedMessage());
    }

    private void dispatchTrace(EventTracer.Level level, String message) {
        if (this.tracer.canTrace(level)) {
            for (TraceListener listener : this.traceListeners) {
                listener.onTrace(level, message);
            }
        }
    }

    /**
     * Dialogue manager builder API.
     */
    public static class Builder {

        private String policyFile;
        private DialoguePolicy dialoguePolicy;
        private ConversationData conversationData;
        private int traceLevel;
        private final List<TraceListener> listeners = new ArrayList<>();

        /**
         * Specify the path to the JSON file containing a Spokestack dialogue
         * policy for the manager to use.
         * @param file Path to a dialogue configuration file.
         * @return the updated builder state
         */
        public Builder withPolicyFile(String file) {
            if (this.dialoguePolicy != null) {
                throw new IllegalArgumentException("dialogue manager cannot"
                      + "use both a policy file and a custom policy");
            }
            this.policyFile = file;
            return this;
        }

        /**
         * Specify the dialogue policy for the manager to use.
         * @param policy The dialogue policy to follow for user interactions.
         * @return the updated builder state
         */
        public Builder withCustomPolicy(DialoguePolicy policy) {
            if (this.policyFile != null) {
                throw new IllegalArgumentException("dialogue manager cannot"
                      + "use both a policy file and a custom policy");
            }
            this.dialoguePolicy = policy;
            return this;
        }

        /**
         * Specify the data store to use for conversation data.
         * @param dataStore The data store to use for the conversation.
         * @return the updated builder state
         */
        public Builder withDataStore(ConversationData dataStore) {
            this.conversationData = dataStore;
            return this;
        }

        /**
         * Specify the maximum level of log messages to be delivered to
         * listeners.
         * @param level The maximum log level to deliver.
         * @return the updated builder state
         */
        public Builder withTraceLevel(int level) {
            this.traceLevel = level;
            return this;
        }

        /**
         * Add a trace listener to receive dialogue-related log messages.
         * @param listener A log message listener.
         * @return the updated builder state
         */
        public Builder addTraceListener(TraceListener listener) {
            this.listeners.add(listener);
            return this;
        }

        /**
         * Build a dialogue manager that reflects the current builder state.
         * @return A dialogue manager that reflects the current builder state.
         */
        public DialogueManager build() {
            if (this.policyFile == null && this.dialoguePolicy == null) {
                throw new IllegalArgumentException("dialogue manager requires"
                      + "either a policy file or custom policy");
            }
            return new DialogueManager(this);
        }
    }
}
