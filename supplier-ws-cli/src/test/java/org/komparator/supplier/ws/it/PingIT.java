package org.komparator.supplier.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Test suite
 */
public class PingIT extends BaseIT {

	// tests
	// assertEquals(expected, actual);

	// public String ping(String x)

	@Test
	public void pingEmptyTest() {
		assertNotNull(client.ping("test"));
	}
	
	@Test
	public void pingVerifyMsg() {
		
		String msg = client.ping("client");
		
		assertEquals(msg, "Hello client from Supplier");
	}

}
