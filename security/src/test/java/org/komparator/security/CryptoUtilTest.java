package org.komparator.security;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.crypto.*;
import java.util.*;
import org.komparator.security.CryptoUtil;
import org.junit.*;
import static org.junit.Assert.*;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

public class CryptoUtilTest {

    PrivateKey privatekey;
    PublicKey publickey;

    CryptoUtil crypto;

    // one-time initialization and clean-up
    @BeforeClass
    public static void oneTimeSetUp() {
        // runs once before all tests in the suite
    }

    @AfterClass
    public static void oneTimeTearDown() {
        // runs once after all tests in the suite
    }

    // members

    // initialization and clean-up for each test
    @Before
    public void setUp() throws Exception{
        FileInputStream is_private = new FileInputStream("src/test/resources/private_key.jks");
        FileInputStream is_public = new FileInputStream("src/test/resources/public_key.cer");
        
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(is_private, "1nsecure".toCharArray());
        String alias = "example";
        Key key = keystore.getKey(alias, "ins3cur3".toCharArray());
     	privatekey = (PrivateKey) key;
        
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		Certificate cert = certFactory.generateCertificate(is_public);
		
		publickey = cert.getPublicKey();
		crypto = new CryptoUtil();
    }

    @After
    public void tearDown() {
        // runs after each test
    }

    // tests
    @Test
    public void testEncryptAndDecrypt() {
    	String toEncrypt = "Mensagem1234";
   
    	
    	byte[] bytesToEncrypt = parseBase64Binary(toEncrypt);
    	byte[] encryptedBytes = crypto.asymCipher(bytesToEncrypt, privatekey);
    	Assert.assertNotNull(encryptedBytes);
    	byte[] decryptedBytes = crypto.asymDecipher(encryptedBytes, publickey);
    	Assert.assertNotNull(decryptedBytes);
    	String decryptedString = printBase64Binary(decryptedBytes);
    	System.out.println("Original: " + toEncrypt + " | Resultado: " + decryptedString);
    	Assert.assertEquals(toEncrypt, decryptedString);
    	
    }

}
