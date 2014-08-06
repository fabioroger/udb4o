package com.udb4o.test;

import java.util.*;

public interface DataContainer {

	Data createData(UUID key);
	Data getData(UUID key);

	void dispose();
	void flush();


}
