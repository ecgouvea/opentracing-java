package io.opentracing.mdcdemo;

import io.opentracing.ActiveSpanSource;
import io.opentracing.Span;
import io.opentracing.impl.AbstractActiveSpan;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MDCActiveSpan illustrates the core ActiveSpan concepts and capabilities to a first approximation. Not
 * production-quality code.
 */
class MDCActiveSpan extends AbstractActiveSpan {
    private MDCActiveSpanSource mdcActiveSpanSource;
    private MDCActiveSpan toRestore = null;

    MDCActiveSpan(MDCActiveSpanSource mdcActiveSpanSource, Span span, Map<String, String> mdcContext, AtomicInteger refCount) {
        super(span, refCount);
        this.mdcActiveSpanSource = mdcActiveSpanSource;
        this.toRestore = mdcActiveSpanSource.tlsSnapshot.get();
        mdcActiveSpanSource.tlsSnapshot.set(this);
        MDC.setContextMap(mdcContext);
    }

    @Override
    protected void doDeactivate() {
        if (mdcActiveSpanSource.tlsSnapshot.get() != this) {
            // This shouldn't happen if users call methods in the expected order. Bail out.
            return;
        }
        mdcActiveSpanSource.tlsSnapshot.set(toRestore);
    }

    @Override
    protected ActiveSpanSource spanSource() {
        return mdcActiveSpanSource;
    }

    static class MDCContinuation extends AbstractActiveSpan.AbstractContinuation {
        private MDCActiveSpanSource mdcActiveSpanSource;
        private final Map<String, String> mdcContext;
        private final Span span;

        MDCContinuation(MDCActiveSpanSource source, Span span, AtomicInteger refCount) {
            super(refCount);
            this.mdcActiveSpanSource = source;
            this.mdcContext = MDC.getCopyOfContextMap();
            this.span = span;
        }

        @Override
        public MDCActiveSpan activate() {
            return new MDCActiveSpan(mdcActiveSpanSource, span, mdcContext, refCount);
        }
    }

}