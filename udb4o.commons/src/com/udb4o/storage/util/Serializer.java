package com.udb4o.storage.util;

import java.io.*;

import com.udb4o.io.*;

public interface Serializer<T> {

	void serialize(NamedDataOutput out, T item) throws IOException;

	T deserialize(NamedDataInput in) throws IOException;

}
