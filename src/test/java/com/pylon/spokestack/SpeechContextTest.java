package com.pylon.spokestack;

import java.util.*;
import java.nio.ByteBuffer;

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.pylon.spokestack.SpeechConfig;
import com.pylon.spokestack.SpeechContext;
import com.pylon.spokestack.SpeechContext.Event;
import com.pylon.spokestack.SpeechContext.TraceLevel;

public class SpeechContextTest implements OnSpeechEventListener {
    private Event event;
    private SpeechContext context;

    @Test
    public void testBuffer() {
        SpeechContext context = new SpeechContext(new SpeechConfig());
        assertEquals(null, context.getBuffer());

        Deque<ByteBuffer> buffer = new LinkedList<>();
        context.attachBuffer(buffer);
        assertEquals(buffer, context.getBuffer());

        context.detachBuffer();
        assertEquals(null, context.getBuffer());
    }

    @Test
    public void testIsSpeech() {
        SpeechContext context = new SpeechContext(new SpeechConfig());
        assertFalse(context.isSpeech());

        context.setSpeech(true);
        assertTrue(context.isSpeech());

        context.setSpeech(false);
        assertFalse(context.isSpeech());
    }

    @Test
    public void testIsActive() {
        SpeechContext context = new SpeechContext(new SpeechConfig());
        assertFalse(context.isActive());

        context.setActive(true);
        assertTrue(context.isActive());

        context.setActive(false);
        assertFalse(context.isActive());
    }

    @Test
    public void testTranscript() {
        SpeechContext context = new SpeechContext(new SpeechConfig());
        assertEquals(context.getTranscript(), "");

        context.setTranscript("test");
        assertEquals("test", context.getTranscript());
    }

    @Test
    public void testConfidence() {
        SpeechContext context = new SpeechContext(new SpeechConfig());
        assertEquals(context.getConfidence(), 0.0);

        context.setConfidence(1.0);
        assertEquals(context.getConfidence(), 1.0);
    }

    @Test
    public void testError() {
        SpeechContext context = new SpeechContext(new SpeechConfig());
        assertEquals(null, context.getError());

        context.setError(new Exception("test"));
        assertEquals("test", context.getError().getMessage());

        context.setError(null);
        assertEquals(null, context.getError());
    }

    @Test
    public void testReset() {
        SpeechConfig config = new SpeechConfig()
            .put("trace-level", TraceLevel.DEBUG.value());

        SpeechContext context = new SpeechContext(config);
        context.setActive(true);
        context.setTranscript("test");
        context.setConfidence(1.0);
        context.setError(new Exception("test"));
        context.traceDebug("trace");

        context.reset();
        assertFalse(context.isActive());
        assertEquals(context.getTranscript(), "");
        assertEquals(context.getConfidence(), 0.0);
        assertEquals(null, context.getError());
        assertEquals(null, context.getMessage());
    }

    @Test
    public void testDispatch() {
        SpeechConfig config = new SpeechConfig()
            .put("trace-level", TraceLevel.INFO.value());
        SpeechContext context = new SpeechContext(config);

        // valid listener
        context.addOnSpeechEventListener(this);
        context.dispatch(Event.ACTIVATE);
        assertEquals(Event.ACTIVATE, this.event);
        assertEquals(context, this.context);

        // removed listener
        context.removeOnSpeechEventListener(this);
        this.event = null;
        this.context = null;
        context.dispatch(Event.ACTIVATE);
        assertEquals(null, this.event);
        assertEquals(null, this.context);

        // listener error
        context.addOnSpeechEventListener(this);
        context.addOnSpeechEventListener(new OnSpeechEventListener() {
            public void onEvent(Event event, SpeechContext context)
                    throws Exception{
                throw new Exception("failed");
            }
        });
        context.dispatch(Event.ACTIVATE);
        assertEquals(Event.TRACE, this.event);
    }

    @Test
    public void testTrace() {
        SpeechConfig config = new SpeechConfig();
        SpeechContext context;

        // default tracing
        context = new SpeechContext(config)
            .addOnSpeechEventListener(this);
        context.traceDebug("trace");
        assertFalse(context.canTrace(TraceLevel.DEBUG));
        assertFalse(context.canTrace(TraceLevel.PERF));
        assertFalse(context.canTrace(TraceLevel.INFO));
        assertEquals(null, this.event);
        assertEquals(null, context.getMessage());

        // skipped tracing
        config.put("trace-level", TraceLevel.INFO.value());
        context = new SpeechContext(config)
            .addOnSpeechEventListener(this);
        context.traceDebug("trace");
        assertEquals(null, this.event);
        assertEquals(null, context.getMessage());

        // informational tracing
        config.put("trace-level", TraceLevel.INFO.value());
        context = new SpeechContext(config)
            .addOnSpeechEventListener(this);
        assertFalse(context.canTrace(TraceLevel.DEBUG));
        assertFalse(context.canTrace(TraceLevel.PERF));
        assertTrue(context.canTrace(TraceLevel.INFO));
        context.traceInfo("test %d", 42);
        assertEquals(Event.TRACE, this.event);
        assertEquals("test 42", context.getMessage());
        context.reset();

        // performance tracing
        config.put("trace-level", TraceLevel.PERF.value());
        context = new SpeechContext(config)
            .addOnSpeechEventListener(this);
        assertFalse(context.canTrace(TraceLevel.DEBUG));
        assertTrue(context.canTrace(TraceLevel.PERF));
        assertTrue(context.canTrace(TraceLevel.INFO));
        context.tracePerf("test %d", 42);
        assertEquals(Event.TRACE, this.event);
        assertEquals("test 42", context.getMessage());
        context.reset();

        // debug tracing
        config.put("trace-level", TraceLevel.DEBUG.value());
        context = new SpeechContext(config)
            .addOnSpeechEventListener(this);
        assertTrue(context.canTrace(TraceLevel.DEBUG));
        assertTrue(context.canTrace(TraceLevel.PERF));
        assertTrue(context.canTrace(TraceLevel.INFO));
        context.traceDebug("test %d", 42);
        assertEquals(Event.TRACE, this.event);
        assertEquals("test 42", context.getMessage());
        context.reset();
    }

    @Test
    public void testSpeechEvents() {
        assertEquals("activate", Event.ACTIVATE.toString());
        assertEquals("deactivate", Event.DEACTIVATE.toString());
        assertEquals("recognize", Event.RECOGNIZE.toString());
        assertEquals("error", Event.ERROR.toString());
        assertEquals("trace", Event.TRACE.toString());
    }

    public void onEvent(Event event, SpeechContext context) {
        this.event = event;
        this.context = context;
    }
}
