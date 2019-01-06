package io.triada.models;

import io.triada.models.cli.ShellScript;
import io.triada.models.key.RsaKey;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;

public final class RsaKeyTest extends Assert {

    @Test
    public void testPublicKey() throws Exception {
        final RsaKey rsaKey =
                new RsaKey(
                        ResourceUtils.getFile(
                                this.getClass().getResource("/keys/id_rsa.pub")
                        )
                );
        assertTrue(rsaKey.asPublic().startsWith("MIICI"));
    }

    @Test
    public void testPrivateKey() throws Exception {
        final RsaKey rsaKey =
                new RsaKey(
                        ResourceUtils.getFile(
                                this.getClass().getResource("/keys/id_rsa")
                        )
                );
        assertTrue(rsaKey.asPublic().startsWith("MIIJJ"));
    }

    @Test
    public void test() throws Exception {
        final String s = FileUtils.readFileToString(new File("/home/strogiyotec/Java/id_rsa/id_rsa.pub"), StandardCharsets.UTF_8);
        final ShellScript shellScript = new ShellScript();
        final File tempFile = File.createTempFile("/tmp/", ".tmp");
        System.out.println(tempFile);
        FileUtils.write(
                tempFile,
                s,
                StandardCharsets.UTF_8
        );
        final String s1 = shellScript.executeCommand(
                String.format(
                        "ssh-keygen -f %s -e -m pem",
                        tempFile.getAbsolutePath()
                )
        );
        System.out.println(s1);
    }
}
