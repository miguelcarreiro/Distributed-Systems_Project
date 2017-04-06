package org.komparator.mediator.ws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.jws.WebService;

import org.komparator.mediator.ws.MediatorPortType;

import org.komparator.supplier.ws.BadProductId;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadText;
import org.komparator.supplier.ws.BadText_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;



@WebService(
		endpointInterface = "org.komparator.mediator.ws.MediatorPortType", 
		wsdlLocation = "mediator.1_0.wsdl", 
		name = "MediatorWebService", 
		portName = "MediatorPort", 
		targetNamespace = "http://ws.mediator.komparator.org/", 
		serviceName = "MediatorService"
		)

public class MediatorPortImpl implements MediatorPortType {

	// end point manager
	private MediatorEndpointManager endpointManager;

	public MediatorPortImpl(MediatorEndpointManager endpointManager) {
		this.endpointManager = endpointManager;
	}
		
	private List<CartView> cartList = new ArrayList<CartView>();

	// Main operations -------------------------------------------------------
	
	@Override
	public List<ItemView> getItems(String productId) throws InvalidItemId_Exception {
		
		UDDINaming uddi = endpointManager.getUddiNaming();
		
		Collection<UDDIRecord> colec = listSuppliers();
		
		ProductView product = null;
		
		ItemView item = new ItemView();
		
		ItemIdView itemId = new ItemIdView();
		
		List<ItemView> productList= new ArrayList<ItemView>();
		
		for (UDDIRecord record : colec ){
			 
			if (productId == null)
				throwInvalidItemId("Product identifier cannot be null!");
			
			productId = productId.trim();
			if (productId.length() == 0)
				
				throwInvalidItemId("Product identifier cannot be empty or whitespace!");
			
			// retrieve product
			SupplierClient supplier;
			try {
				supplier = new SupplierClient(record.getUrl());
				
				product = supplier.getProduct(productId);
				
				itemId.setProductId(productId);
				
				itemId.setSupplierId(record.getOrgName());
					
				item.setDesc(product.getDesc());
					
				item.setPrice(product.getPrice());
					
				item.setItemId(itemId);
							
				productList.add(item);
				
			} catch (SupplierClientException e) {
				
				e.printStackTrace();
				
			} catch (BadProductId_Exception e) {
				
				e.printStackTrace();
			}
				
			
						
		}
		
		return productList;
	}
	
	
	@Override
	public List<ItemView> searchItems(String descText) throws InvalidText_Exception {
		
		UDDINaming uddi = endpointManager.getUddiNaming();
		
		Collection<UDDIRecord> colec = listSuppliers();
			
		ItemView item = new ItemView();
		
		ItemIdView itemId = new ItemIdView();
		
		List<ItemView> productList= new ArrayList<ItemView>();
		
		for (UDDIRecord record : colec ){
			 
			if (descText == null)
				throwInvalidText("Product description cannot be null!");
			
			// retrieve product
			SupplierClient supplier;
			
			try {
				supplier = new SupplierClient(record.getUrl());
				
				List<ProductView> IDlist = supplier.searchProducts(descText);
				
				for(ProductView p: IDlist){
					
					itemId.setProductId(p.getId());
					
					itemId.setSupplierId(record.getOrgName());
						
					item.setDesc(p.getDesc());
						
					item.setPrice(p.getPrice());
						
					item.setItemId(itemId);
								
					productList.add(item);
					
				}
			} catch (SupplierClientException e) {
				
				e.printStackTrace();
				
			} catch (BadText_Exception e) {
				
				e.printStackTrace();
			} 
		}
		
		return productList;
	}
	
	@Override
	public void addToCart(String cartId, ItemIdView itemId, int itemQty) throws InvalidCartId_Exception,
			InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		
		UDDINaming uddi = endpointManager.getUddiNaming();
		
		Collection<UDDIRecord> colec = listSuppliers();
		
		List<CartItemView> items = new ArrayList<CartItemView>();
		
		ItemView iV = new ItemView();
		
		ProductView product = null;
		
		if(cartId == null){
			throwInvalidCartId("CartId cannot be null!");
		}
		
		if(itemId == null){
			throwInvalidItemId("ItemId cannot be null!");
		}
		
