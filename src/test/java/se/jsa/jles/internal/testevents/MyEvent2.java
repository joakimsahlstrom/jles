package se.jsa.jles.internal.testevents;

public class MyEvent2 {
	private long num;
	
	public MyEvent2() {
		// for jles
	}

	public MyEvent2(long num) {
		this.num = num;
	}

	public long getNum() {
		return num;
	}

	public void setNum(long num) {
		this.num = num;
	}
	
	@Override
	public boolean equals(Object arg0) {
		if (!arg0.getClass().equals(MyEvent2.class)) {
			return false;
		}
		MyEvent2 other = (MyEvent2) arg0;
		return num == other.num;
	}
	
	@Override
	public int hashCode() {
		return (int)num;
	}

	@Override
	public String toString() {
		return "MyEvent2 [num=" + num + "]";
	}

}
