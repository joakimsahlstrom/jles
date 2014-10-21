package se.jsa.jles;

import java.util.ArrayList;
import java.util.List;

public class TestUtil {

	public static List<Object> collect(Iterable<Object> source) {
		List<Object> result = new ArrayList<Object>();
		for (Object o : source) {
			result.add(o);
		}
		return result;
	}
	
}
