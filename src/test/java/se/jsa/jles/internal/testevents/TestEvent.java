package se.jsa.jles.internal.testevents;


public class TestEvent {
	private String name;
	private long id;
	private boolean first;

	public TestEvent() {
	}

	public TestEvent(String name, long id, boolean first) {
		this.name = name;
		this.id = id;
		this.first = first;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean getFirst() {
		return first;
	}

	public void setFirst(boolean first) {
		this.first = first;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TestEvent) {
			TestEvent other = (TestEvent) obj;
			return name.equals(other.name)
					&& id == other.id
					&& first == other.first;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (int) ((name.hashCode() * 31 + id) * 31 + (first ? 1 : 0));
	}

	@Override
	public String toString() {
		return "TestEvent [name=" + name + ", id=" + id + ", first="
				+ first + "]";
	}
}