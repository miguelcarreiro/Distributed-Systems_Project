package org.komparator.mediator.ws.it;

import java.util.List;

import org.junit.*;
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

public class AddToCartIT extends BaseIT {

	@Before
	public void setUp() throws InvalidText_Exception, BadProductId_Exception, BadProduct_Exception, SupplierClientException{
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
		
		ProductView product3 = new ProductView();
		product3.setId("Alameda");
		product3.setDesc("Torres ist");
		product3.setPrice(10);
		product3.setQuantity(1);
		client.createProduct(product3);
	}
	
	/*@Test
	public void success() throws InvalidCartId_Exception, NotEnoughItems_Exception, InvalidQuantity_Exception, InvalidItemId_Exception{
		ItemIdView itemId = new ItemIdView();
		itemId.setProductId("Ze");
		itemId.setSupplierId("T21_Supplier1");
		mediatorClient.addToCart("Aldi",itemId , 1);
		List<CartView> listCart = mediatorClient.listCarts();
		CartView cart = listCart.get(0);
		Assert.assertEquals("Ze", cart.getItems().get(0).getItem().getItemId().getProductId());
		Assert.assertEquals("Piriquito", cart.getItems().get(0).getItem().getDesc());
		Assert.assertEquals(1, cart.getItems().get(0).getQuantity());
	}*/
	
	@Test
	public void successExistentCart() throws InvalidCartId_Exception, NotEnoughItems_Exception, InvalidQuantity_Exception, InvalidItemId_Exception{
		ItemIdView itemId1 = new ItemIdView();
		itemId1.setProductId("Ze");
		itemId1.setSupplierId("T21_Supplier1");
		mediatorClient.addToCart("Aldi", itemId1 , 1);
		
		ItemIdView itemId2 = new ItemIdView();
		itemId2.setProductId("Tagus");
		itemId2.setSupplierId("T21_Supplier1");
		mediatorClient.addToCart("Aldi", itemId2 , 1);
		
		List<CartView> listCart = mediatorClient.listCarts();
		CartView cart = listCart.get(0);
		
		Assert.assertEquals("Tagus", cart.getItems().get(0).getItem().getItemId().getProductId());
		Assert.assertEquals("Ze", cart.getItems().get(1).getItem().getItemId().getProductId());

	}
	/*
	@Test(expected=InvalidCartId_Exception.class)
	public void cartIdNull() throws InvalidCartId_Exception, NotEnoughItems_Exception, InvalidQuantity_Exception, InvalidItemId_Exception{
		ItemIdView itemId = new ItemIdView();
		itemId.setProductId("Tagus");
		itemId.setSupplierId("T21_Supplier1");
		mediatorClient.addToCart(null,itemId , 1);
		
	}
	
	@Test(expected=InvalidItemId_Exception.class)
	public void itemIdNull() throws InvalidCartId_Exception, NotEnoughItems_Exception, InvalidQuantity_Exception, InvalidItemId_Exception{
		mediatorClient.addToCart("Lidel",null , 1);
	}
	
	@Test(expected=InvalidQuantity_Exception.class)
	public void quantityNegative() throws InvalidCartId_Exception, NotEnoughItems_Exception, InvalidQuantity_Exception, InvalidItemId_Exception{
		ItemIdView itemId = new ItemIdView();
		itemId.setProductId("Tagus");
		itemId.setSupplierId("T21_Supplier1");
		mediatorClient.addToCart("Contenent",itemId , -1);
		
	}
	
	@Test(expected=NotEnoughItems_Exception.class)
	public void insufficientQuantity() throws InvalidCartId_Exception, NotEnoughItems_Exception, InvalidQuantity_Exception, InvalidItemId_Exception{
		ItemIdView itemId = new ItemIdView();
		itemId.setProductId("Ze");
		itemId.setSupplierId("T21_Supplier1");
		mediatorClient.addToCart("Pingo azedo",itemId , 2);
		
	}*/
	
}
