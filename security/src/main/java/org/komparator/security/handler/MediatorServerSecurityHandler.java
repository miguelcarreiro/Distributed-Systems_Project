package org.komparator.security.handler;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Date;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.text.SimpleDateFormat;

import java.security.cert.Certificate;
import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import org.komparator.security.CryptoUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClientApp;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

/**
 * This SOAPHandler verifies the security of the message.
 *
 */
public class MediatorServerSecurityHandler implements SOAPHandler<SOAPMessageContext> {

	public static String senderName = "SENDER";
	
	public static final String CONTEXT_PROPERTY = "my.property";


	//
	// Handler interface implementation
	//

	/**
	 * Gets the header blocks that can be processed by this Handler instance. If
	 * null, processes all.
	 */
	@Override
	public Set<QName> getHeaders() {
		return null;
	}

	/**
	 * The handleMessage method is invoked for normal processing of inbound and
	 * outbound messages.
	 */
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		System.out.println("AddHeaderHandler: Handling message.");

		Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		try {
			if (!outboundElement.booleanValue()) {
				System.out.println("Reading header in inbound SOAP message...");
				
				// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
		
				SOAPBody soapBody = se.getBody();
				
				NodeList children = soapBody.getFirstChild().getChildNodes();
				for (int i = 0; i < children.getLength(); i++) {
						Node argument = children.item(i);
						
						if (argument.getNodeName().equals("creditCardNr")) {
							
							String encryptedArgument = argument.getTextContent();
							System.out.println("----------Atributo enc------");
							System.out.println(encryptedArgument);
							System.out.println("----------Fim Atributo enc------");
							
							FileInputStream is_private = new FileInputStream("src/main/resources/T21_Mediator.jks");
							
							KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
					        keystore.load(is_private, "nondij9M".toCharArray());
					        
					        String alias = "t21_mediator";
					        Key key = keystore.getKey(alias, "nondij9M".toCharArray());
					        
				        	PrivateKey privateKey = (PrivateKey) key;
				        	
							if (is_private != null){
								is_private.close();
							}
							System.out.println("----------Certificados gerado------");
							
							byte[] encryptedByte = parseBase64Binary(encryptedArgument);
							CryptoUtil crypto = new CryptoUtil();
							byte[] decryptedByte = crypto.asymDecipher(encryptedByte, privateKey);
							String decodedSecretArgument = printBase64Binary(decryptedByte);
							
							System.out.println("----------Encriptado------");
							System.out.println(decodedSecretArgument);
							System.out.println("----------Fim Encriptado------");
							
							argument.setTextContent(decodedSecretArgument);
							msg.saveChanges();
							
						}
				}

			} else {}
		} catch (RuntimeException e){
			throw new RuntimeException();
		} catch (IOException e){
		} catch (Exception e) {
			System.out.print("Caught exception in handleMessage: ");
			System.out.println(e);
			System.out.println("-----------Log-------------------");
			e.printStackTrace();
			System.out.println("-----------End Log---------------");
			System.out.println("Continue normal processing...");
		}

		return true;
	}

	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		System.out.println("Ignoring fault message...");
		return true;
	}

	/**
	 * Called at the conclusion of a message exchange pattern just prior to the
	 * JAX-WS runtime dispatching a message, fault or exception.
	 */
	@Override
	public void close(MessageContext messageContext) {
		// nothing to clean up
	}

}