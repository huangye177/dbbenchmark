package org.itc.model;

public class DataInteroperatorFactory {

	public static DataInteroperator buildDataInteroperator(InteroperateType interoperatorType) {
		
		DataInteroperator interoperator = null;
		
		switch (interoperatorType) {
			case GEOMONI_BASIC: {
				interoperator = null;
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
