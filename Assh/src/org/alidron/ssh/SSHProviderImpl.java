/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.alidron.ssh;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.AuthPassword;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import net.schmizz.sshj.userauth.method.PasswordResponseProvider;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import org.alidron.ssh.spi.SSHProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lexa
 */
@ServiceProvider(service = SSHProvider.class)
public class SSHProviderImpl extends SSHProvider {

    private static final boolean initDone = init();

    private static boolean init() {
        // Change security policy
        try {
            Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
            field.setAccessible(true);
            field.set(null, java.lang.Boolean.FALSE);
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (NoSuchFieldException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        }

        return true;
    }

    @Override
    public SSHClient connect(String server, int port) throws IOException {
        String txt = "Username:";
        String title = String.format("Username for %s", server);

        NotifyDescriptor.InputLine input = new NotifyDescriptor.InputLine(txt, title);
        input.setInputText(System.getProperty("user.name")); // specify a default name
        if (DialogDisplayer.getDefault().notify(input) != NotifyDescriptor.OK_OPTION) {
            return null;
        }
        String username = input.getInputText();

        SSHClient ssh = new SSHClient();
        this.loadKnownHostsInteractive(ssh);
        ssh.connect(server, port);

        try {
            final String base = System.getProperty("user.home") + File.separator + ".ssh" + File.separator;
            PasswordFinder pf = new PasswordAsker();
            ssh.auth(username,
                    new AuthPublickey(ssh.loadKeys(base + "id_rsa", pf)),
                    new AuthPublickey(ssh.loadKeys(base + "id_dsa", pf)),
                    new AuthPassword(pf),
                    new AuthKeyboardInteractive(new PasswordResponseProvider(pf)));
        } catch (UserAuthException ex) {
            ssh.disconnect();
            throw ex;
        } catch (TransportException ex) {
            ssh.disconnect();
            throw ex;
        }

        return ssh;
    }

    private void loadKnownHostsInteractive(SSHClient ssh) throws IOException {
        boolean loaded = false;
        final File sshDir = OpenSSHKnownHosts.detectSSHDir();
        if (sshDir != null) {
            for (File loc : Arrays.asList(new File(sshDir, "known_hosts"), new File(sshDir, "known_hosts2"))) {
                try {
                    ssh.addHostKeyVerifier(new OpenSSHKnownHostsInteractive(loc));
                    loaded = true;
                } catch (IOException ex) {
                    // Ignore for now
                }
            }
        }
        if (!loaded) {
            throw new IOException("Could not load known_hosts");
        }
    }

}
