package org.itc.model;

public abstract class IDataInteroperator {

	public abstract String interoperate(String originalSQL, DBType dbtype, OperationType operType);
	
}
