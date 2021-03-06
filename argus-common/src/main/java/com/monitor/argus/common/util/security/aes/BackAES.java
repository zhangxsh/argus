package com.monitor.argus.common.util.security.aes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * AES加密与解密基本工具类
 * <p>
 * 分两种加密与解密方式
 * 
 * @author Ying
 * @date 2015年10月20日 下午1:23:08
 * 
 */
public class BackAES {

    private static Logger logger = LoggerFactory.getLogger(BackAES.class);
    /** 默认偏移 */
    private static String ivParameter = "1234567890123456";//

    private static String TRANSFORMATION_MODE = "";
    private static boolean isPwd = false;

    private static int pwdLenght = 16;
    private static String val = "0";

    /** ECB */
    public static final int TRANSFORMATION_TYPE_ECB = 0;
    /** CBC */
    public static final int TRANSFORMATION_TYPE_CBC = 1;
    /** CFB */
    public static final int TRANSFORMATION_TYPE_CFB = 2;
    /** OFB */
    public static final int TRANSFORMATION_TYPE_OFB = 3;
    /** AES */
    public static final String AES_WAYS = "AES";
    /** UTF_8 */
    public static final String UTF_8 = "utf-8";
    /** PKCS5Padding */
    public static final String PADDING_PKCS5PADDING = "PKCS5Padding";
    /** 斜杠 */
    public static final String SLASH = "/";

    public static String selectMod(int type) {
        switch (type) {
            case TRANSFORMATION_TYPE_ECB:
                isPwd = false;
                TRANSFORMATION_MODE = AES_WAYS + SLASH + AESType.ECB.key() + SLASH + PADDING_PKCS5PADDING;
                break;
            case TRANSFORMATION_TYPE_CBC:
                isPwd = true;
                TRANSFORMATION_MODE = AES_WAYS + SLASH + AESType.CBC.key() + SLASH + PADDING_PKCS5PADDING;
                break;
            case TRANSFORMATION_TYPE_CFB:
                isPwd = true;
                TRANSFORMATION_MODE = AES_WAYS + SLASH + AESType.CFB.key() + SLASH + PADDING_PKCS5PADDING;
                break;
            case TRANSFORMATION_TYPE_OFB:
                isPwd = true;
                TRANSFORMATION_MODE = AES_WAYS + SLASH + AESType.OFB.key() + SLASH + PADDING_PKCS5PADDING;
                break;
        }
        return TRANSFORMATION_MODE;

    }

    /**
     * 加密方式一（密匙必须为16位）
     * 
     * @param encryptedSrc
     * @param encryptKey
     * @param cipherType
     * @return
     * @throws Exception
     */
    public static byte[] encrypt(String encryptedSrc, String encryptKey, int cipherType) throws Exception {
        logger.debug("encrypt-S {},{},{}", encryptedSrc, encryptKey, cipherType);
        encryptKey = toMakekey(encryptKey, pwdLenght, val);
        Cipher cipher = Cipher.getInstance(selectMod(cipherType));
        byte[] raw = encryptKey.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, AES_WAYS);
        // 使用CBC模式，需要一个向量iv，可增加加密算法的强度
        IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
        if (isPwd == false) {
            // ECB 不用密码
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        }
        byte[] encrypted = cipher.doFinal(encryptedSrc.getBytes(UTF_8));
        // 使用BASE64做转码。
        byte[] encryptedBase64 = Base64.encode(encrypted);
        logger.debug("encrypt-E {},{},{} {}", encryptedSrc, encryptKey, cipherType, encryptedBase64);
        return encryptedBase64;
    }

    /**
     * 解密方式一
     * 
     * @param decryptedSrc
     * @param decryptKey
     * @param cipherType
     * @return
     * @throws Exception
     */
    public static String decrypt(String decryptedSrc, String decryptKey, int cipherType) throws Exception {
        logger.debug("DE-S {},{},{}", decryptedSrc, decryptKey, cipherType);
        String originalString = null;
        String decryptKeyMade = toMakekey(decryptKey, pwdLenght, val);
        try {
            byte[] raw = decryptKeyMade.getBytes("ASCII");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, AES_WAYS);
            Cipher cipher = Cipher.getInstance(selectMod(cipherType));
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
            if (isPwd == false) {
                // ECB 不用密码
                cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            }
            // 先用base64解密
            byte[] encrypted1 = Base64.decode(decryptedSrc.getBytes());
            byte[] original = cipher.doFinal(encrypted1);
            originalString = new String(original, UTF_8);
        } catch (Exception ex) {
            logger.error("decrypt- ", ex);
            return null;
        }
        logger.debug("DE-E-RTN{}FRPRM{},{},{}", decryptedSrc, decryptKey, cipherType, originalString);
        return originalString;

    }

    /**
     * decrypt Key
     * 
     * @param str
     * @param strLength
     * @param val
     * @return
     */
    private static String toMakekey(String str, int strLength, String val) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(str).append(val);
                str = buffer.toString();
                strLen = str.length();
            }
        }
        return str;
    }

    /*********************************** 第二种 ***********************************************/
    /**
     * 加密方式二 TODO：logger日志
     * 
     * @param encryptedSrc
     * @param password
     * @return
     */
    public static byte[] newencrypt(String encryptedSrc, String password) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance(AES_WAYS);
            kgen.init(128, new SecureRandom(password.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, AES_WAYS);
            Cipher cipher = Cipher.getInstance(AES_WAYS);// 创建AES加密编码器
            byte[] byteContent = encryptedSrc.getBytes(UTF_8);
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化AES加密
            byte[] result = cipher.doFinal(byteContent);
            return result; // AES加密结果
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解密方式二
     * 
     * @param content 待解密内容,格式为byte数组
     * @param password AES解密使用的密钥
     * @return
     */
    public static byte[] newdecrypt(byte[] content, String password) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance(AES_WAYS);
            kgen.init(128, new SecureRandom(password.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, AES_WAYS);
            Cipher cipher = Cipher.getInstance(AES_WAYS);// 创建AES加密编码器
            cipher.init(Cipher.DECRYPT_MODE, key);// 初始化AES加密
            byte[] result = cipher.doFinal(content);
            return result; // 得到AES解密结果
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将二进制转换成16进制
     * 
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * java将16进制转换为二进制
     * 
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

}
