package org.komparator.supplier.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.supplier.ws.*;

/**
 * Test suite
 */
public class SearchProductsIT extends BaseIT {

	// static members

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() throws BadText_Exception, BadProductId_Exception, BadProduct_Exception  {
		
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
		
		{
			ProductView product = new ProductView();
			product.setId("B4");
			product.setDesc("Soccer ball");
			product.setPrice(20);
			product.setQuantity(10);
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

	// public List<ProductView> searchProducts(String descText) throws
	// BadText_Exception

	// bad input tests

	@Test(expected = BadText_Exception.class)
		public void nullDescription() throws BadText_Exception{
			client.searchProducts(null);
		}
	
	@Test(expected = BadText_Exception.class)
		public void emptyDescription() throws BadText_Exception{
			
			client.searchProducts("");
		}
	
	
	// main tests

	@Test
		public void descriptionNotExists() throws BadText_Exception{
		
			List<ProductView> result=  client.searchProducts("Tennis");
			
			assertTrue(result.isEmpty());
	
		}
	
	@Test
		public void wrongDescription() throws BadText_Exception{
		
			List<ProductView> result=  client.searchProducts("X1");
			
			assertTrue(result.isEmpty());
		}

	
	@Test
		public void sameDescriptions() throws BadText_Exception, BadProductId_Exception, BadProduct_Exception{
		
			List<ProductView> result=  client.searchProducts("Soccer ball");
			
			assertEquals(result.get(0).getDesc(), result.get(1).getDesc());
					
		}
	
	@Test
		public void sameDescriptionsWithFiltration () throws BadText_Exception, BadProductId_Exception, BadProduct_Exception {

			ProductView product0 = new ProductView();
			product0.setId("C0");
			product0.setDesc("Handball");
			product0.setPrice(15);
			product0.setQuantity(30);
			client.createProduct(product0);
			

			ProductView product1 = new ProductView();
			product1.setId("C1");
			product1.setDesc("Handball");
			product1.setPrice(3);
			product1.setQuantity(7);
			client.createProduct(product1);
			

			ProductView product2 = new ProductView();
			product2.setId("C2");
			product2.setDesc("Swimming");
			product2.setPrice(34);
			product2.setQuantity(17);
			client.createProduct(product2);
			

			ProductView product3 = new ProductView();
			product3.setId("C3");
			product3.setDesc("Volleyball");
			product3.setPrice(2);
			product3.setQuantity(11);
			client.createProduct(product3);
			
			ProductView product4 = new ProductView();
			product4.setId("C4");
			product4.setDesc("Swimming");
			product4.setPrice(23);
			product4.setQuantity(40);
			client.createProduct(product4);
			
			List<ProductView> result=  client.searchProducts("Handball");
			
			assertEquals(result.get(0).getDesc(), result.get(1).getDesc());
			
	}
	
	@Test
		public void successBaseball () throws BadText_Exception, BadProductId_Exception, BadProduct_Exception{
		
			List<ProductView> resultBaseball=  client.searchProducts("Baseball");
			
			assertEquals("Y2", resultBaseball.get(0).getId());
			
		}
	
	@Test
		public void successSoccer () throws BadText_Exception, BadProductId_Exception, BadProduct_Exception{
		
			List<ProductView> resultSoccer=  client.searchProducts("Soccer ball");
			
			assertEquals("B4", resultSoccer.get(0).getId());
			
			assertEquals("Z3", resultSoccer.get(1).getId());
		}
	
	@Test
		public void successBasket () throws BadText_Exception, BadProductId_Exception, BadProduct_Exception{
				
			List<ProductView> resultBasket=  client.searchProducts("Basketball");
			
			assertEquals("X1", resultBasket.get(0).getId());
		}
	
}
