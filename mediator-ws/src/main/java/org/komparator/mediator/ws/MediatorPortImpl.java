package org.komparator.mediator.ws;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Timer;

import javax.jws.HandlerChain;
import javax.jws.WebService;

import org.komparator.mediator.ws.MediatorPortType;
import org.komparator.mediator.ws.cli.MediatorClient;
import org.komparator.mediator.ws.MediatorEndpointManager.Type;
import org.komparator.supplier.ws.BadProductId;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadText;
import org.komparator.supplier.ws.BadText_Exception;
import org.komparator.supplier.ws.InsufficientQuantity_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.PurchaseView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;

import pt.ulisboa.tecnico.sdis.ws.cli.CreditCardClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CreditCardClientException;
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
@HandlerChain(file = "mediator-ws_handler-chain.xml")
public class MediatorPortImpl implements MediatorPortType {

	// end point manager
	private MediatorEndpointManager endpointManager;

	public MediatorPortImpl(MediatorEndpointManager endpointManager) {
		this.endpointManager = endpointManager;
		
	}
		
	private List<CartView> cartList = new ArrayList<CartView>();
	
	private List<ShoppingResultView> shopHistoryList = new ArrayList<ShoppingResultView>();
	
	private static int idBuy = 0;
	
	private long actualDate;
	

	// Main operations -------------------------------------------------------
	
	public long getActualDate(){
		return actualDate;
	}

	@Override
	public List<ItemView> getItems(String productId) throws InvalidItemId_Exception {
		
		if (productId == null){
			throwInvalidItemId("Product identifier cannot be null!");
		}
		
		productId = productId.trim();
		
		if (productId.length() == 0){
			throwInvalidItemId("Product identifier cannot be empty or whitespace!");
		}
		
		UDDINaming uddi = endpointManager.getUddiNaming();
		
		Collection<UDDIRecord> colec = listSuppliers();
		
		ProductView product = null;
		
		ItemView item = new ItemView();
		
		ItemIdView itemId = new ItemIdView();
		
		List<ItemView> productList= new ArrayList<ItemView>();
		int suppliers = 0;
		
		for (UDDIRecord record : colec ){

			SupplierClient supplier;
			try {
				supplier = new SupplierClient(record.getUrl());
				
				product = supplier.getProduct(productId);
				
				if (product != null) {
					suppliers++;
					
					itemId.setProductId(productId);
					
					itemId.setSupplierId(record.getOrgName());
					
					item.setDesc(product.getDesc());
					
					item.setPrice(product.getPrice());
						
					item.setItemId(itemId);
								
					productList.add(item);
				}
				product = null;
			} catch (BadProductId_Exception e) {
	
			} catch (SupplierClientException e) {
				
				e.printStackTrace();
				
			}
		}
		
		
		return productList;
	}
	
	
	@Override
	public List<ItemView> searchItems(String descText) throws InvalidText_Exception {
		
		if (descText == null){
			throwInvalidText("Product description cannot be null!");
		}
		
		if(descText.isEmpty()){
			throwInvalidText("Product description cannot be empty!");
		}
		
		UDDINaming uddi = endpointManager.getUddiNaming();
		
		Collection<UDDIRecord> colec = listSuppliers();

		List<ItemView> productList = new ArrayList<ItemView>();
		
		List<ItemView> productListOrdered = new ArrayList<ItemView>();
		
		ItemView lowestItem;
		
		boolean ordered = false;
		
		for (UDDIRecord record : colec){
			 
			SupplierClient supplier;
			
			try {
				supplier = new SupplierClient(record.getUrl());
		
				List<ProductView> IDlist = supplier.searchProducts(descText);
				
				for(ProductView p: IDlist){
					ItemView item = new ItemView();
					ItemIdView itemId = new ItemIdView();
					
					itemId.setProductId(p.getId());
					itemId.setSupplierId(record.getOrgName());
					item.setDesc(p.getDesc());
					item.setPrice(p.getPrice());
					item.setItemId(itemId);
								
					productList.add(item);
					
				}
			} catch (BadText_Exception e) {
			} catch (SupplierClientException e) {
				e.printStackTrace();
			} 
		}
		
		if(productList.size() < 2){
			productListOrdered = productList;
		}
		else {
			lowestItem = productList.get(0);
			while(!ordered){
				for (ItemView itemView : productList){
					if (lowestItem.getItemId().getProductId().compareTo(itemView.getItemId().getProductId()) > 0){
						lowestItem = itemView;
					} else if(lowestItem.getItemId().getProductId().compareTo(itemView.getItemId().getProductId()) == 0){
						if(lowestItem.getPrice() > (itemView.getPrice())){
							lowestItem = itemView;
						}
					}
				}
				productListOrdered.add(lowestItem);
				productList.remove(productList.indexOf(lowestItem));
				if(productList.size() > 0){
					lowestItem = productList.get(0);
				} else{
					ordered = true;
				}
				
			}
		}

		return productListOrdered;
	}
	
