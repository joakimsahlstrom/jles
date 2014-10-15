package se.jsa.jles.internal.util;

import java.util.HashMap;
import java.util.Map;

public class Primitives {

	public final static Map<Class<?>, Class<?>> map = new HashMap<Class<?>, Class<?>>();
	static {
	    map.put(boolean.class, Boolean.class);
	    map.put(byte.class, Byte.class);
	    map.put(short.class, Short.class);
	    map.put(char.class, Character.class);
	    map.put(int.class, Integer.class);
	    map.put(long.class, Long.class);
	    map.put(float.class, Float.class);
	    map.put(double.class, Double.class);
	}

	public static Class<?> asBoxedPrimitive(Class<?> returnType) {
		if (returnType.isPrimitive()) {
			return map.get(returnType);
		}
		return returnType;
	}
	
}
