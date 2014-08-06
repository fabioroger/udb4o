package com.udb4o;

import com.udb4o.io.*;

public interface Streamer {

	NamedDataOutput openOutput(boolean append);

	NamedDataInput openInput();

}
