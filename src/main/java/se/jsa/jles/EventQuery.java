package se.jsa.jles;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import se.jsa.jles.internal.util.Objects;

/**
 * 
 * @author joasah
 * @deprecated Use EventQuery2 instead
 */
@Deprecated
public class EventQuery {
	private final Map<Class<?>, Matcher> queries;

	EventQuery(Map<Class<?>, Matcher> queries) {
		this.queries = Collections.unmodifiableMap(new HashMap<Class<?>, Matcher>(queries));
	}

	public Collection<Map.Entry<Class<?>, Matcher>> queries() {
		return queries.entrySet();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static EventQuery query(Class<?>... eventTypes) {
		Builder builder = new Builder();
		for (Class<?> eventType : eventTypes) {
			builder = builder.query(eventType);
		}
		return builder.build();
	}

	public static EventQuery query(Class<?> eventType, Matcher match) {
		return new Builder().query(eventType, match).build();
	}

	public static class Builder {
		private final Map<Class<?>, Matcher> queries;
		public Builder() {
			this.queries = Collections.emptyMap();
		}
		private Builder(Map<Class<?>, Matcher> queries, Class<?> eventType, Matcher match) {
			HashMap<Class<?>, Matcher> result = new HashMap<Class<?>, Matcher>(queries);
			result.put(eventType, match);
			this.queries = Collections.unmodifiableMap(result);
		}
		public Builder query(Class<?> eventType) {
			return append(eventType, Matcher.MATCH_ALL);
		}
		public Builder query(Class<?> eventType, Matcher match) {
			return append(eventType, Objects.requireNonNull(match));
		}
		private Builder append(Class<?> eventType, Matcher match) {
			return new Builder(queries, eventType, match);
		}
		public EventQuery build() {
			return new EventQuery(queries);
		}
	}

}
