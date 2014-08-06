package com.udb4o.objectrepository;

public class PrimitiveType implements Type {

	public final static PrimitiveType STRING = new StringType();

	private final String name;

	public PrimitiveType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
