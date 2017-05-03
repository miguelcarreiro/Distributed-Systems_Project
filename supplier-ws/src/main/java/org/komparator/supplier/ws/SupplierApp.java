package org.komparator.supplier.ws;

import org.komparator.security.handler.SupplierSecurityHandler;

/** Main class that starts the Supplier Web Service. */
public class SupplierApp {

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length == 0 || args.length == 2) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + SupplierApp.class.getName() + " wsURL OR uddiURL wsName wsURL");
			return;
		}
		String uddiURL = null;
		String wsName = null;
		String wsURL = null;
	
		SupplierEndpointManager endpoint = null;
		
		
		
		if (args.length == 1) {
			wsURL = args[0];
			endpoint = new SupplierEndpointManager(wsURL);
		} else if (args.length >= 3) {
			
			uddiURL = args[0];
			wsName = args[1];
			wsURL = args[2];
			endpoint = new SupplierEndpointManager(uddiURL, wsName, wsURL);
			endpoint.setVerbose(true);
			SupplierSecurityHandler.senderName = wsName;
		}

		try {
			endpoint.start();
			endpoint.awaitConnections();
		} finally {
			endpoint.stop();
		}

	}

}
