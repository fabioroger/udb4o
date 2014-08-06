package com.udb4.agent;

import java.lang.instrument.Instrumentation;

import com.udb4o.enhancer.Udb4oClassTransformer;

public class EnhancerAgent {

	public static void premain(String agentArguments, Instrumentation instrumentation) {
		instrumentation.addTransformer(new Udb4oClassTransformer());
	}

}
