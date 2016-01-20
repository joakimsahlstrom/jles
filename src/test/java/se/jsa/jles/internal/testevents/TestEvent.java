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


public class TestEvent {
	private String name;
	private long id;
	private boolean first;

	public TestEvent() {
	}

	public TestEvent(String name, long id, boolean first) {
		this.name = name;
		this.id = id;
		this.first = first;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean getFirst() {
		return first;
	}

	public void setFirst(boolean first) {
		this.first = first;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TestEvent) {
			TestEvent other = (TestEvent) obj;
			return name.equals(other.name)
					&& id == other.id
					&& first == other.first;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (int) ((name.hashCode() * 31 + id) * 31 + (first ? 1 : 0));
	}

	@Override
	public String toString() {
		return "TestEvent [name=" + name + ", id=" + id + ", first="
				+ first + "]";
	}
}