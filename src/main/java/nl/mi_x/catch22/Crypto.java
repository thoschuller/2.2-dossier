package nl.mi_x.catch22;

import java.security.*;

import javax.crypto.*;

import java.util.Base64;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.RSAPublicKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
* This class contains methods to de- and encrypt data, either using RSA and a key from the "privateKey"-environment variable, or using a parameter specified algorithm and key
*
*  @author MI-X 2022 course 2.2 project group 6
*  @author Thomas Schuller
*  @author Nienke Heemskerk
*  @author Rosan van der Linden
*  @author Melle Wels
*/
public class Crypto{

  /**
*
*  Gets AES SecretKey from string
*
*/
  public static SecretKey getSecretKey(String keyString){
    byte[] encodedKey = Base64.getDecoder().decode(keyString);
    return new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
  }

  /**
  * Gets private RSA key from environment's "privateKey" variable
  */
  private static PrivateKey getPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException{
    String privKeyTemp = System.getenv("privateKey").replace("-----BEGIN PRIVATE KEY-----", "");
        privKeyTemp = privKeyTemp.replace("-----END PRIVATE KEY-----", "");
        privKeyTemp = privKeyTemp.replaceAll("\\s+","");
    byte[] privKeyDecoded = Base64.getDecoder().decode(privKeyTemp);
    
    PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(privKeyDecoded);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    return kf.generatePrivate(privKeySpec);
  }

  /**
  * Gets public RSA key from environment's "privateKey" variable
  */
  public static PublicKey getPublicKey()throws NoSuchAlgorithmException, InvalidKeySpecException{
    RSAPrivateCrtKey privk = (RSAPrivateCrtKey) getPrivateKey();
    RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(privk.getModulus(), privk.getPublicExponent());
    KeyFactory kf = KeyFactory.getInstance("RSA");
    return kf.generatePublic(publicKeySpec);
  }

/**
*  decrypts a bytearray containing an encrypted string using the key in the environment's "privateKey" variable and the RSA algorithm
*
*  @param decryptable the bytearray to decrypt
*  @param privatekey a boolean value indicating whether to use the private or public key
*         true: method uses the private key
*         false: methode uses the public key
*/
  public static String decryptOwnKey(byte[] decryptable, boolean privateKey) throws Exception{
    Key key;
    if(privateKey) key = getPrivateKey();
    else key = getPublicKey();
    return decrypt(decryptable, key, "RSA");
  }

/**
*  encrypts a string using the key in the environment's "privateKey" variable and the RSA algorithm
*
*  @param encryptable the string to encrypt
*  @param privatekey a boolean value indicating whether to use the private or public key
*         true: method uses the private key
*         false: methode uses the public key
*/
  public static byte[] encryptOwnKey(String encryptable, boolean privateKey) throws Exception{
    Key key;
    if(privateKey) key = getPrivateKey();
    else key = getPublicKey();
    return encrypt(encryptable, key, "RSA");
  }

  /**
  * decrypts a string using the given key and algorithm
  * 
  * @param decryptable the bytearray containing an encrypted string
  * @param KEY the key or secret to be used for the decryption
  * @param algorithm a string containing the name of the algorithm to be used, eg: "RSA"
  *
  */
  public static String decrypt(byte[] decryptable, Key KEY, String algorithm) throws Exception{
    Cipher cipher = Cipher.getInstance(algorithm);
    cipher.init(Cipher.DECRYPT_MODE, KEY);
    String decrypted = new String(cipher.doFinal(decryptable));
    return decrypted;
  }

  /**
  * encrypts a string using the given key and algorithm
  * 
  * @param encryptable the string to encrypt
  * @param KEY the key or secret to be used for the encryption
  * @param algorithm a string containing the name of the algorithm to be used, eg: "RSA"
  *
  */
  public static byte[] encrypt(String encryptable, Key KEY, String algorithm) throws Exception{
    Cipher cipher = Cipher.getInstance(algorithm);
    cipher.init(Cipher.ENCRYPT_MODE, KEY);
    byte[] encrypted = cipher.doFinal(encryptable.getBytes());
    return encrypted;
  }
  
}