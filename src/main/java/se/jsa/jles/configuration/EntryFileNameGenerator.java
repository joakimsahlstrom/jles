package se.jsa.jles.configuration;

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

}
