package com.udb4o.table.impl;

import java.lang.reflect.*;
import java.util.*;

import com.udb4o.table.*;

public class HeapIndexableList<T> implements IndexableList<T> {
	
	interface ITableIndex<T> {
		
		void add(T item);

		void clear();

		void remove(T item);

		void addAll(Collection<? extends T> c);

		void removeAll(Collection<?> c);
		
	}
	
	class TableIndex<FT> implements Index<T, FT>, ITableIndex<T> {

		private final IndexKeyGetter<T, FT> index;
		private Map<FT, List<T>> map = new HashMap<FT, List<T>>();

		public TableIndex(IndexKeyGetter<T, FT> index) {
			this.index = index;
		}

		public void add(T item) {
			FT key = index.key(item);
			List<T> list = map.get(key);
			if (list == null) {
				list = new ArrayList<T>();
				map.put(key, list);
			}
			list.add(item);
		}

		@Override
		public List<T> getAll(FT key) {
			return map.get(key);
		}

		public void clear() {
			map.clear();
		}

		public void remove(T item) {
			FT key = index.key(item);
			list = map.get(key);
			if (list == null) {
				return;
			}
			list.remove(item);
			if (list.isEmpty()) {
				map.remove(key);
			}
		}

		@Override
		public void addAll(Collection<? extends T> c) {
			for(T t : c) {
				add(t);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void removeAll(Collection<?> c) {
			for(Object t : c) {
				remove((T) t);
			}
		}

		@Override
		public T first(FT key) {
			List<T> all = map.get(key);
			if (all.isEmpty()) {
				return null;
			}
			return all.get(0);
		}

		@Override
		public void drop() {
			indexes.remove(this);
		}
		
	}

	private List<T> list = new ArrayList<T>();
	private List<TableIndex<?>> indexes = new ArrayList<TableIndex<?>>();
	private boolean updatingIndex = true;
	private ITableIndex<T> indexesProxy;

	{
		init();
	}
	
	@SuppressWarnings("unchecked")
	private void init() {
		indexesProxy = (ITableIndex<T>) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{ITableIndex.class}, new InvocationHandler() {
			
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				for(ITableIndex<T> ti : indexes) {
					method.invoke(ti, args);
				}
				return null;
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see com.udb4o.util.IIndexableList#isUpdatingIndex()
	 */
	@Override
	public boolean isUpdatingIndex() {
		return updatingIndex;
	}

	/* (non-Javadoc)
	 * @see com.udb4o.util.IIndexableList#setUpdatingIndex(boolean)
	 */
	@Override
	public void setUpdatingIndex(boolean updateIndexes) {
		boolean b = isUpdatingIndex();
		this.updatingIndex = updateIndexes;
		if (!b && updateIndexes) {
			indexesProxy.clear();
			for(T t:list) {
				indexesProxy.add(t);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.udb4o.util.IIndexableList#createIndex(com.udb4o.util.IndexKeyGetter)
	 */
	@Override
	public <FT> Index<T, FT> createIndex(IndexKeyGetter<T, FT> index) {
		TableIndex<FT> e = new TableIndex<FT>(index);
		indexes.add(e);
		if (isUpdatingIndex()) {
			for(T item : list) {
				e.add(item);
			}
		}
		return e;
	}
	
	/* (non-Javadoc)
	 * @see com.udb4o.util.IIndexableList#add(T)
	 */
	@Override
	public boolean add(T item) {
		boolean added = list.add(item);
		if (added && isUpdatingIndex()) {
			indexesProxy.add(item);
		}
		return added;
	}
	
	/* (non-Javadoc)
	 * @see com.udb4o.util.IIndexableList#remove(T)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object item) {
		boolean removed = list.remove(item);
		if (removed && isUpdatingIndex()) {
			indexesProxy.remove((T) item);
		}
		return removed;
	}

	public int size() {
		return list.size();
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

	public boolean contains(Object o) {
		return list.contains(o);
	}

	public Iterator<T> iterator() {
		return list.iterator();
	}

	public Object[] toArray() {
		return list.toArray();
	}

	public <E> E[] toArray(E[] a) {
		return list.toArray(a);
	}

	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	public boolean addAll(Collection<? extends T> c) {
		boolean added = list.addAll(c);
		if (added && isUpdatingIndex()) {
			indexesProxy.addAll(c);
		}
		return added;
	}

	public boolean addAll(int index, Collection<? extends T> c) {
		boolean added = list.addAll(index, c);
		if (added && isUpdatingIndex()) {
			indexesProxy.addAll(c);
		}
		return added;
	}

	public boolean removeAll(Collection<?> c) {
		boolean removed = list.removeAll(c);
		if (removed && isUpdatingIndex()) {
			indexesProxy.removeAll(c);
		}
		return removed;
	}

	public boolean retainAll(Collection<?> c) {
		return list.retainAll(c);
	}

	public void clear() {
		list.clear();
	}

	public boolean equals(Object o) {
		return list.equals(o);
	}

	public int hashCode() {
		return list.hashCode();
	}

	public T get(int index) {
		return list.get(index);
	}

	public T set(int index, T element) {
		T set = list.set(index, element);
		if (isUpdatingIndex()) {
			if (set != null) {
				indexesProxy.remove(set);
			}
			indexesProxy.add(element);
		}
		return set;
	}

	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	public ListIterator<T> listIterator() {
		return list.listIterator();
	}

	public ListIterator<T> listIterator(int index) {
		return list.listIterator(index);
	}

	public List<T> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	@Override
	public void add(int index, T element) {
		list.add(element);
		if (isUpdatingIndex()) {
			indexesProxy.add(element);
		}
	}

	@Override
	public T remove(int index) {
		T removed = list.remove(index);
		if (removed != null && isUpdatingIndex()) {
			indexesProxy.remove(removed);
		}
		return removed;
	}


}