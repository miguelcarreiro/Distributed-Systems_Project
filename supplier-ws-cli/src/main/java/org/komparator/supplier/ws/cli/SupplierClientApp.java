package org.komparator.supplier.ws.cli;

import org.komparator.security.handler.SupplierSecurityHandler;

/** Main class that starts the Supplier Web Service client. */
public class SupplierClientApp {

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length < 1) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + SupplierClientApp.class.getName() + " wsURL");
			return;
		}
		String wsURL = args[0];

		// Create client
		System.out.printf("Creating client for server at %s%n", wsURL);
		SupplierClient client = new SupplierClient(wsURL);

		// the following remote invocations are just basic examples
		// the actual tests are made using JUnit
		SupplierSecurityHandler.senderName = "T21_Mediator";
		System.out.println("Invoke ping()...");
		String result = client.ping("client");
		System.out.print("Result: ");
		System.out.println(result);

	}

}
