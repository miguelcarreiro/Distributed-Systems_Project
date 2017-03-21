package org.komparator.supplier.ws.it;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.supplier.ws.*;

/**
 * Test suite
 */
public class CreateProductIT extends BaseIT {

	// static members

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() {
		
		// clear remote service state before all tests
		client.clear();
	
		
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
	
	@Test(expected = BadProductId_Exception.class)
		public void idNullTest() throws BadQuantity_Exception, BadProductId_Exception, InsufficientQuantity_Exception, BadProduct_Exception{
		
			ProductView product = new ProductView();
			product.setId(null);
			product.setDesc("Soccer ball");
			product.setPrice(7);
			product.setQuantity(38);
			client.createProduct(product);
	}
	
	@Test(expected = BadProductId_Exception.class)
		public void idSpaceTest() throws BadQuantity_Exception, BadProductId_Exception, InsufficientQuantity_Exception, BadProduct_Exception{
		
			ProductView product = new ProductView();
			product.setId(" ");
			product.setDesc("Soccer ball");
			product.setPrice(23);
			product.setQuantity(12);
			client.createProduct(product);
	}
	
	@Test(expected = BadProduct_Exception.class)
		public void priceNegativeTest() throws BadQuantity_Exception, BadProductId_Exception, InsufficientQuantity_Exception, BadProduct_Exception{
		
			ProductView product = new ProductView();
			product.setId("X2");
			product.setDesc("Soccer ball");
			product.setPrice(-3);
			product.setQuantity(10);
			client.createProduct(product);
	}
	
	@Test(expected = BadProduct_Exception.class)
		public void quantityNegativeTest() throws BadQuantity_Exception, BadProductId_Exception, InsufficientQuantity_Exception, BadProduct_Exception{
		
			ProductView product = new ProductView();
			product.setId("X3");
			product.setDesc("Soccer ball");
			product.setPrice(20);
			product.setQuantity(-1);
			client.createProduct(product);
	}
	
	@Test(expected = BadProduct_Exception.class)
		public void nullProductTest() throws BadQuantity_Exception, BadProductId_Exception, InsufficientQuantity_Exception, BadProduct_Exception{
		
			ProductView product = null;
			client.createProduct(product);
		}
		

	 
	// main tests
	
}

