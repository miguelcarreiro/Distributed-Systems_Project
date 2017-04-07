package org.komparator.mediator.ws.it;

import java.util.List;

import org.junit.*;
import org.komparator.mediator.ws.InvalidItemId_Exception;
import org.komparator.mediator.ws.ItemView;
import org.komparator.mediator.ws.cli.MediatorClient;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;

public class GetItemsIT extends BaseIT {

	@Before
	public void oneTimeSetUp() throws BadProductId_Exception, BadProduct_Exception, SupplierClientException, InvalidItemId_Exception{
		SupplierClient client = new SupplierClient("http://localhost:8081/supplier-ws/endpoint");
		client.clear();
		ProductView product = new ProductView();
		product.setId("Ze");
		product.setDesc("Piriquito");
		product.setPrice(7);
		product.setQuantity(38);
		client.createProduct(product);
	}
	
	@Test
	public void success() throws InvalidItemId_Exception{
		List<ItemView>productList = mediatorClient.getItems("Ze");
		Assert.assertEquals("Ze", productList.get(0).getItemId().getProductId());
		Assert.assertEquals("Piriquito", productList.get(0).getDesc());
		Assert.assertEquals(7, productList.get(0).getPrice());
	}
	
	@Test(expected = InvalidItemId_Exception.class)
	public void wrongId() throws InvalidItemId_Exception{
		List<ItemView>productList = mediatorClient.getItems("Joao");
	}
	
	@Test(expected = InvalidItemId_Exception.class)
	public void emptyId() throws InvalidItemId_Exception{
		List<ItemView>productList = mediatorClient.getItems("");

	}
	
	@Test(expected = InvalidItemId_Exception.class)
	public void blankId() throws InvalidItemId_Exception{
		List<ItemView>productList = mediatorClient.getItems(" ");

	}
	

	@After
	public void clean(){
		mediatorClient.clear();
	}
	
}
