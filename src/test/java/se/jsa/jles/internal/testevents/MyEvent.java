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


public class MyEvent {
	private int num;

	public MyEvent() {
		// for jles
	}

	public MyEvent(int num) {
		this.num = num;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	@Override
	public boolean equals(Object arg0) {
		if (!arg0.getClass().equals(MyEvent.class)) {
			return false;
		}
		MyEvent other = (MyEvent) arg0;
		return num == other.num;
	}

	@Override
	public int hashCode() {
		return num;
	}

	@Override
	public String toString() {
		return "MyEvent [num=" + num + "]";
	}
}
