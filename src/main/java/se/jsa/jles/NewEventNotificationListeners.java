package se.jsa.jles;

import java.util.ArrayList;
import java.util.List;

public class NewEventNotificationListeners {

	public interface NewEventNotificationListener {
		void onNewEvent();
	}

	private final List<NewEventNotificationListener> listeners = new ArrayList<NewEventNotificationListener>();

	public void registerListener(NewEventNotificationListener listener) {
		this.listeners.add(listener);
	}

	public void onNewEvent() {
		for (NewEventNotificationListener listener : listeners) {
			listener.onNewEvent();
		}
	}

}
