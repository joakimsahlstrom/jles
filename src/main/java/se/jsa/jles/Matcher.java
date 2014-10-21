package se.jsa.jles;

import se.jsa.jles.internal.FieldConstraint;
import se.jsa.jles.internal.EventId;
import se.jsa.jles.internal.TypedEventRepo;

/**
 * 
 * @author joasah
 * @deprecated Will be removed once EventQuery2 becomes EventQuery!
 */
@Deprecated
public abstract class Matcher {

	public static Matcher MATCH_ALL = new Matcher() {
		@Override
		public Iterable<EventId> buildFilteringIterator(TypedEventRepo eventRepo) {
			return eventRepo.getIterator(FieldConstraint.noConstraint());
		}
	};

	public abstract Iterable<EventId> buildFilteringIterator(TypedEventRepo eventRepo);

}
