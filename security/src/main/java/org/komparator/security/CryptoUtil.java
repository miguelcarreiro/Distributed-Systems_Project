package org.komparator.security;

import java.io.*;
import java.security.*;
import javax.crypto.*;
import java.util.*;

public class CryptoUtil {

	public byte[] asymCipher(byte[] plainBytes, Key publicKey){
		try{
			javax.crypto.Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			byte[] cipherBytes = cipher.doFinal(plainBytes);
			return cipherBytes;
		} catch (Exception e){
			
		}
		return null;
	}
	
	public byte[] asymDecipher(byte[] cipherBytes, Key privateKey){
		try{
			javax.crypto.Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			byte[] plainBytes = cipher.doFinal(cipherBytes);
			return plainBytes;
		} catch(Exception e){
			
		}
		return null;
	}

	
	
}
