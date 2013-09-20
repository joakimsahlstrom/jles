package se.jsa.jles.internal;

public interface TypedEventRepo {

	public Iterable<EventIndex> getIterator(EventFieldConstraint constraint);

	public Object readEvent(EventIndex eventIndex);

	public Object readEventField(EventIndex eventIndex, String fieldName);

	public Indexing getIndexing(String fieldName);

}
