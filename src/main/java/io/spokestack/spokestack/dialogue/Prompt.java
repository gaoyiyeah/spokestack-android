package io.spokestack.spokestack.dialogue;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A prompt to be delivered to the user. Prompts can include representations of
 * a system reply formatted for print and for TTS synthesis as well as reprompts
 * to deliver to the user after a period of time has passed and hints for the
 * system about handling expected user responses to the prompt's wording.
 *
 * <p>
 * For an example of the latter, a prompt's text might end in asking the user a
 * yes/no question. The meanings of "Yes" and "no" are highly contextual, so
 * prompts that end in such questions should include a {@link Proposal} that
 * indicates the action the app should take if a user responds in the
 * affirmative or negative.
 * </p>
 */
public final class Prompt {

    private final String id;
    private final String text;
    private final String voice;
    private final Proposal proposal;
    private final List<Prompt> reprompts;

    private Prompt(Builder builder) {
        this.id = builder.id;
        this.text = builder.text;
        if (builder.voice == null) {
            this.voice = builder.text;
        } else {
            this.voice = builder.voice;
        }
        this.proposal = builder.proposal;
        this.reprompts = builder.reprompts;
    }

    /**
     * @return The prompt's ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Get a version of the prompt formatted for TTS synthesis.
     *
     * @param data The current conversation data, used to expand any template
     *             placeholders in the prompt's text.
     * @return A version of the prompt formatted for TTS synthesis.
     */
    public String getVoiceReply(ConversationData data) {
        // TODO fill templates
        return this.voice;
    }

    /**
     * Get a version of the prompt formatted for print.
     *
     * @param data The current conversation data, used to expand any template
     *             placeholders in the prompt's text.
     * @return A version of the prompt formatted for print.
     */
    public String getTextReply(ConversationData data) {
        // TODO fill templates
        return this.text;
    }

    /**
     * @return this prompt's proposal.
     */
    public Proposal getProposal() {
        return proposal;
    }

    /**
     * @return any reprompts associated with this prompt.
     */
    public List<Prompt> getReprompts() {
        return reprompts;
    }

    /**
     * Prompt builder API.
     */
    public static final class Builder {

        private final String id;
        private final String text;
        private String voice;
        private Proposal proposal;
        private List<Prompt> reprompts;

        /**
         * Create a new prompt builder with the minimal set of required data.
         *
         * @param promptId  The prompt's ID.
         * @param textReply A reply template formatted for print.
         */
        public Builder(@NonNull String promptId, @NonNull String textReply) {
            this.id = promptId;
            this.text = textReply;
            this.reprompts = new ArrayList<>();
        }

        /**
         * Add a reply template formatted for TTS synthesis to the current
         * prompt.
         *
         * @param voiceReply The voice prompt to be added.
         * @return the updated builder
         */
        public Builder withText(@NonNull String voiceReply) {
            this.voice = voiceReply;
            return this;
        }

        /**
         * Add a proposal to the current prompt.
         *
         * @param prop The proposal to be added.
         * @return the updated builder
         */
        public Builder withProposal(@NonNull Proposal prop) {
            this.proposal = prop;
            return this;
        }

        /**
         * Specify reprompts for the current prompt.
         *
         * @param prompts The reprompts to attach.
         * @return the updated builder
         */
        public Builder withReprompts(@NonNull List<Prompt> prompts) {
            this.reprompts = new ArrayList<>(prompts);
            return this;
        }

        /**
         * @return a complete prompt created from the current builder state.
         */
        public Prompt build() {
            return new Prompt(this);
        }
    }
}
