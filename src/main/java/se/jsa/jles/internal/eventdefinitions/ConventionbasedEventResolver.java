/*
 * Copyright 2016 Joakim Sahlström
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.jsa.jles.internal.eventdefinitions;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import se.jsa.jles.EventRepoReport;
import se.jsa.jles.internal.EventDeserializer;
import se.jsa.jles.internal.EventSerializer;
import se.jsa.jles.internal.EventTypeId;
import se.jsa.jles.internal.fields.EventField;
import se.jsa.jles.internal.util.Objects;

public class ConventionbasedEventResolver implements EventResolver {

	private final Set<Class<?>> knownTypes = new HashSet<Class<?>>();
	private final Map<Class<?>, Set<Class<?>>> eventToSerializableEventMap = new HashMap<Class<?>, Set<Class<?>>>();
	private final Map<Class<?>, Method> eventToGetSerializableEventMethodMap = new HashMap<Class<?>, Method>();
	private final Map<Class<?>, Method> serializableEventToAsEventMethodMap = new HashMap<Class<?>, Method>();

	@Override
	public EventRepoReport report() {
		EventRepoReport result = new EventRepoReport().appendLine(ConventionbasedEventResolver.class.getSimpleName());
		for (Map.Entry<Class<?>, Set<Class<?>>> eventmapping : eventToSerializableEventMap.entrySet()) {
			result.appendLine(eventmapping.getKey().getSimpleName() + " -> " + describe(eventmapping.getValue()));
		}
		return result;
	}

	private String describe(Set<Class<?>> classes) {
		StringBuilder res = new StringBuilder().append("[");
		boolean first = true;
		for (Class<?> c : classes) {
			if (first) {
				first = false;
			} else {
				res.append(", ");
			}
			res.append(c.getCanonicalName());
		}
		return res.append("]").toString();
	}

	@Override
	public Class<?>[] getSerializableEventTypes(Class<?>[] eventTypes) {
		ArrayList<Class<?>> result = new ArrayList<Class<?>>();
		for (Class<?> eventType : eventTypes) {
			onNewEventType(eventType);
			result.addAll(getSerializableEventTypes(eventType));
		}
		return result.toArray(new Class[result.size()]);
	}

	@Override
	public Object getSerializableEvent(Object event) {
		onNewEventType(event.getClass());
		Method asSerializableMethod = getAsSerializableMethod(event.getClass());
		if (asSerializableMethod != null) {
			try {
				return asSerializableMethod.invoke(event);
			} catch (Exception e) {
				throw new RuntimeException("Bad convention conformance for eventType: " + event.getClass());
			}
		} else {
			return event;
		}
	}

	Object getDeserializedEvent(Object event) {
		Method method = getAsEventMethod(event.getClass());
		if (method != null) {
			try {
				return method.invoke(event);
			} catch (Exception e) {
				throw new RuntimeException("Bad convention conformance for eventType: " + event.getClass(), e);
			}
		} else {
			return event;
		}
	}

	@Override
	public EventDeserializer wrapDeserializer(EventDeserializer eventDeserializer) {
		return new ConventionbasedEventDeserializer(eventDeserializer);
	}

	@Override
	public EventSerializer wrapSerializer(EventSerializer eventSerializer) {
		return new ConventionbasedEventSerializer(eventSerializer);
	}

	private class ConventionbasedEventDeserializer implements EventDeserializer {
		private final EventDeserializer deserializer;

		public ConventionbasedEventDeserializer(EventDeserializer deserializer) {
			this.deserializer = Objects.requireNonNull(deserializer);
		}

		@Override
		public Object deserializeEvent(ByteBuffer input) {
			Object deserializedEvent = deserializer.deserializeEvent(input);
			onNewEventType(deserializedEvent.getClass());
			return getDeserializedEvent(deserializedEvent);
		}

		@Override
		public Object deserializeEventField(ByteBuffer input, EventField eventField) {
			Object deserializeEventField = deserializer.deserializeEventField(input, eventField);
			return eventField.mapFieldValue(deserializeEventField);
		}
	}

	private class ConventionbasedEventSerializer implements EventSerializer {
		private final EventSerializer serializer;

		public ConventionbasedEventSerializer(EventSerializer serializer) {
			this.serializer = Objects.requireNonNull(serializer);
		}

		@Override
		public ByteBuffer serializeEvent(Object event) {
			onNewEventType(event.getClass());
			Object serializableEvent = getSerializableEvent(event);
			return serializer.serializeEvent(serializableEvent);
		}

		@Override
		public EventTypeId getEventTypeId() {
			return serializer.getEventTypeId();
		}
	}

	private Set<Class<?>> getSerializableEventTypes(Class<?> eventType) {
		return eventToSerializableEventMap.get(eventType);
	}

	private Method getAsSerializableMethod(Class<? extends Object> eventType) {
		return eventToGetSerializableEventMethodMap.get(eventType);
	}

	private Method getAsEventMethod(Class<? extends Object> serializableEventType) {
		return serializableEventToAsEventMethodMap.get(serializableEventType);
	}

	// event types registration

	@Override
	public void registerEventTypes(Class<?>[] eventTypes) {
		for (Class<?> eventType : eventTypes) {
			onNewEventType(eventType);
		}
	}

	public void onNewEventType(Class<?> eventType) {
		if (!knownTypes.contains(eventType)) {
			registerAnyAsSerializableMethod(eventType);
			registerAnyAsEventMethod(eventType);
			knownTypes.add(eventType);
		}
	}

	// asSerializable

	private void registerAnyAsSerializableMethod(Class<?> eventType) {
		Method asSerializableMethod = findAsSerializableMethod(eventType);
		registerEventToSerializableEventType(eventType, asSerializableMethod != null ? asSerializableMethod.getReturnType() : eventType);
		eventToGetSerializableEventMethodMap.put(eventType, isAsSerializableMethod(asSerializableMethod) ? asSerializableMethod : null);
	}

	private Method findAsSerializableMethod(Class<?> eventType) {
		Method method = null;
		try {
			method = eventType.getMethod("asSerializable");
			verifyAsSerializableMethod(eventType, method);
		} catch (NoSuchMethodException e) {
			// ok
		} catch (Exception e) {
			throw new RuntimeException("Bad convention conformance for eventType: " + eventType, e);
		}
		return method;
	}

	private void verifyAsSerializableMethod(Class<?> eventType, Method asSerializableMethod) {
		try {
			if (!eventType.equals(asSerializableMethod.getReturnType().getMethod("asEvent").getReturnType())) {
				throw new RuntimeException("Bad convention conformance for eventType: " + eventType);
			}
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Bad convention conformance for eventType: " + eventType, e);
		} catch (SecurityException e) {
			throw new RuntimeException("Bad convention conformance for eventType: " + eventType, e);
		}

		try {
			asSerializableMethod.getReturnType().getConstructor(); // Verify that the serializable event has the correct constructor signature
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Serializable event class missing no argument constructor: " + asSerializableMethod.getReturnType().getSimpleName(), e);
		} catch (SecurityException e) {
			throw new RuntimeException("Serializable event class non-public no argument constructor: " + asSerializableMethod.getReturnType().getSimpleName(), e);
		}
	}

	private void registerEventToSerializableEventType(Class<?> eventType, Class<?> serializableEventType) {
		if (!eventToSerializableEventMap.containsKey(eventType)) {
			eventToSerializableEventMap.put(eventType, new HashSet<Class<?>>());
		}
		eventToSerializableEventMap.get(eventType).add(serializableEventType);
	}

	private boolean isAsSerializableMethod(Method method) {
		return method != null && method.getParameterTypes().length == 0;
	}

	// asEvent

	private void registerAnyAsEventMethod(Class<?> serializableEventType) {
		Method method = null;
		try {
			method = serializableEventType.getMethod("asEvent");
			verifyAsEventMethod(serializableEventType, method);
		} catch (NoSuchMethodException e) {
			// ok
		} catch (Exception e) {
			throw new RuntimeException("Bad convention conformance for eventType: " + serializableEventType, e);
		}
		if (method != null) {
			registerEventToSerializableEventType(method.getReturnType(), serializableEventType);
		}
		serializableEventToAsEventMethodMap.put(serializableEventType, isAsEventMethod(method) ? method : null);
	}

	private void verifyAsEventMethod(Class<?> serializableEventType, Method asEventMethod) {
		try {
			if (asEventMethod.getReturnType().getMethod("asSerializable") == null) {
				throw new RuntimeException("Bad convention conformance for eventType: " + serializableEventType);
			}
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Bad convention conformance for serializable eventType: " + serializableEventType);
		} catch (SecurityException e) {
			throw new RuntimeException("Bad convention conformance for serializable eventType: " + serializableEventType);
		}
	}

	private boolean isAsEventMethod(Method method) {
		return method != null && method.getParameterTypes().length == 0;
	}

}
