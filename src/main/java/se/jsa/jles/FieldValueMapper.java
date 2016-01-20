package se.jsa.jles;

public interface FieldValueMapper {
	Object map(Object value);

	public class IdentityMapper implements FieldValueMapper {
		@Override
		public Object map(Object value) {
			return value;
		}
	}
}
