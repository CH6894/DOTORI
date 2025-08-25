package com.pingu.DOTORI.common;

public enum TestUserResCode {
	SUCCESS(0);
	
	private final int value;
	
	private void ResCode((int value) {
		this.value = value;
	}
	
	public int value() {
		return value;
	}
}
