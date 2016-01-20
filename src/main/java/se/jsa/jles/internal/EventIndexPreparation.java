package se.jsa.jles.internal;


public interface EventIndexPreparation {
	EventFile getEventFile();
	IndexFile getEventTypeIndex();
	EventDefinitions getEventDefinitions();
	void schedule(Runnable runnable);
}