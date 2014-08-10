/**
   Copyright Mob&Me 2012 (@MobAndMe)

   Licensed under the LGPL Lesser General Public License, Version 3.0 (the "License"),  
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.gnu.org/licenses/lgpl.html

   Unless required by applicable law or agreed to in writing, software 
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
   Website: http://adaframework.com
   Contact: Txus Ballesteros <txus.ballesteros@mobandme.com>
*/

package com.mobandme.ada;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.mobandme.ada.exceptions.AdaFrameworkException;

/**
 * Helper class to manage encryption processes. 
 * @version 2.4.3
 * @author Mob&Me
 *
 */
final class EncryptionHelper {
	private final static String HEX = "0123456789ABCDEF";
	private static String encriptionAlgorithm = DataUtils.DEFAULT_ENCRIPTION_ALGORITHM;
	
	public static void setEncriptionAlgorithm(String algorithm) {
		encriptionAlgorithm = algorithm;
	}
	public static String getEncriptionAlgorithm() {
		return encriptionAlgorithm;
	}
	
	public static String encrypt(String masterEncryptionKey, String cleartext) throws AdaFrameworkException {
		try {
			
			if (cleartext != null && !cleartext.trim().equals("")) {
				byte[] rawKey = getRawKey(masterEncryptionKey.getBytes());
		        byte[] result = encrypt(rawKey, cleartext.getBytes());
		        return toHex(result);
			} else {
				return cleartext;
			}
			
		} catch (Exception e) {
			throw new AdaFrameworkException(e);
		}
	}
	
	public static String decrypt(String masterEncryptionKey, String encrypted) throws AdaFrameworkException {
		try {
			
			if (encrypted != null && !encrypted.trim().equals("")) {
		        byte[] rawKey = getRawKey(masterEncryptionKey.getBytes());
		        byte[] enc = toByte(encrypted);
		        byte[] result = decrypt(rawKey, enc);
		        
		        return new String(result);
			} else {
				return encrypted;
			}
	        
		} catch (Exception e) {
			throw new AdaFrameworkException(e);
		}
	}
	
	private static byte[] getRawKey(byte[] seed) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance(encriptionAlgorithm);
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(seed);
        
	    kgen.init(128, sr); // 192 and 256 bits may not be available
	    SecretKey skey = kgen.generateKey();
	    byte[] raw = skey.getEncoded();
	    return raw;
	}
	
	
	private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
	    SecretKeySpec skeySpec = new SecretKeySpec(raw, encriptionAlgorithm);
	    Cipher cipher = Cipher.getInstance(encriptionAlgorithm);
	    cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
	    byte[] encrypted = cipher.doFinal(clear);
	    return encrypted;
	}
	
	private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
	    SecretKeySpec skeySpec = new SecretKeySpec(raw, encriptionAlgorithm);
	    Cipher cipher = Cipher.getInstance(encriptionAlgorithm);
	    cipher.init(Cipher.DECRYPT_MODE, skeySpec);
	    byte[] decrypted = cipher.doFinal(encrypted);
	    
	    return decrypted;
	}
		
	private static byte[] toByte(String hexString) {
	        int len = hexString.length()/2;
	        byte[] result = new byte[len];
	        for (int i = 0; i < len; i++)
	                result[i] = Integer.valueOf(hexString.substring(2*i, 2*i+2), 16).byteValue();
	        return result;
	}
	
	private static String toHex(byte[] buf) {
	        if (buf == null)
	                return "";
	        StringBuffer result = new StringBuffer(2*buf.length);
	        for (int i = 0; i < buf.length; i++) {
	                appendHex(result, buf[i]);
	        }
	        return result.toString();
	}
	
	private static void appendHex(StringBuffer sb, byte b) {
	        sb.append(HEX.charAt((b>>4)&0x0f)).append(HEX.charAt(b&0x0f));
	}
}
