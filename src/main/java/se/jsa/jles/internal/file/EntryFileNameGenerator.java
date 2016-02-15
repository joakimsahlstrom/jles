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
package se.jsa.jles.internal.file;

import se.jsa.jles.internal.EventTypeId;

public class EntryFileNameGenerator {

	public String getEventTypeIndexFileName() {
		return "events.if";
	}

	public String getEventFileName() {
		return "events.ef";
	}

	public String getEventDefintionsFileName() {
		return "events.def";
	}

	public String getEventFieldIndexFileName(EventTypeId eventTypeId, String fieldName) {
		return "events_" + eventTypeId.toLong() + "_" + fieldName + ".if";
	}

	public String getEventIndexFileName(EventTypeId eventTypeId) {
		return "events_" + eventTypeId.toLong() + ".if";
	}
	
	public String getMultiFileIndexName(EventTypeId eventTypeId) {
		return "mfi_events_" + eventTypeId.toLong() + ".if";
	}
}