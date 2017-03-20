package org.komparator.supplier.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.supplier.ws.*;

/**
 * Test suite
 */
public class BuyProductIT extends BaseIT {

	// static members

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() throws BadQuantity_Exception, InsufficientQuantity_Exception, BadProductId_Exception, BadProduct_Exception  {
		
		// clear remote service state before all tests
		client.clear();
		
		{
			ProductView product = new ProductView();
			product.setId("X1");
			product.setDesc("Basketball");
			product.setPrice(10);
			product.setQuantity(10);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("Y2");
			product.setDesc("Baseball");
			product.setPrice(20);
			product.setQuantity(20);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("Z3");
			product.setDesc("Soccer ball");
			product.setPrice(30);
			product.setQuantity(30);
			client.createProduct(product);
		}
		
	}

	@AfterClass
	public static void oneTimeTearDown() {
		// clear remote service state after all tests
		client.clear();
	}

	// members

	// initialization and clean-up for each test
	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	// tests
	// assertEquals(expected, actual);

	// public String buyProduct(String productId, int quantity)
	// throws BadProductId_Exception, BadQuantity_Exception,
	// InsufficientQuantity_Exception {

	// bad input tests

	@Test(expected = BadQuantity_Exception.class)
		public void zeroQuantity() throws BadQuantity_Exception, BadProductId_Exception, InsufficientQuantity_Exception{
		
			client.buyProduct("Z3",0);
		}
	
	@Test(expected = BadQuantity_Exception.class)
		public void negativeQuantity() throws BadQuantity_Exception, BadProductId_Exception, InsufficientQuantity_Exception{
		
			client.buyProduct("Z3",-4);
		}
	
	@Test(expected = BadProductId_Exception.class)
		public void nullStringId() throws BadQuantity_Exception, BadProductId_Exception, InsufficientQuantity_Exception{
		
			client.buyProduct(null,5);
	}
	
	@Test(expected = BadProductId_Exception.class)
		public void emptyStringId() throws BadQuantity_Exception, BadProductId_Exception, InsufficientQuantity_Exception{
	
			client.buyProduct("",2);
		}
	
	@Test(expected = BadProductId_Exception.class)
		public void stringIdWithSpaces() throws BadQuantity_Exception, BadProductId_Exception, InsufficientQuantity_Exception{
		
			client.buyProduct("ol a",2);
	}
	 
	// main tests
	
	@Test(expected = InsufficientQuantity_Exception.class)
	public void quantityAboveLimit() throws BadQuantity_Exception, BadProductId_Exception, InsufficientQuantity_Exception{
		
		client.buyProduct("Z3",31);
	}
	
	@Test(expected = InsufficientQuantity_Exception.class)
	public void quantityIsDownToZero() throws BadQuantity_Exception, BadProductId_Exception, InsufficientQuantity_Exception{
		
		client.buyProduct("Y2",20);
		client.buyProduct("Y2",1);
	}
	
	@Test(expected = BadProductId_Exception.class)
	public void productNotExists() throws BadQuantity_Exception, BadProductId_Exception, InsufficientQuantity_Exception{
		client.buyProduct("ZZ", 10);
	}
	
	@Test
	public void success () throws BadQuantity_Exception, BadProductId_Exception, InsufficientQuantity_Exception{
		
		client.buyProduct("X1", 3);
		assertEquals(7 , client.getProduct("X1").getQuantity() );
		
	}	

	
}
