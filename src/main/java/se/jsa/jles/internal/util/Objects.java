package se.jsa.jles.internal.util;


public class Objects {

	private Objects() {
		//hide this
	}

	public static <T> T requireNonNull(T indicies) {
		if (indicies == null) {
			throw new NullPointerException();
		}
		return indicies;
	}

}
