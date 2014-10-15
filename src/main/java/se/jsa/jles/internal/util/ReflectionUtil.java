package se.jsa.jles.internal.util;

import java.lang.reflect.Method;

public class ReflectionUtil {

	public static Method getPropertyRetrieveMethod(Class<?> eventType, String fieldName) {
		for (Method method : eventType.getMethods()) {
			if ((method.getName().equals("get" + fieldName) || method.getName().equals("has" + fieldName) || method.getName().equals("is" + fieldName))
					&& method.getParameterTypes().length == 0) {
				return method;
			}
		}
		throw new NoSuchMethodError("Could not find property retrieve method for field " + fieldName + " for type " + eventType);
	}
	
}
