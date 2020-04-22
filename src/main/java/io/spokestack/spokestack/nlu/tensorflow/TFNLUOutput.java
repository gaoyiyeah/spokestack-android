package io.spokestack.spokestack.nlu.tensorflow;

import androidx.annotation.NonNull;
import io.spokestack.spokestack.nlu.NLUContext;
import io.spokestack.spokestack.nlu.Slot;
import io.spokestack.spokestack.util.Tuple;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * An internal class to contain the business logic for turning raw model outputs
 * into usable intents/slots.
 */
final class TFNLUOutput {

    private final Metadata metadata;
    private Map<String, SlotParser> slotParsers;

    TFNLUOutput(Metadata nluMetadata) {
        this.metadata = nluMetadata;
        this.slotParsers = new HashMap<>();
    }

    /**
     * Set the parsers that should be used for a collection of slot types.
     * @param parsers A map of slot type to the parser used for that type.
     */
    public void registerSlotParsers(Map<String, SlotParser> parsers) {
        this.slotParsers = parsers;
    }

    /**
     * Extract the intent from the model's output tensor.
     *
     * @param output   The output tensor containing the intent prediction.
     * @return A tuple consisting of the intent from the model's output tensor
     * and the model's posterior probability (confidence value) for that
     * prediction.
     */
    public Tuple<Metadata.Intent, Float> getIntent(ByteBuffer output) {
        Metadata.Intent[] intents = this.metadata.getIntents();
        Tuple<Integer, Float> prediction = bufferArgMax(output, intents.length);
        return new Tuple<>(intents[prediction.first()], prediction.second());
    }

    /**
     * Extract the slot values captured for a specific utterance from the
     * model's slot tag output tensor.
     *
     * @param context  The context used to communicate trace events.
     * @param encoded  The original encoded input, used to determine string
     *                 values for model output.
     * @param output   The output tensor containing slot tag predictions.
     * @return A map of slot name to raw string values.
     */
    public Map<String, String> getSlots(
          NLUContext context,
          EncodedTokens encoded,
          ByteBuffer output) {
        int numTokens = encoded.getIds().size();
        String[] tagLabels = getLabels(output, numTokens);
        context.traceDebug("Tag labels: %s", Arrays.toString(tagLabels));
        Map<Integer, Integer> slotLocations = new HashMap<>();
        Integer curSlotStart = null;
        for (int i = 0; i < tagLabels.length; i++) {
            String label = tagLabels[i];
            if (label.equals("o")) {
                if (curSlotStart != null) {
                    curSlotStart = null;
                }
            } else {
                if (label.startsWith("b")) {
                    curSlotStart = i;
                }

                // add both b_ and i_ tagged tokens to the current slot
                if (curSlotStart != null) {
                    slotLocations.put(curSlotStart, i + 1);
                }
            }
        }

        Map<String, String> slots = new HashMap<>();
        for (Map.Entry<Integer, Integer> slotRange : slotLocations.entrySet()) {
            String value = encoded.decodeRange(slotRange.getKey(),
                  slotRange.getValue(), true);
            String slotName = tagLabels[slotRange.getKey()].substring(2);
            String curValue = slots.get(slotName);
            if (curValue != null) {
                curValue += " " + value;
                slots.put(slotName, curValue);
            } else {
                slots.put(slotName, value);
            }
        }
        return slots;
    }

    String[] getLabels(ByteBuffer output, int numTokens) {
        int numTags = this.metadata.getTags().length;
        String[] labels = new String[numTokens];
        for (int i = 0; i < labels.length; i++) {
            Tuple<Integer, Float> labelled = bufferArgMax(output, numTags);
            labels[i] = this.metadata.getTags()[labelled.first()];
        }
        return labels;
    }

    private Tuple<Integer, Float> bufferArgMax(ByteBuffer buffer, int n) {
        float[] posteriors = new float[n];
        for (int i = 0; i < n; i++) {
            posteriors[i] = buffer.getFloat();
        }
        return argMax(posteriors);
    }

    private Tuple<Integer, Float> argMax(float[] values) {
        int maxIndex = 0;
        float maxValue = values[0];

        for (int i = 1; i < values.length; i++) {
            float curVal = values[i];
            if (curVal > maxValue) {
                maxIndex = i;
                maxValue = curVal;
            }
        }
        return new Tuple<>(maxIndex, maxValue);
    }

    /**
     * Parse raw slot values into objects according to the slot parsers
     * registered for this model.
     *
     * @param context  The context used to communicate trace events.
     * @param intent     The intent for which slots are being parsed.
     * @param slotValues A map of slot name to raw string value output from the
     *                   model.
     * @return A map of slot name to parsed slot values.
     */
    public Map<String, Slot> parseSlots(
          @NonNull NLUContext context,
          @NonNull Metadata.Intent intent,
          @NonNull Map<String, String> slotValues) {
        Map<String, Slot> parsed = parseImplicitSlots(intent);
        for (String slotName : slotValues.keySet()) {
            Metadata.Slot metaSlot = intent.getSlot(slotName);
            String slotVal = slotValues.get(slotName);
            if (metaSlot == null) {
                context.traceWarn("no %s slot in %s intent",
                      slotName, intent.getName());
                Slot defaultParse = new Slot(slotName, slotVal, slotVal);
                parsed.put(slotName, defaultParse);
                continue;
            }
            Slot parsedValue = parseSlotValue(metaSlot, slotVal);
            parsed.put(parsedValue.getName(), parsedValue);
        }

        return parsed;
    }

    private Map<String, Slot> parseImplicitSlots(Metadata.Intent intent) {
        Map<String, Slot> slots = new HashMap<>();
        for (Metadata.Slot slot : intent.getImplicitSlots()) {
            Slot parsed = new Slot(slot.getName(),
                  String.valueOf(slot.getValue()), slot.getValue());
            slots.put(slot.getName(), parsed);
        }
        return slots;
    }

    private Slot parseSlotValue(Metadata.Slot metaSlot, String slotValue) {
        SlotParser parser = this.slotParsers.get(metaSlot.getType());
        String slotName = metaSlot.getName();
        try {
            Object parsed = parser.parse(metaSlot.getFacets(), slotValue);
            return new Slot(slotName, slotValue, parsed);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing slot "
                  + slotName, e);
        }
    }
}