	@Override
	public synchronized void addToCart(String cartId, ItemIdView itemId, int itemQty) throws InvalidCartId_Exception,
			InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		
		UDDINaming uddi = endpointManager.getUddiNaming();
		
		Collection<UDDIRecord> colec = listSuppliers();
		
		List<CartItemView> itemsOfCart = new ArrayList<CartItemView>();
		
		boolean productInCart = false;
		
		int productQty = 0;
		
		if(cartId == null){
			throwInvalidCartId("CartId cannot be null!");
		}
		
		if(cartId.equals("")){
			throwInvalidCartId("CartId cannot be blank!");
		}

		boolean cartEx = cartExists(cartId);
		
		if(itemId == null){
			throwInvalidItemId("ItemId cannot be null!");
		}
		
		if(itemId.getProductId() == null || itemId.getSupplierId() ==  null){

			throwInvalidItemId("ItemId parameters cannot be null or empty!");
		}
		
		if(itemId.getProductId().equals("") || itemId.getSupplierId() .equals("")){
			
			throwInvalidItemId("ItemId parameters cannot be null or empty!");
		}
		
		if(itemQty <= 0){
			throwInvalidQuantity("Quantity cannot be negative or zero!");
		}
		for (UDDIRecord record : colec ){
			if(record.getOrgName().equals(itemId.getSupplierId())){
				
				SupplierClient supplier;
				
				ProductView product = null;
				productQty = 0;
				try {
					supplier = new SupplierClient(record.getUrl());
					
					product = supplier.getProduct(itemId.getProductId());
					if (product == null){
						throwInvalidItemId("Product not exists!");
					}
					productQty = product.getQuantity();
					if(productQty < itemQty){
						throwNotEnoughItems("Not enough items! ");
					}
					
				} catch (SupplierClientException e) {
					System.out.println("Failure in supplier " + record.getOrgName());
					return;
				} catch (BadProductId_Exception e) {
					
				}
				
				if(cartEx){
					CartView carro = getCart(cartId);
					for(CartItemView c: carro.items){
						
						if(c.getItem().getItemId().getProductId().equals(itemId.getProductId()) && c.getItem().getItemId().getSupplierId().equals(itemId.getSupplierId())){
							if((c.getQuantity() + itemQty) > productQty){
								throwNotEnoughItems("Not enough items! ");
							} else {
								c.setQuantity( c.getQuantity() + itemQty) ;
								productInCart = true;
							}
						}
					}
				}

				if(!productInCart && product != null){
					ItemView iV = new ItemView();
					
					CartItemView cartItem = new CartItemView();
					
					iV.setItemId(itemId);
					
					iV.setDesc(product.getDesc());
					
					iV.setPrice(product.getPrice());
					
					cartItem.setItem(iV);
					
					cartItem.setQuantity(itemQty);
					
					itemsOfCart.add(cartItem);
				}
				productInCart = false;
			}
		}
		if(!cartEx){
			CartView carrinho = new CartView();
			
			carrinho.items = itemsOfCart;
			
			carrinho.setCartId(cartId);
			
			cartList.add(carrinho);
		}
		
