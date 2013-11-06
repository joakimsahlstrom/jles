package se.jsa.jles;

import se.jsa.jles.internal.FieldConstraint;
import se.jsa.jles.internal.EventId;
import se.jsa.jles.internal.TypedEventRepo;


public abstract class Match {

	public static Match MATCH_ALL = new Match() {
		@Override
		public Iterable<EventId> buildFilteringIterator(TypedEventRepo eventRepo) {
			return eventRepo.getIterator(FieldConstraint.noConstraint());
		}
	};

	public abstract Iterable<EventId> buildFilteringIterator(TypedEventRepo eventRepo);

}
