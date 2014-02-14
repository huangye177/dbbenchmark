package org.itc.model;

public abstract class DataInteroperator {

	public abstract String interoperate(String originalSQL, DBType dbtype, OperationType operType);
	
}
