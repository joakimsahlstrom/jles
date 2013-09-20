package se.jsa.jles.internal.fields;

import java.nio.ByteBuffer;

public class FieldSerializer {

	public int getSerializedStringLength(String string) {
		return 2 + string.getBytes().length;
	}

	public void putString(ByteBuffer output, String string) {
		if (string.getBytes().length > Short.MAX_VALUE) {
			throw new RuntimeException("String was " + string.length() + " bytes, max allowed is " + Short.MAX_VALUE);
		}
		output.putShort((short)string.getBytes().length);
		output.put(string.getBytes());
	}

	public String getString(ByteBuffer input) {
		short stringLength = input.getShort();
		byte[] stringData = new byte[stringLength];
		input.get(stringData);

		return new String(stringData);
	}

}
