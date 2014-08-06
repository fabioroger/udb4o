package com.udb4o.db4o.test;

import com.udb4o.storage.util.*;

public interface ClassMetadata<T> {

	Serializer<T> serializer();

}
