package org.komparator.security.handler;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Date;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Provider.Service;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateFactory;
import java.text.SimpleDateFormat;

import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
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
public class MediatorClientSecurityHandler implements SOAPHandler<SOAPMessageContext> {
	
	public static final String CONTEXT_PROPERTY = "my.property";
	
	/** print some error messages to standard error. */
	public static boolean outputFlag = true;


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
		
				SOAPBody soapBody = se.getBody();
				
				NodeList children = soapBody.getFirstChild().getChildNodes();
				for (int i = 0; i < children.getLength(); i++) {
						Node argument = children.item(i);
						
						if (argument.getNodeName().equals("creditCardNr")) {
							
							String secretArgument = argument.getTextContent();
							
							CAClient ca = new CAClient("http://sec.sd.rnl.tecnico.ulisboa.pt:8081/ca?WSDL");
							String certString = ca.getCertificate("T21_Mediator");
							byte[] bytes = certString.getBytes(StandardCharsets.UTF_8);
							CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
							
							InputStream in = new ByteArrayInputStream(bytes);
							Certificate cert = certFactory.generateCertificate(in);
							if (in != null){
								in.close();
							}
							//verificacao se o certificado e valido
							
							FileInputStream in_ca = new FileInputStream("src/main/resources/ca.cer");
							
							CertificateFactory caFactory = CertificateFactory.getInstance("X.509");
							Certificate ca_cer = certFactory.generateCertificate(in_ca);
							
							if (in_ca != null){
								in_ca.close();
							}
							
							if((verifySignedCertificate(cert, ca_cer.getPublicKey()))){
								byte[] decryptByte = parseBase64Binary(secretArgument);
								CryptoUtil crypto = new CryptoUtil();
								byte[] encryptByte = crypto.asymCipher(decryptByte, cert.getPublicKey());
								String encodedSecretArgument = printBase64Binary(encryptByte);
								
								argument.setTextContent(encodedSecretArgument);
								msg.saveChanges();
							}
							
						}
				}

			} else {}
		} catch (RuntimeException e){
			throw new RuntimeException();
		//} catch (IOException e){
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
	
	public static boolean verifySignedCertificate(Certificate certificate, PublicKey caPublicKey) {
		try {
			certificate.verify(caPublicKey);
		} catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException
				| SignatureException e) {
			if (outputFlag) {
				System.err.println("Caught exception while verifying certificate with CA public key : " + e);
				System.err.println("Returning false.");
			}
			return false;
		}
		return true;
	}
}