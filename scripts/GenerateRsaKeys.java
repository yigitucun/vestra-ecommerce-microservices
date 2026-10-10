package scripts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

/**
 * Vestra RSA 2048-bit Key Pair Generator
 * Usage:
 *   java scripts/GenerateRsaKeys.java [output-directory]
 * Example:
 *   java scripts/GenerateRsaKeys.java certs
 */
public class GenerateRsaKeys {

    public static void main(String[] args) {
        String outputDirStr = args.length > 0 ? args[0] : "certs";
        Path outputDir = Paths.get(outputDirStr);

        try {
            System.out.println("Generating 2048-bit RSA key pair...");
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();

            String privatePem = toPem(kp.getPrivate(), "PRIVATE KEY");
            String publicPem = toPem(kp.getPublic(), "PUBLIC KEY");

            Files.createDirectories(outputDir);
            Path privateKeyPath = outputDir.resolve("private_key.pem");
            Path publicKeyPath = outputDir.resolve("public_key.pem");

            Files.writeString(privateKeyPath, privatePem);
            Files.writeString(publicKeyPath, publicPem);

            System.out.println("SUCCESS: RSA key pair generated successfully!");
            System.out.println("  Private Key : " + privateKeyPath.toAbsolutePath());
            System.out.println("  Public Key  : " + publicKeyPath.toAbsolutePath());
            System.out.println();
            System.out.println("IMPORTANT: Ensure these files are excluded by .gitignore and NEVER committed to Git.");
        } catch (NoSuchAlgorithmException | IOException e) {
            System.err.println("ERROR: Failed to generate keys: " + e.getMessage());
            System.exit(1);
        }
    }

    private static String toPem(Object key, String type) {
        byte[] encoded;
        if (key instanceof PrivateKey pk) {
            encoded = pk.getEncoded();
        } else if (key instanceof PublicKey pbk) {
            encoded = pbk.getEncoded();
        } else {
            throw new IllegalArgumentException("Unsupported key type");
        }

        String base64 = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(encoded);
        return "-----BEGIN " + type + "-----\n" + base64 + "\n-----END " + type + "-----\n";
    }
}
