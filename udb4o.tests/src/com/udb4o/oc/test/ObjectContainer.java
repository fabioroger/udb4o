package com.udb4o.oc.test;

import java.io.*;
import java.util.*;

public interface ObjectContainer extends Closeable, Flushable {

	void store(Object object);

	<T> Collection<T> query(Class<T> clazz);

	void commit();

}
