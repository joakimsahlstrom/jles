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
package se.jsa.jles.internal.testevents;

import java.util.Date;

import se.jsa.jles.internal.util.Objects;

public class EventWithNonStaticSerializable {
	private final Name name;
	private final Date date;

	public EventWithNonStaticSerializable(Name name, Date date) {
		this.name = Objects.requireNonNull(name);
		this.date = Objects.requireNonNull(date);
	}

	public Name getName() {
		return name;
	}

	public Date getDate() {
		return date;
	}

	public SerializableEventV1 asSerializable() {
		return new SerializableEventV1(this);
	}

	@Override
	public String toString() {
		return "NonSerializableEvent [name=" + name + ", date=" + date + "]";
	}

	public class SerializableEventV1 {
		private String name;

		public SerializableEventV1() {
			// for EventStore
		}

		public SerializableEventV1(EventWithNonStaticSerializable event) {
			name = event.getName().toString();
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public EventWithNonStaticSerializable asEvent() {
			return new EventWithNonStaticSerializable(Name.valueOf(name), new Date(0L));
		}
	}

}
