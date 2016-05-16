package me.zhl.libkeyboard.keyboard;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by zhang on 2016/4/19 0019.
 */
public class KeyboardEncryptTools {

    private static byte[] sPassword = "123456".getBytes();

    private static final int BYTE_MAX_LENGTH = 256;

    /**
     * 对称加密
     */
    public static byte[] encrypt(StringBuffer data) {
        if(data == null || data.length() == 0){
            return null;
        }

        byte[] strBuilderByte = new byte[BYTE_MAX_LENGTH];
        int byteLength = 0;
        for (int i = 0; i < data.length(); i++) {
            String zz = data.substring(i,i+1);
            byte[] bb = zz.getBytes();
            for (byte b: bb) {
                strBuilderByte[byteLength++] = b;
            }
        }

        return encrypt(strBuilderByte, byteLength);
    }

    public static byte[] encrypt(byte[] data) {
        if (null == data || 0 == data.length) {
            return null;
        }
        return encrypt(data, data.length);
    }

    public static byte[] encrypt(byte[] content, int length) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");

            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
            sr.setSeed(sPassword);
            kgen.init(128, sr); // 192 and 56 bits may not be available
            byte[] enCodeFormat = kgen.generateKey().getEncoded();

            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            return cipher.doFinal(content, 0, length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解密
     */
    public static byte[] decrypt(byte[] data) {
        if (null == data || 0 == data.length) {
            return null;
        }
        return decrypt(data, data.length);
    }
    public static byte[] decrypt(byte[] data, int length) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");

            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
            sr.setSeed(sPassword);
            kgen.init(128, sr); // 192 and 256 bits may not be available
            byte[] enCodeFormat = kgen.generateKey().getEncoded();

            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
            return cipher.doFinal(data, 0, length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
