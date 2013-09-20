package se.jsa.jles;

import se.jsa.jles.internal.EventFieldConstraint;
import se.jsa.jles.internal.EventIndex;
import se.jsa.jles.internal.TypedEventRepo;


public abstract class Match {

	public static Match MATCH_ALL = new Match() {
		@Override
		public Iterable<EventIndex> buildFilteringIterator(TypedEventRepo eventRepo) {
			return eventRepo.getIterator(new EventFieldConstraint());
		}
	};

	public abstract Iterable<EventIndex> buildFilteringIterator(TypedEventRepo eventRepo);

}
