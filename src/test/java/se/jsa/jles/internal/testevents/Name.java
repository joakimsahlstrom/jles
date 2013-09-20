package se.jsa.jles.internal.testevents;

import se.jsa.jles.internal.util.Objects;

public class Name {

	private final String name;

	public Name(String name) {
		this.name = Objects.requireNonNull(name);
	}

	public String getName() {
		return name;
	}

	public static Name valueOf(String name) {
		return new Name(name);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Name
				&& ((Name)obj).name.equals(this.name);
	}

}
