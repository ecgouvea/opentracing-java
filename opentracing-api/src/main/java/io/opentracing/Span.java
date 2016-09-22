/**
 * Copyright 2016 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.opentracing;

import java.util.Map;

/**
 * Represents an in-flight span in the opentracing system.
 *
 * <p>Spans are created by the {@link Tracer#buildSpan} interface.
 */
public interface Span extends AutoCloseable {
    /**
     * Retrieve the associated SpanContext.
     *
     * This may be called at any time, including after calls to finish().
     *
     * @return the SpanContext that encapsulates Span state that should propagate across process boundaries.
     */
    SpanContext context();

    /**
     * Sets the end timestamp to now and records the span.
     *
     * <p>With the exception of calls to Span.context(), this should be the last call made to the span instance, and to
     * do otherwise leads to undefined behavior.
     *
     * @see Span#context()
     */
    void finish();

    /**
     * Sets an explicit end timestamp and records the span.
     *
     * <p>With the exception of calls to Span.context(), this should be the last call made to the span instance, and to
     * do otherwise leads to undefined behavior.
     *
     * @param finishMicros an explicit finish time, in microseconds since the epoch
     *
     * @see Span#context()
     */
    void finish(long finishMicros);

    void close();

    /**
     * Set a key:value tag on the Span.
     */
    // overloaded 3x to support the BasicType concern
    Span setTag(String key, String value);

    /** Same as {@link #setTag(String, String)}, but for boolean values. */
    Span setTag(String key, boolean value);

    /** Same as {@link #setTag(String, String)}, but for numeric values. */
    Span setTag(String key, Number value);

    /**
     * Log key:value pairs to the Span with the current walltime timestamp.
     *
     * <p>A contrived example (using Guava, which is not required):
     * <pre>{@code
     span.log(
         ImmutableMap.Builder<String, Object>()
             .put("event", "soft error")
             .put("type", "cache timeout")
             .put("waited.millis", 1500)
             .build());
     }</pre>
     *
     * @param fields key:value log fields. Tracer implementations are expected to support String, numeric, and boolean
     *               values; some may also support arbitrary Objects.
     * @return the Span, for chaining
     */
    Span log(Map<String, Object> fields);

    /**
     * Like log(Map&lt;String, Object&gt;), but with an explicit timestamp.
     *
     * @param timestampMicroseconds The explicit timestamp for the log record. Must be greater than or equal to the
     *                              Span's start timestamp.
     * @param fields key:value log fields. Tracer implementations are expected to support String, numeric, and boolean
     *               values; some may also support arbitrary Objects.
     * @return the Span, for chaining
     */
    Span log(long timestampMicroseconds, Map<String, Object> fields);

    /**
     * Sets a baggage item in the Span (and its SpanContext) as a key/value pair.
     *
     * Baggage enables powerful distributed context propagation functionality where arbitrary application data can be
     * carried along the full path of request execution throughout the system.
     *
     * Note 1: Baggage is only propagated to the future (recursive) children of this SpanContext.
     *
     * Note 2: Baggage is sent in-band with every subsequent local and remote calls, so this feature must be used with
     * care.
     *
     * @return this Span instance, for chaining
     */
    Span setBaggageItem(String key, String value);

    /**
     * @return the value of the baggage item identified by the given key, or null if no such item could be found
     */
    String getBaggageItem(String key);

    /**
     * Sets the string name for the logical operation this span represents.
     *
     * @return this Span instance, for chaining
     */
    Span setOperationName(String operationName);

    /**
     * @deprecated use {@link #log(Map)} like this
     * {@code span.log(Map.of("event", "timeout", "millis", 1500))}
     **/
    Span log(String eventName, /* @Nullable */ Object payload);
    /**
     * @deprecated use {@link #log(Map)} like this
     * {@code span.log(timestampMicroseconds, Map.of("event", "timeout", "millis", 1500))}
     **/
    Span log(long timestampMicroseconds, String eventName, /* @Nullable */ Object payload);
}
