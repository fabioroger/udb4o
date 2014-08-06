package com.udb4o.objectrepository;

public interface DataType extends Type {

	DataField addField(String name, Type fieldType);

	DataObject createInstance();

}
