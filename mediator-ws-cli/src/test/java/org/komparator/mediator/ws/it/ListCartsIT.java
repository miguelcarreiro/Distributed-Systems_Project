package org.komparator.mediator.ws.it;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.komparator.mediator.ws.CartView;
import org.komparator.mediator.ws.InvalidCartId_Exception;
import org.komparator.mediator.ws.InvalidItemId_Exception;
import org.komparator.mediator.ws.InvalidQuantity_Exception;
import org.komparator.mediator.ws.InvalidText_Exception;
import org.komparator.mediator.ws.ItemIdView;
import org.komparator.mediator.ws.NotEnoughItems_Exception;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;

public class ListCartsIT extends BaseIT {
	@Before
	public void setUp() throws InvalidQuantity_Exception, NotEnoughItems_Exception, InvalidText_Exception, BadProductId_Exception, BadProduct_Exception, SupplierClientException, InvalidCartId_Exception, InvalidItemId_Exception{
		SupplierClient client = new SupplierClient("http://localhost:8081/supplier-ws/endpoint");
		client.clear();
		ProductView product = new ProductView();
		product.setId("Ze");
		product.setDesc("Piriquito");
		product.setPrice(7);
		product.setQuantity(1);
		client.createProduct(product);
		
		ProductView product2 = new ProductView();
		product2.setId("Tagus");
		product2.setDesc("Bar azul ist");
		product2.setPrice(3);
		product2.setQuantity(40);
		client.createProduct(product2);
		
		ItemIdView itemId1 = new ItemIdView();
		itemId1.setProductId("Ze");
		itemId1.setSupplierId("T21_Supplier1");
		mediatorClient.addToCart("Aldi", itemId1 , 1);
		
		ItemIdView itemId2 = new ItemIdView();
		itemId2.setProductId("Tagus");
		itemId2.setSupplierId("T21_Supplier1");
		mediatorClient.addToCart("Contenent", itemId2 , 1);
	}
	
	
	@Test
	public void success(){
		List<CartView> listCart = mediatorClient.listCarts();
		CartView cart1 = listCart.get(1);
		CartView cart2 = listCart.get(0);
		Assert.assertEquals("Contenent", cart1.getCartId());
		Assert.assertEquals("Contenent", cart2.getCartId());
	}
	
	
	
	
	@After
	public void clean(){
		mediatorClient.clear();
	}
}
