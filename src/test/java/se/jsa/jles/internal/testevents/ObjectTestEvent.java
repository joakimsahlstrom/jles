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


public class ObjectTestEvent {
	private String name;
	private Long id;
	private Boolean first;

	public ObjectTestEvent() {
	}

	public ObjectTestEvent(String name, Long id, Boolean first) {
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getFirst() {
		return first;
	}

	public void setFirst(Boolean first) {
		this.first = first;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ObjectTestEvent) {
			ObjectTestEvent other = (ObjectTestEvent) obj;
			return eq(name, other.name)
					&& eq(id, other.id)
					&& eq(first, other.first);
		}
		return false;
	}

	private boolean eq(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	@Override
	public int hashCode() {
		return (name.hashCode() * 31 + id.hashCode()) * 31 + (first ? 1 : 0);
	}

	@Override
	public String toString() {
		return "TestEvent [name=" + name + ", id=" + id + ", first="
				+ first + "]";
	}
}