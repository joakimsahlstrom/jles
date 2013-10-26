package se.jsa.jles.internal.util;

import java.nio.ByteBuffer;
import java.util.List;

import se.jsa.jles.internal.fields.EventField;

public class NullFieldMap {
	private final List<EventField> fields;
	private final byte[] mask;

	public NullFieldMap(List<EventField> fields, byte[] mask) {
		this.fields = Objects.requireNonNull(fields);
		this.mask = Objects.requireNonNull(mask);
	}

	public byte[] getMask() {
		return mask;
	}

	public boolean isFieldNull(EventField field) {
		if (field.getFieldType().isPrimitive()) {
			return false;
		}

		int pos = getPos(field);
		return (mask[pos / 8] & 1 << pos % 8) != 0;
	}

	private int getPos(EventField field) {
		int pos = 0;
		for (int fieldIndex = 0; fieldIndex < fields.size(); fieldIndex++) {
			if (fields.get(fieldIndex).getFieldType().isPrimitive()) {
				continue;
			}

			if (fields.get(fieldIndex).equals(field)) {
				return pos;
			}
			pos++;
		}
		throw new RuntimeException("Field " + field + " not in " + fields);
	}

	public static int getSizeInBytes(List<EventField> fields) {
		int numNullableFields = 0;
		for (EventField field : fields) {
			if (!field.getFieldType().isPrimitive()) {
				numNullableFields++;
			}
		}
		return (int) Math.ceil(numNullableFields / 8.0);
	}

	public static NullFieldMap buildFromEvent(List<EventField> fields, Object event) {
		byte[] mask = new byte[getSizeInBytes(fields)];
		int fieldCounter = 0;
		for (EventField field : fields) {
			if (!field.getFieldType().isPrimitive()) {
				if (field.getValue(event) == null) {
					mask[fieldCounter/8] = (byte) (mask[fieldCounter / 8] | 1 << fieldCounter % 8);
				}
				fieldCounter++;
			}
		}
		return new NullFieldMap(fields, mask);
	}

	public static NullFieldMap buildFromBuffer(List<EventField> fields, ByteBuffer buffer) {
		byte[] mask = new byte[getSizeInBytes(fields)];
		if (mask.length > 0) {
			buffer.get(mask);
		}
		return new NullFieldMap(fields, mask);
	}
}
