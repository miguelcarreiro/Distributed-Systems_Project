package org.komparator.security;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateFactory;

import javax.crypto.*;
import java.util.*;
import org.komparator.security.CryptoUtil;
import org.junit.*;
import static org.junit.Assert.*;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

public class CryptoUtilTest {

    Key private_key;
    Key public_key;

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
       /* FileInputStream is_private = new FileInputStream("private_key.jks");
        FileInputStream is_public = new FileInputStream("public_key.cer");
        
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(is_private, "1nsecure".toCharArray());
        String alias = "alias_private";
        private_key = keystore.getKey(alias, "ins3cur3".toCharArray());
        
        CAClient cac = new CAClient("url");
        String textcertificate = cac.getCertificate("Txx_Mediator");
        Certificate cert = 
        
        
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        java.security.cert.Certificate public_cert = cf.generateCertificate(is_public);*/
        
       
    }

    @After
    public void tearDown() {
        // runs after each test
    }

    // tests
    @Test
    public void testEncryptAndDecrypt() {
    	/*String toEncrypt = "Mensagem de teste";
    	
    	byte[] bytesToEncrypt = toEncrypt.getBytes();
    	byte[] encryptedBytes = asymCipher(bytesToEncrypt, private_key);*/
    	
        // do something ...

        // assertEquals(expected, actual);
        // if the assert fails, the test fails
    }

}
