package github.hjkim27.util.enc;

import github.hjkim27.exception.EncodingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * <pre>
 *     AES256Util
 *     - Symmetric key algorithms using the same key during encryption/decryption
 * </pre>
 *
 * @author hjkim27
 * @since 0.0.1-SNAPSHOT
 */
@Slf4j
public class AesFileUtil {

    protected static final String AES = "AES";
    protected static final String UTF_8 = StandardCharsets.UTF_8.name();
    protected static final String ENCRYPT_KEY = "hEjNkCiRmY2P7T";

    /**
     * <pre>
     *     AES File Encrypted
     * </pre>
     *
     * @param inputFile  Encrypt target File
     * @param outputFile Encrypted File
     * @throws EncodingException Error occurs during encryption
     */
    public static void encryptFile(File inputFile, File outputFile) throws EncodingException {
        try {
            if (inputFile == null) {
                throw new EncodingException("inputFile:File is null");
            }
            byte[] encryptKeyBytes = ENCRYPT_KEY.getBytes(UTF_8);
            SecretKeySpec secretKeySpec = new SecretKeySpec(encryptKeyBytes, AES);
            Cipher cipher = Cipher.getInstance(AES);

            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] inputFileBytes = Files.readAllBytes(inputFile.toPath());
            byte[] encryptedBytes = cipher.doFinal(inputFileBytes);

            FileUtils.writeByteArrayToFile(outputFile, encryptedBytes);

        } catch (EncodingException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new EncodingException(e);
        }
    }

    /**
     * <pre>
     *     AES File Decrypted
     * </pre>
     *
     * @param inputFile  Decrypt target File
     * @param outputFile Decrypted File
     * @throws EncodingException Error occurs during decryption
     */
    public static void decryptFile(File inputFile, File outputFile) throws EncodingException {
        try {
            if (inputFile == null) {
                throw new EncodingException("inputFile:File is null");
            }
            byte[] encryptionKeyBytes = ENCRYPT_KEY.getBytes(UTF_8);
            SecretKeySpec secretKeySpec = new SecretKeySpec(encryptionKeyBytes, AES);
            Cipher cipher = Cipher.getInstance(AES);

            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] inputFileBytes = Files.readAllBytes(inputFile.toPath());
            byte[] decryptedBytes = cipher.doFinal(inputFileBytes);

            FileUtils.writeByteArrayToFile(outputFile, decryptedBytes);
        } catch (EncodingException e) {
            throw e;
        } catch (Exception e) {
            throw new EncodingException(e);
        }
    }
}
