package org.itc.model;

public class DataInteroperatorFactory {

	public static IDataInteroperator buildDataInteroperator(InteroperateType interoperatorType) {
		
		IDataInteroperator interoperator = null;
		
		switch (interoperatorType) {
			case GEOMONI_BASIC: {
				interoperator = new InteroperatorGeomoniBasic();
				break;
			}
			case TUNNEL_BASIC: {
				interoperator = null;
				break;
			}
			default: {
				break;
			}
		}
		return interoperator;
	}
}
