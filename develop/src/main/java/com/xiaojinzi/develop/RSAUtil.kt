package com.xiaojinzi.develop

import org.apache.commons.codec.binary.Base64
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

internal object RSAUtil {

    const val RSA = "RSA"
    const val algorithm = "RSA/None/PKCS1Padding"

    //生成秘钥对
    @get:Throws(Exception::class)
    val keyPair: KeyPair
        get() {
            val keyPairGenerator = KeyPairGenerator.getInstance(RSA)
            keyPairGenerator.initialize(2048)
            return keyPairGenerator.generateKeyPair()
        }

    //获取公钥(Base64编码)
    @Throws(UnsupportedEncodingException::class)
    fun getPublicKey(keyPair: KeyPair): String {
        val publicKey = keyPair.public
        val bytes = publicKey.encoded
        return byte2Base64(bytes)
    }

    //获取私钥(Base64编码)
    @Throws(UnsupportedEncodingException::class)
    fun getPrivateKey(keyPair: KeyPair): String {
        val privateKey = keyPair.private
        val bytes = privateKey.encoded
        return byte2Base64(bytes)
    }

    //将Base64编码后的公钥转换成PublicKey对象
    @Throws(Exception::class)
    fun string2PublicKey(pubStr: String): PublicKey {
        val keyBytes = base642Byte(pubStr)
        val keySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance(RSA)
        return keyFactory.generatePublic(keySpec)
    }

    //将Base64编码后的私钥转换成PrivateKey对象
    @Throws(Exception::class)
    fun string2PrivateKey(priStr: String): PrivateKey {
        val keyBytes = base642Byte(priStr)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance(RSA)
        return keyFactory.generatePrivate(keySpec)
    }

    //公钥加密
    @Throws(Exception::class)
    fun publicEncrypt(content: ByteArray?, publicKey: PublicKey?): ByteArray {
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return cipher.doFinal(content)
    }

    // 公钥解密
    @Throws(Exception::class)
    fun publicDecrypt(content: ByteArray?, publicKey: PublicKey?): ByteArray {
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.DECRYPT_MODE, publicKey)
        return cipher.doFinal(content)
    }

    //私钥加密
    @Throws(Exception::class)
    fun privateEncrypt(content: ByteArray?, privateKey: PrivateKey?): ByteArray {
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, privateKey)
        return cipher.doFinal(content)
    }

    @Throws(Exception::class)
    fun privateDecrypt(content: ByteArray?, privateKey: PrivateKey?): ByteArray {
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        return cipher.doFinal(content)
    }

    //字节数组转Base64编码
    @Throws(UnsupportedEncodingException::class)
    fun byte2Base64(bytes: ByteArray?): String {
        return String(Base64.encodeBase64(bytes), Charset.forName("UTF-8"))
    }

    //Base64编码转字节数组
    @Throws(IOException::class)
    fun base642Byte(base64Key: String): ByteArray {
        return Base64.decodeBase64(base64Key.toByteArray(charset("UTF-8")))
    }
}