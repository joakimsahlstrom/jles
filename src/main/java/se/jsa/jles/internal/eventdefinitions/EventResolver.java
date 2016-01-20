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

import se.jsa.jles.internal.EventDeserializer;
import se.jsa.jles.internal.EventSerializer;

public interface EventResolver {

	public void registerEventTypes(Class<?>[] eventTypes);

	/**
	 * Try to find a corresponding serializable event by some strategy. If that fails,
	 * return the incoming object
	 */
	public Object getSerializableEvent(Object event);

	/**
	 * @param eventTypes
	 * @return All types that are serializable versions of the incoming types
	 */
	public Class<?>[] getSerializableEventTypes(Class<?>[] eventTypes);

	public EventDeserializer wrapDeserializer(EventDeserializer eventDeserializer);

	public EventSerializer wrapSerializer(EventSerializer eventSerializer);

}