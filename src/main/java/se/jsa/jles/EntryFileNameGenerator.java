package se.jsa.jles;

class EntryFileNameGenerator {

	public String getEventTypeIndexFileName() {
		return "events.if";
	}

	public String getEventFileName() {
		return "events.ef";
	}

	public String getEventDefintionsFileName() {
		return "events.def";
	}

	public String getEventFieldIndexFileName(Long eventTypeId, String fieldName) {
		return "events_" + eventTypeId + "_" + fieldName + ".if";
	}

	public String getEventIndexFileName(Long eventTypeId) {
		return "events_" + eventTypeId + ".if";
	}
	
}
