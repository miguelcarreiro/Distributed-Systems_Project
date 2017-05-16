package org.komparator.mediator.ws;

import java.io.IOException;
import java.util.Collection;
import java.util.Timer;

import javax.xml.ws.Endpoint;

import org.komparator.security.handler.SupplierSecurityHandler;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;

/** End point manager */
public class MediatorEndpointManager {

	/** UDDI naming server location */
	private String uddiURL = null;
	/** Web Service name */
	private String wsName = null;

	/** Get Web Service UDDI publication name */
	public String getWsName() {
		return wsName;
	}
	
	public static enum Type {
		PRIMARY, SECONDARY;
	}
	
	Type type;
	
	private Timer timer;

	/** Web Service location to publish */
	private String wsURL = null;

	/** Port implementation */

	public MediatorPortImpl portImpl = new MediatorPortImpl(this);

	/** Obtain Port implementation */
	public MediatorPortType getPort() {
		
        return portImpl;
	}

	/** Web Service endpoint */
	private Endpoint endpoint = null;
	/** UDDI Naming instance for contacting UDDI server */
	private UDDINaming uddiNaming = null;

	/** Get UDDI Naming instance for contacting UDDI server */
	UDDINaming getUddiNaming() {
		return uddiNaming;
	}

	/** output option **/
	private boolean verbose = true;

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/** constructor with provided UDDI location, WS name, and WS URL */
	public MediatorEndpointManager(String uddiURL, String wsName, String wsURL) {
		this.uddiURL = uddiURL;
		this.wsName = wsName;
		this.wsURL = wsURL;
	}

	/** constructor with provided web service URL */
	public MediatorEndpointManager(String wsURL) {
		if (wsURL == null)
			throw new NullPointerException("Web Service URL cannot be null!");
		this.wsURL = wsURL;
	}

	/* end point management */

	public void start() throws Exception {
		try {
			// publish end point

			endpoint = Endpoint.create(this.portImpl);
			if (verbose) {
				System.out.printf("Starting %s%n", wsURL);
			}
			endpoint.publish(wsURL);
		} catch (Exception e) {
			endpoint = null;
			if (verbose) {
				System.out.printf("Caught exception when starting: %s%n", e);
				e.printStackTrace();
			}
			throw e;
		}
		publishToUDDI();
	}

	public void awaitConnections() {
		if (verbose) {
			System.out.println("Awaiting connections");
			System.out.println("Press enter to shutdown");
		}
		try {
			System.in.read();
		} catch (IOException e) {
			if (verbose) {
				System.out.printf("Caught i/o exception when awaiting requests: %s%n", e);
			}
		}
	}

	public void stop() throws Exception {
		try {
			if (endpoint != null) {
				// stop end point
				endpoint.stop();
				if (verbose) {
					System.out.printf("Stopped %s%n", wsURL);
				}
			}
		} catch (Exception e) {
			if (verbose) {
				System.out.printf("Caught exception when stopping: %s%n", e);
			}
		}

		this.portImpl = null;
		timer.cancel();
		unpublishFromUDDI();
	}

	/* UDDI */

	void publishToUDDI() throws Exception {
		try {
			// publish to UDDI
			if (uddiURL != null) {
				if(verifyMediator().equals(Type.PRIMARY)){
					
						if (verbose) {
							System.out.printf("Publishing '%s' to UDDI at %s%n", wsName, uddiURL);
						}
						uddiNaming = new UDDINaming(uddiURL);
						uddiNaming.rebind(wsName, wsURL);
				}
				else{
				}
			}
			System.out.println("Mediator type: " + type.toString());
			LifeProof lifeProof = new LifeProof(this);
			System.out.println("Criou lifeProof");
			timer = new Timer();
			timer.scheduleAtFixedRate(lifeProof, 10000, 5000);
		} catch (Exception e) {
			uddiNaming = null;
			if (verbose) {
				System.out.printf("Caught exception when binding to UDDI: %s%n", e);
			}
			throw e;
		}
	}

	void unpublishFromUDDI() {
		try {
			if (uddiNaming != null) {
				// delete from UDDI
				uddiNaming.unbind(wsName);
				if (verbose) {
					System.out.printf("Unpublished '%s' from UDDI%n", wsName);
				}
				uddiNaming = null;
			}
		} catch (Exception e) {
			if (verbose) {
				System.out.printf("Caught exception when unbinding: %s%n", e);
			}
		}
	}
	
	public Type verifyMediator(){
		try{
			uddiNaming = new UDDINaming(uddiURL);
			if(uddiNaming.lookupRecord("T21_Mediator") == null){
				type = Type.PRIMARY;
				return type;
			}
		} catch (Exception e){}
		type = Type.SECONDARY;
		return type;
	
	}
	
	void replaceUDDI() throws Exception {
		try {
			// publish to UDDI
			if (uddiURL != null) {
				if (verbose) {
					System.out.printf("Publishing '%s' to UDDI at %s%n", wsName, uddiURL);
				}
				uddiNaming = new UDDINaming(uddiURL);
				uddiNaming.rebind(wsName, wsURL);
			}
			type = Type.PRIMARY;
			System.out.println("Mediator type: " + type.toString());
			//duvida
			/*LifeProof lifeProof = new LifeProof(portImpl, type, wsURL);
			System.out.println("Criou lifeProof");
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(lifeProof, 10000, 5000);*/
		} catch (Exception e) {
			uddiNaming = null;
			if (verbose) {
				System.out.printf("Caught exception when binding to UDDI: %s%n", e);
			}
			throw e;
		}
	}
	
	public void setType(Type type){
		this.type = type;
	}
	
	public Type getType(){
		return this.type;
	}
	
	public String getWsURL(){
		return this.wsURL;
	}

}
