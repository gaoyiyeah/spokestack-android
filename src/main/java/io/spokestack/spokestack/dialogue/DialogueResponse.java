package io.spokestack.spokestack.dialogue;

/**
 * A simple data class that represents a system response to a user's natural
 * language request.
 *
 * <p>
 * A system response includes the following, some of which may be {@code null}
 * for any given instance:
 * </p>
 *
 * <ul>
 *     <li>
 *         A reply prompt to be printed and/or read to the user.
 *     </li>
 *     <li>
 *         A destination scene to be displayed in the app.
 *     </li>
 *     <li>
 *         The name of an app feature the user intended to activate (e.g.,
 *         search, list item selection).
 *     </li>
 *     <li>
 *         A flag representing whether the system expects a reply from the user
 *         after the prompt is delivered.
 *     </li>
 * </ul>
 */
public final class DialogueResponse {

    private final boolean endsConversation;
    private final String feature;
    private final Prompt prompt;
    private final String scene;

    private DialogueResponse(Builder builder) {
        this.endsConversation = builder.endsConversation;
        this.feature = builder.feature;
        this.prompt = builder.prompt;
        this.scene = builder.scene;
    }

    /**
     * @return {@code true} if the conversation with the user should end after
     * this response; {@code false} if the system should continue listening.
     */
    public boolean endsConversation() {
        return endsConversation;
    }

    /**
     * @return The name of the app feature that the user activated via voice, if
     * any.
     */
    public String getFeature() {
        return feature;
    }

    /**
     * @return The prompt to be delivered to the user.
     */
    public Prompt getPrompt() {
        return prompt;
    }

    /**
     * @return The name of the app scene that corresponds to the system's reply.
     */
    public String getScene() {
        return scene;
    }

    /**
     * Dialogue response builder API.
     */
    public static final class Builder {

        private final Prompt prompt;
        private String scene;
        private String feature;
        private boolean endsConversation;

        /**
         * Creates a new response builder.
         *
         * @param systemPrompt The prompt to be delivered to the user.
         */
        public Builder(Prompt systemPrompt) {
            this.prompt = systemPrompt;
        }

        /**
         * Specify that the response should end the conversation with the user.
         *
         * @return the updated builder state
         */
        public Builder endConversation() {
            this.endsConversation = true;
            return this;
        }

        /**
         * Specify a destination scene that corresponds to the system's reply.
         *
         * @param destinationScene The name of the scene that should be
         *                         displayed to the user.
         * @return the updated builder state
         */
        public Builder withScene(String destinationScene) {
            this.scene = destinationScene;
            return this;
        }

        /**
         * Specify a feature activated by the user's request.
         *
         * @param featureName The name of the feature activated by the user.
         * @return the updated builder state
         */
        public Builder withFeature(String featureName) {
            this.feature = featureName;
            return this;
        }

        /**
         * Build a new system response from the current builder state.
         *
         * @return A system response that reflects the current builder state.
         */
        public DialogueResponse build() {
            return new DialogueResponse(this);
        }
    }
}
