package org.komparator.security.handler;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Date;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
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
import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClientApp;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

/**
 * This SOAPHandler verifies the security of the message.
 *
 */
public class SupplierSecurityHandler implements SOAPHandler<SOAPMessageContext> {

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
			if (outboundElement.booleanValue()) {
				System.out.println("Writing header in outbound SOAP message...");

				// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				
				byte[] msgBytes = msg.toString().getBytes();
				MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
				messageDigest.update(msgBytes);
				byte[] digest = messageDigest.digest();
				String digestString = printBase64Binary(digest);
				
				System.out.print("Digest: ");
				System.out.println(digestString);

				FileInputStream is_private = new FileInputStream("src/main/resources/"+ senderName + ".jks");
				
				KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		        keystore.load(is_private, "nondij9M".toCharArray());
		        
		        
		        String alias = senderName.toLowerCase();
		        Key key = keystore.getKey(alias, "nondij9M".toCharArray());
		        
	        	PrivateKey privateKey = (PrivateKey) key;
	        	Signature sig = Signature.getInstance("SHA1withRSA");
				sig.initSign(privateKey);
				sig.update(digest);
				byte[] signature = sig.sign();
				
				String sigString = printBase64Binary(signature);
				
				System.out.print("Signature: ");
				System.out.println(sigString);
				
				// add header
				SOAPHeader sh = se.getHeader();
				if (sh == null)
					sh = se.addHeader();

				// add header element (name, namespace prefix, namespace)
				Name name = se.createName("Signature", "s", "http://signature");
				SOAPHeaderElement element = sh.addHeaderElement(name);


				// add header element value
				element.addTextNode(sigString);

			} else {
				System.out.println("Reading header in inbound SOAP message...");
				
				
				// get SOAP envelope header
				SOAPMessage msg = smc.getMessage();
				// delete header
				msg.getSOAPHeader().detachNode();
				
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPHeader sh = se.getHeader();

				// check header
				if (sh == null) {
					System.out.println("Header not found.");
					return true;
				}
	
				
				// get first header element
				Name name = se.createName("Timestamp", "d", "http://timestamp");
				Iterator it = sh.getChildElements(name);
				// check header element
				if (!it.hasNext()) {
					System.out.println("Header element not found.");
					return true;
				}
				SOAPElement element = (SOAPElement) it.next();
				
				CAClient ca = new CAClient("http://sec.sd.rnl.tecnico.ulisboa.pt:8081/ca?WSDL");
				String certString = ca.getCertificate(senderName.toLowerCase());
				byte[] bytes = certString.getBytes(StandardCharsets.UTF_8);
				InputStream in = new ByteArrayInputStream(bytes);
				CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
				Certificate cert = certFactory.generateCertificate(in);


				// get header element value
				
				String encryptString = element.getValue();
				byte[] encryptByte = parseBase64Binary(encryptString);
				CryptoUtil crypto = new CryptoUtil();
				byte[] decryptByte = crypto.asymDecipher(encryptByte, cert.getPublicKey());
				
				
				
				// create digest
				byte[] msgBytes = msg.toString().getBytes();
				MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
				messageDigest.update(msgBytes);
				byte[] digest = messageDigest.digest();
				String digestString = printBase64Binary(digest);
				
				if(!digest.equals(decryptByte)){
					throw new RuntimeException();
				}
				
			}
		} catch (RuntimeException e){
			throw new RuntimeException();
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