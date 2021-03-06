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

public class NonSerializableEvent {

	private final Name name;
	private final Date date;

	public NonSerializableEvent(Name name, Date date) {
		this.name = Objects.requireNonNull(name);
		this.date = Objects.requireNonNull(date);
	}

	public Name getName() {
		return name;
	}

	public Date getDate() {
		return date;
	}

	public SerializableEventV2 asSerializable() {
		return new SerializableEventV2(this);
	}

	@Override
	public String toString() {
		return "NonSerializableEvent [name=" + name + ", date=" + date + "]";
	}

	public static class SerializableEventV1 {
		private String name;

		public SerializableEventV1() {
			// for EventStore
		}

		public SerializableEventV1(NonSerializableEvent event) {
			name = event.getName().toString();
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public NonSerializableEvent asEvent() {
			return new NonSerializableEvent(Name.valueOf(name), new Date(0L));
		}
	}

	public static class SerializableEventV2 {
		private String name;
		private long date;

		public SerializableEventV2() {
			// for EventStore
		}

		public SerializableEventV2(NonSerializableEvent event) {
			name = event.getName().toString();
			date = event.getDate().getTime();
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public long getDate() {
			return date;
		}

		public void setDate(long date) {
			this.date = date;
		}

		public NonSerializableEvent asEvent() {
			return new NonSerializableEvent(Name.valueOf(name), new Date(date));
		}
	}

}
