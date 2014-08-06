package com.udb4o.table;

import java.util.*;

public interface IndexableList<T> extends List<T> {

	<FT> Index<T, FT> createIndex(IndexKeyGetter<T, FT> index);

	boolean isUpdatingIndex();
	void setUpdatingIndex(boolean updateIndexes);
}
