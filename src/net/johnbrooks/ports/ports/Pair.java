package net.johnbrooks.ports.ports;

public class Pair<A,B> {
	private A key;
	private B value;
	public Pair(A a ,B b) {
		key = a;
		value = b;
	}
	public void setKey(A o) {
		this.key = o;
	}
	public void setValue(B o){
		this.value = o;
	}
	public A getKey() {
		return this.key;
	}
	public B getValue() {
		return this.value;
	}

}
