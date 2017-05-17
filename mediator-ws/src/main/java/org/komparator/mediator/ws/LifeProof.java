package org.komparator.mediator.ws;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;
import org.komparator.mediator.ws.MediatorEndpointManager.*;
import org.komparator.mediator.ws.cli.MediatorClient;
import org.komparator.mediator.ws.cli.MediatorClientException;

import com.sun.xml.ws.client.ClientTransportException;


public class LifeProof extends TimerTask{
	MediatorEndpointManager mediatorEndpointManager;
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	
	public LifeProof(MediatorEndpointManager mediatorEndpointManager){
		this.mediatorEndpointManager = mediatorEndpointManager;
	}
	
	@Override
	public void run(){
		String wsURL = mediatorEndpointManager.getWsURL();
		if(mediatorEndpointManager.getType().equals(Type.PRIMARY)){
			int index = wsURL.indexOf("8");
			if(wsURL.length() > index + 4){
				String strPort = wsURL.substring(index+3, index+4);
				int port = Integer.parseInt(strPort);
				port = (port % 2) + 1;
				wsURL = wsURL.substring(0, index + 3) + port + wsURL.substring(index + 4);
			}

			try{
				MediatorClient client = new MediatorClient(wsURL);
				client.imAlive();

			} catch (ClientTransportException ce){
				System.out.println("Nao ha secundario");
			} catch (MediatorClientException e){
				System.out.println("correu mal xau");
			}
			
		} else {
			Date date = new Date();
			MediatorPortType portType = mediatorEndpointManager.getPort();
			if(date.getTime() - mediatorEndpointManager.portImpl.getActualDate() > mediatorEndpointManager.getTimerRate()){
				System.out.println("Falhou! Servidor secundario vai assumir papel de principal");
				try{
					mediatorEndpointManager.replaceUDDI();
				} catch (Exception e){
					
				}
			}
		}
	}

}