		if(itemQty <0){
			throwInvalidQuantity("Quantity cannot be negative!");
		}
		for (UDDIRecord record : colec ){
			 
			if(record.getOrgName().equals(itemId.getSupplierId())){
				
				SupplierClient supplier;
				try {
					supplier = new SupplierClient(record.getUrl());
					
					product = supplier.getProduct(itemId.getProductId());
					
					if(product.getQuantity() < itemQty){
						
						throwNotEnoughItems("Not enough items! ");
					}
					
				} catch (SupplierClientException e) {
					
					e.printStackTrace();
					
				} catch (BadProductId_Exception e) {
					
					e.printStackTrace();
				}
				if (items.contains(itemId.getProductId())){
					
					for(CartItemView c: items){
						
						if(c.getItem().equals(itemId)){
							
							c.setQuantity( c.getQuantity() + itemQty) ;
						}
					}
				}
				else{
					
					CartItemView cartItem = new CartItemView();
					
					iV.setItemId(itemId);
					
					iV.setDesc(product.getDesc());
					
					iV.setPrice(product.getPrice());
					
					cartItem.setItem(iV);
					
					cartItem.setQuantity(itemQty);
					
					items.add(cartItem);
				}
			}
		}
		if(cartExists(cartId) == false){
			
			CartView carrinho = new CartView();
			
			carrinho.items = items;
			
			carrinho.setCartId(cartId);
			
			cartList.add(carrinho);
		}
		
		if(getCart(cartId) != null){
			
			CartView carrinho = getCart(cartId);
			
			carrinho.items = items;
		}
	}
	
	@Override
	public ShoppingResultView buyCart(String cartId, String creditCardNr)
			throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
		
		UDDINaming uddi = endpointManager.getUddiNaming();
		
		Collection<UDDIRecord> colec = listSuppliers();
		
		if(cartId == null){
			
			throwInvalidCartId("CartId cannot be null!");
		}
		
		if(creditCardNr == null){
			throwInvalidCreditCard("CreditCartNr cannot be null!");
		}
		
		/*if(!CreditCardClient.validateNumber(creditCardNr)){
			
		}*/
		/*
		for (UDDIRecord record : colec ){
		
			SupplierClient supplier;
			
			for(CartView lista: cartList){
				
				if(lista.getCartId().equals(cartId)){
					
					for()
					
					try 
					{
					} catch (SupplierClientException e) {
						
						e.printStackTrace();
					}
				}
			}
		}
		*/
		
		return null;
	}
	// Auxiliary operations --------------------------------------------------	
	
	public boolean cartExists(String cartId){
		
		for(CartView cartview: cartList){
			
			if(cartview.getCartId().equals(cartId)){
				return true;
			}
		}
		return false;
	}
	
	public CartView getCart(String cartId){
		
		for (CartView lista: cartList){
			
			if(lista.getCartId().equals(cartId)){
				return lista;
			}
		}
		return null;
	}
	
	public Collection<UDDIRecord> listSuppliers (){
		
		UDDINaming uddi = endpointManager.getUddiNaming();
		
		Collection<UDDIRecord> colec = null;
		
		try{
			colec = uddi.listRecords("T21_Supplier%");
			
		} catch ( UDDINamingException e ){
			
			return null;

		}
		return colec;
	}
	
	public String ping(String string){
		
		UDDINaming uddi = endpointManager.getUddiNaming();
		
		Collection<UDDIRecord> colec = listSuppliers();
		
		for (UDDIRecord record : colec ){
			
			try {
				uddi.bind(record);
				
				System.out.println("Texto: " + record.getOrgName());
				
			} catch (UDDINamingException e) {
				
				e.printStackTrace();
			}
		}
		return "Ok";
	}

	@Override
	public void clear() {
	
	}

	
	@Override
	public List<CartView> listCarts() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<ShoppingResultView> shopHistory() {
		// TODO Auto-generated method stub
		return null;
	}

	
	// View helpers -----------------------------------------------------
	
    // TODO

    
	// Exception helpers -----------------------------------------------------

	/** Helper method to throw new BadProductId exception */
	private void throwInvalidItemId(final String message) throws InvalidItemId_Exception {
		InvalidItemId faultInfo = new InvalidItemId();
		faultInfo.message = message;
		throw new InvalidItemId_Exception(message, faultInfo);
	}
	
	private void throwInvalidText(final String message) throws InvalidText_Exception {
		InvalidText faultInfo = new InvalidText();
		faultInfo.message = message;
		throw new InvalidText_Exception(message, faultInfo);
	}
	
	private void throwInvalidCartId(final String message) throws InvalidCartId_Exception {
		InvalidCartId faultInfo = new InvalidCartId();
		faultInfo.message = message;
		throw new InvalidCartId_Exception(message, faultInfo);
	}
	
	private void throwInvalidQuantity(final String message) throws InvalidQuantity_Exception {
		InvalidQuantity faultInfo = new InvalidQuantity();
		faultInfo.message = message;
		throw new InvalidQuantity_Exception(message, faultInfo);
	}
	
	private void throwNotEnoughItems(final String message) throws NotEnoughItems_Exception {
		NotEnoughItems faultInfo = new NotEnoughItems();
		faultInfo.message = message;
		throw new NotEnoughItems_Exception(message, faultInfo);
	}
	
	private void throwInvalidCreditCard(final String message) throws InvalidCreditCard_Exception {
		InvalidCreditCard faultInfo = new InvalidCreditCard();
		faultInfo.message = message;
		throw new InvalidCreditCard_Exception(message, faultInfo);
	}

}
