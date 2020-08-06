package io.spokestack.spokestack.dialogue;

/**
 * A proposal indicates the intended interpretation of a user's "yes" or "no"
 * response in the current conversational context.
 */
public class Proposal {

    private final String accept;
    private final String reject;

    /**
     * Creates a new proposal.
     * @param acceptIntent The intent to be performed if the user responds in
     *                     the affirmative.
     * @param rejectIntent The intent to be performed if the user responds in
     *                     the negative.
     */
    public Proposal(String acceptIntent, String rejectIntent) {
        if (acceptIntent == null) {
            acceptIntent = Intent.NEXT.getIntentName();
        }
        if (rejectIntent == null) {
            rejectIntent = Intent.BACK.getIntentName();
        }
        this.accept = acceptIntent;
        this.reject = rejectIntent;
    }
}
