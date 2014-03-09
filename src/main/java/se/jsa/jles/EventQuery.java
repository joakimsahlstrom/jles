package se.jsa.jles;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import se.jsa.jles.internal.util.Objects;

public class EventQuery {
	private final Map<Class<?>, Match> queries;

	EventQuery(Map<Class<?>, Match> queries) {
		this.queries = Collections.unmodifiableMap(new HashMap<Class<?>, Match>(queries));
	}

	public Collection<Map.Entry<Class<?>, Match>> queries() {
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

	public static EventQuery query(Class<?> eventType, Match match) {
		return new Builder().query(eventType, match).build();
	}

	public static class Builder {
		private final Map<Class<?>, Match> queries;
		public Builder() {
			this.queries = Collections.emptyMap();
		}
		private Builder(Map<Class<?>, Match> queries, Class<?> eventType, Match match) {
			HashMap<Class<?>, Match> result = new HashMap<Class<?>, Match>(queries);
			result.put(eventType, match);
			this.queries = Collections.unmodifiableMap(result);
		}
		public Builder query(Class<?> eventType) {
			return append(eventType, Match.MATCH_ALL);
		}
		public Builder query(Class<?> eventType, Match match) {
			return append(eventType, Objects.requireNonNull(match));
		}
		private Builder append(Class<?> eventType, Match match) {
			return new Builder(queries, eventType, match);
		}
		public EventQuery build() {
			return new EventQuery(queries);
		}
	}

}
