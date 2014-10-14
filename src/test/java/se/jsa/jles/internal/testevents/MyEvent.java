package se.jsa.jles.internal.testevents;


public class MyEvent {
	private int num;
	
	public MyEvent() {
		// for jles
	}

	public MyEvent(int num) {
		this.num = num;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}
	
	@Override
	public boolean equals(Object arg0) {
		if (!arg0.getClass().equals(MyEvent.class)) {
			return false;
		}
		MyEvent other = (MyEvent) arg0;
		return num == other.num;
	}
	
	@Override
	public int hashCode() {
		return num;
	}

	@Override
	public String toString() {
		return "MyEvent [num=" + num + "]";
	}
}
