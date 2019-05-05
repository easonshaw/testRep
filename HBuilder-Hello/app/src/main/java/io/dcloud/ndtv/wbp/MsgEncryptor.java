package io.dcloud.ndtv.wbp;

import java.io.ByteArrayOutputStream;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.geronimo.mail.util.Base64;

/**
 * @author xiewj 报文加解密机
 */
public class MsgEncryptor {
	private static String m_EncryptKey = "WasuNdtvGYRJ@5611.6645";

	private static byte[] symmetricDecrypto(byte[] byteSource, String sKey) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			int mode = Cipher.DECRYPT_MODE;
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			byte[] keyData = sKey.getBytes("UTF-8");
			DESKeySpec keySpec = new DESKeySpec(keyData);
			Key key = keyFactory.generateSecret(keySpec);
			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(mode, key);
			byte[] result = cipher.doFinal(byteSource);
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			baos.close();
		}
	}

	private static byte[] symmetricEncrypto(byte[] byteSource, String sKey) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			int mode = Cipher.ENCRYPT_MODE;
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			byte[] keyData = sKey.getBytes("UTF-8");
			DESKeySpec keySpec = new DESKeySpec(keyData);
			Key key = keyFactory.generateSecret(keySpec);
			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(mode, key);
			byte[] result = cipher.doFinal(byteSource);
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			baos.close();
		}
	}

	public static String decrypt(String Base64Str) {
		try {
			byte[] m_Data = Base64.decode(Base64Str);
			System.out.println(Base64Str);
			System.out.println(new String(symmetricDecrypto(m_Data, m_EncryptKey), "UTF-8"));
			return new String(symmetricDecrypto(m_Data, m_EncryptKey), "UTF-8");
		} catch (Exception ex) {
			return null;
		}
	}

	public static String encrypt(String ReturnStr) {
		try {
			System.out.println(ReturnStr);
			byte[] m_Data = symmetricEncrypto(ReturnStr.getBytes("UTF-8"), m_EncryptKey);
			System.out.println(new String(Base64.encode(m_Data), "UTF-8"));
			return new String(Base64.encode(m_Data), "UTF-8");
		} catch (Exception ex) {
			return null;
		}
	}

	public StringBuilder stringBuilder = new StringBuilder();

	public void addData(String data) {
		stringBuilder.append(data);
		System.out.println(stringBuilder.length()+"/"+stringBuilder.toString());
	}
	public void clearData(String data) {
		stringBuilder.delete(0,stringBuilder.length());
		System.out.println(stringBuilder.length()+"/"+stringBuilder.toString());
	}

	public String decrypt2() {
		try {
			byte[] m_Data = Base64.decode(stringBuilder.toString());
			System.out.println(stringBuilder.toString());
			System.out.println(new String(symmetricDecrypto(m_Data, m_EncryptKey), "UTF-8"));
			return new String(symmetricDecrypto(m_Data, m_EncryptKey), "UTF-8");
		} catch (Exception ex) {
			return null;
		}
	}

	public String encrypt2() {
		try {
			System.out.println(stringBuilder.toString());
			byte[] m_Data = symmetricEncrypto(stringBuilder.toString().getBytes("UTF-8"), m_EncryptKey);
			System.out.println(new String(Base64.encode(m_Data), "UTF-8"));
			return new String(Base64.encode(m_Data), "UTF-8");
		} catch (Exception ex) {
			return null;
		}
	}
	
}
