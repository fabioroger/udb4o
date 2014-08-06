package com.udb4o.objectrepository;

public interface ClassAnalyzerVisitor {

	void visit(String name, String superName);

	void visitField(String name, String type);

}
