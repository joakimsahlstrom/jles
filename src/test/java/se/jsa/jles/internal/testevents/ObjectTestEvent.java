package se.jsa.jles.internal.testevents;


public class ObjectTestEvent {
	private String name;
	private Long id;
	private Boolean first;

	public ObjectTestEvent() {
	}

	public ObjectTestEvent(String name, Long id, Boolean first) {
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getFirst() {
		return first;
	}

	public void setFirst(Boolean first) {
		this.first = first;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ObjectTestEvent) {
			ObjectTestEvent other = (ObjectTestEvent) obj;
			return name.equals(other.name)
					&& id.equals(other.id)
					&& first.equals(other.first);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return ((name.hashCode() * 31 + id.hashCode()) * 31 + (first ? 1 : 0));
	}

	@Override
	public String toString() {
		return "TestEvent [name=" + name + ", id=" + id + ", first="
				+ first + "]";
	}
}