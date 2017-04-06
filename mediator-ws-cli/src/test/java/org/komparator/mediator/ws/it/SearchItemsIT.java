package org.komparator.mediator.ws.it;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.*;
import org.komparator.mediator.ws.InvalidItemId_Exception;
import org.komparator.mediator.ws.InvalidText_Exception;
import org.komparator.mediator.ws.ItemView;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;

public class SearchItemsIT extends BaseIT {
	@Before
	public void setUp() throws InvalidText_Exception, BadProductId_Exception, BadProduct_Exception, SupplierClientException{
		SupplierClient client = new SupplierClient("http://localhost:8081/supplier-ws/endpoint");
		client.clear();
		ProductView product = new ProductView();
		product.setId("Ze");
		product.setDesc("Piriquito");
		product.setPrice(7);
		product.setQuantity(38);
		client.createProduct(product);
		
		ProductView product2 = new ProductView();
		product2.setId("Tagus");
		product2.setDesc("Bar azul ist");
		product2.setPrice(3);
		product2.setQuantity(1);
		client.createProduct(product2);
		
		ProductView product3 = new ProductView();
		product3.setId("Alameda");
		product3.setDesc("Torres ist");
		product3.setPrice(10);
		product3.setQuantity(2);
		client.createProduct(product3);
	}
	
	@Test
	public void success() throws InvalidText_Exception{
		List<ItemView>productList = mediatorClient.searchItems("Piriquito");
		Assert.assertEquals("Ze", productList.get(0).getItemId().getProductId());
	}
	
	@Test
	public void sameDescription() throws InvalidText_Exception{
		List<ItemView>productList = mediatorClient.searchItems("ist");
		Assert.assertEquals("Alameda", productList.get(0).getItemId().getProductId());
		Assert.assertEquals("Tagus", productList.get(1).getItemId().getProductId());
	}
	
	@Test(expected=InvalidText_Exception.class)
	public void nullDescription() throws InvalidText_Exception{
		List<ItemView>productList = mediatorClient.searchItems(null);
	}
	
	@Test(expected=InvalidText_Exception.class)
	public void emptyDescription() throws InvalidText_Exception{
		List<ItemView>productList = mediatorClient.searchItems("");
	}
	
	@Test
	public void wrongDescription() throws InvalidText_Exception{
		List<ItemView>productList = mediatorClient.searchItems("IPL");
		Assert.assertTrue(productList.isEmpty());

	}
}
