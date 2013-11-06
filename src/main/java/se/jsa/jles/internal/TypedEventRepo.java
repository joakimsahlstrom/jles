package se.jsa.jles.internal;

public interface TypedEventRepo {

	public Iterable<EventId> getIterator(FieldConstraint constraint);

	public Object readEvent(EventId eventIndex);

	public Object readEventField(EventId eventIndex, String fieldName);

	public IndexType getIndexing(String fieldName);

}
