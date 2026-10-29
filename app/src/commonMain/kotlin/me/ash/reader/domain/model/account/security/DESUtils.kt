package me.ash.reader.domain.model.account.security

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec

object DESUtils {

    const val empty = "CvJ1PKM8EW8="
    private const val secret = "mJn':4Nbk};AMVFGEWiY!(8&gp1xOv@/"

    fun encrypt(cleartext: String): String {
        val key = SecretKeyFactory
            .getInstance("DES")
            .generateSecret(DESKeySpec(secret.toByteArray()))

        return Cipher.getInstance("DES").run {
            init(Cipher.ENCRYPT_MODE, key)
            Base64.getEncoder().encodeToString(doFinal(cleartext.toByteArray()))
        }
    }

    fun decrypt(ciphertext: String): String {
        val key = SecretKeyFactory
            .getInstance("DES")
            .generateSecret(DESKeySpec(secret.toByteArray()))

        return Cipher.getInstance("DES").run {
            init(Cipher.DECRYPT_MODE, key)
            String(doFinal(Base64.getMimeDecoder().decode(ciphertext)))
        }
    }
}