		else{
			CartView carrinho = getCart(cartId);
			for(CartItemView i : itemsOfCart){
				carrinho.items.add(i);
			}
			
		}
		// update cart secondary mediator
		String wsURL = endpointManager.getWsURL();
		int index = wsURL.indexOf("8");
		if(wsURL.length() > index + 4){
			String strPort = wsURL.substring(index+3, index+4);
			int port = Integer.parseInt(strPort);
			port = (port % 2) + 1;
			wsURL = wsURL.substring(0, index + 3) + port + wsURL.substring(index + 4);
		
			try{
				MediatorClient mediatorClient = new MediatorClient(wsURL);
				mediatorClient.updateCart(getCart(cartId));
			} catch (Exception e){
				
			}
		}
	}
	
	@Override
	public synchronized ShoppingResultView buyCart(String cartId, String creditCardNr)
			throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
		
		UDDINaming uddi = endpointManager.getUddiNaming();
		
		Collection<UDDIRecord> colec = listSuppliers();
		List<CartItemView> purchases = new ArrayList<CartItemView>();
		List<CartItemView> rejected = new ArrayList<CartItemView>();
		ShoppingResultView shopping = new ShoppingResultView();
		int totalPrice = 0;
		
		if(cartId == null || cartId.equals("")){
			
			throwInvalidCartId("CartId cannot be null or empty!");
		}
		
		if(creditCardNr == null || creditCardNr.equals("")){
			throwInvalidCreditCard("CreditCartNr cannot be null or empty!");
		}
		
		if(getCart(cartId) == null || getCart(cartId).getItems().size() == 0){
			throwInvalidCartId("Cart doesn't exist!");
		}
		
		try{
			CreditCardClient creditCard = new CreditCardClient("http://ws.sd.rnl.tecnico.ulisboa.pt:8080/cc");
		
			if(creditCard.validateNumber(creditCardNr)){
			
				for (UDDIRecord record : colec ){
					
					for(CartView lista: cartList){
						
						if(lista.getCartId().equals(cartId)){
							
							for(CartItemView items: lista.items){
								
								if(items.getItem().getItemId().getSupplierId().equals(record.getOrgName())){
									
									try{
										SupplierClient supplier = new SupplierClient(record.getUrl());
										supplier.buyProduct(items.getItem().getItemId().getProductId(), items.getQuantity());
										purchases.add(items);
										totalPrice += (items.getItem().getPrice())*(items.getQuantity());
									} catch (InsufficientQuantity_Exception e) {
										rejected.add(items);
										
									} catch (Exception e){	
									}
								}
							}
						}
					}
				}
			}
			else{
				throwInvalidCreditCard("CreditCart payment refused!");
			}
		} catch(InvalidCreditCard_Exception | CreditCardClientException e){
				throwInvalidCreditCard("Error creating credit card!");
		}
		
		Result result;
		
		if(purchases.size() == 0){
			result = Result.EMPTY;
		}
		else if(rejected.size() == 0){
			result = Result.COMPLETE;
		}
		else {
			result = Result.PARTIAL;
		}
		
		shopping.setResult(result);
		shopping.setTotalPrice(totalPrice);
		shopping.setId(Integer.toString(idBuy));
		idBuy++;

		if(purchases.size() > 0){
			shopping.purchasedItems = purchases;
			}
		
		if(rejected.size() > 0){
			shopping.droppedItems = rejected;
		}
		
		shopHistoryList.add(0, shopping);
		// update cart secondary mediator
				String wsURL = endpointManager.getWsURL();
				int index = wsURL.indexOf("8");
				if(wsURL.length() > index + 4){
					String strPort = wsURL.substring(index+3, index+4);
					int port = Integer.parseInt(strPort);
					port = (port % 2) + 1;
					wsURL = wsURL.substring(0, index + 3) + port + wsURL.substring(index + 4);
				
					try{
						MediatorClient mediatorClient = new MediatorClient(wsURL);
						mediatorClient.updateShopHistory(shopping);
					} catch (Exception e){
						
					}
				}
		return shopping;
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
		String result = "";
		if(colec.size() != 0){
			for (UDDIRecord record : colec ){
				try{
					SupplierClient supplier = new SupplierClient(record.getUrl());
					String pingResponse = supplier.ping(string);
					result += record.getOrgName() + " OK: " + pingResponse + "\n";
					
				} catch (Exception e){
					System.out.println(record.getOrgName() + "not OK: Please check!");
				}
			}
		} else{
			return "Ping with mediator done, no suppliers registered";
		}
		System.out.println(result);
		return "Ping with mediator and all registered suppliers done";
	}

	@Override
	public void clear() {
		UDDINaming uddi = endpointManager.getUddiNaming();
		Collection<UDDIRecord> colec = listSuppliers();
		SupplierClient supplier;
		cartList = new ArrayList<CartView>();
		idBuy = 0;
		
		for (UDDIRecord record : colec ){
			try{	
				supplier = new SupplierClient(record.getUrl());
				supplier.clear();
			} catch (Exception e){
				
			}
		}
	}

	@Override
	public List<CartView> listCarts() {
		return cartList;
	}
	
	@Override
	public List<ShoppingResultView> shopHistory() {
		return shopHistoryList;
	}
	
	@Override
	public void imAlive() {
		try{
			if(endpointManager.type.equals(Type.SECONDARY)){
				Date date = new Date();
				actualDate = date.getTime();
			}
		} catch(NullPointerException e){
			
		}
	}
	
	@Override
	public void updateCart(CartView cart){
		for(CartView cartView : cartList){
			if(cartView.getCartId().equals(cart.getCartId())){
				cartList.remove(cartView);
				break;
			}
		}
		cartList.add(cart);
	}
	
	@Override
	public void updateShopHistory(ShoppingResultView shopping){
		shopHistoryList.add(0, shopping);
	}

	
	// View helpers -----------------------------------------------------
	

    
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
	
	private void throwEmptyCart(final String message) throws EmptyCart_Exception {
		EmptyCart faultInfo = new EmptyCart();
		faultInfo.message = message;
		throw new EmptyCart_Exception(message, faultInfo);
	}

}
