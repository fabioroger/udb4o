package com.udb4o.objectrepository;

public interface Transaction {

	DataType createDataType(String name);

	DataType getDataType(String typeName);

}
