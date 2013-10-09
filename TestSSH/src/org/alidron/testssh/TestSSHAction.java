/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.alidron.testssh;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.common.KeyType;
import net.schmizz.sshj.common.SecurityUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.AuthPassword;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import net.schmizz.sshj.userauth.method.PasswordResponseProvider;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;
import org.alidron.testssh.spi.SSHCookie;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

@ActionID(
        category = "SSHActions",
        id = "org.alidron.testssh.TestSSHAction"
)
@ActionRegistration(
        displayName = "#CTL_TestSSHAction"
)
@Messages("CTL_TestSSHAction=Test SSH connection")
public final class TestSSHAction implements ActionListener {

    private final SSHCookie context;

    public TestSSHAction(SSHCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                runMySSH();
            }
        });
    }
    
    private void runMySSH() {
        ProgressHandle handle = ProgressHandleFactory.createHandle("Test SSH Connection");
        handle.start(10);
        
        try {
            Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
            field.setAccessible(true);
            field.set(null, java.lang.Boolean.FALSE);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        handle.progress("Get server/port info", 1);
        String server = context.getServer();
        int port = context.getPort();

        handle.progress("Get username info", 2);
        String txt = "Username:";
        String title = String.format("Username for %s", server);

        NotifyDescriptor.InputLine input = new NotifyDescriptor.InputLine(txt, title);
        input.setInputText(System.getProperty("user.name")); // specify a default name
        if (DialogDisplayer.getDefault().notify(input) != NotifyDescriptor.OK_OPTION) {
            handle.finish();
            return;
        }
        String username = input.getInputText();

        try {
            final SSHClient ssh = new SSHClient();
            
            handle.progress("Load known hosts", 3);
            ssh.loadKnownHosts();
            loadKnownHostsConsole(ssh);
            //ssh.addHostKeyVerifier(new PromiscuousVerifier());

            PasswordFinder pf = new PasswordFinder() {

                private int counter = 0;

                @Override
                public char[] reqPassword(Resource<?> rsrc) {
                    String txt = "Password (warning, clear text): ";
                    String title = String.format("Password for %s", rsrc.getDetail().toString());

                    NotifyDescriptor.InputLine input = new NotifyDescriptor.InputLine(txt, title);
                    if (DialogDisplayer.getDefault().notify(input) != NotifyDescriptor.OK_OPTION) {
                        return null;
                    }

                    counter++;
                    return input.getInputText().toCharArray();
                }

                @Override
                public boolean shouldRetry(Resource<?> rsrc) {
                    return counter < 3;
                }
            };

            handle.progress("SSH connect...", 4);
            ssh.connect(server, port);
            try {
                handle.progress("Authentication", 5);
                final String base = System.getProperty("user.home") + File.separator + ".ssh" + File.separator;
                ssh.auth(username,
                        new AuthPublickey(ssh.loadKeys(base + "id_rsa", pf)),
                        new AuthPublickey(ssh.loadKeys(base + "id_dsa", pf)),
                        new AuthPassword(pf),
                        new AuthKeyboardInteractive(new PasswordResponseProvider(pf)));

                handle.progress("Start session", 6);
                final Session session = ssh.startSession();
                try {
                    handle.progress("Send command: ping -c 1 google.com", 7);
                    final Session.Command cmd = session.exec("ping -c 1 google.com");
                    System.out.println(IOUtils.readFully(cmd.getInputStream()).toString());
                    handle.progress("Join on command", 8);
                    cmd.join(5, TimeUnit.SECONDS);
                    System.out.println("\n** exit status: " + cmd.getExitStatus());
                } finally {
                    handle.progress("Closing session", 9);
                    session.close();
                }
            } finally {
                handle.progress("SSH disconnect", 10);
                ssh.disconnect();
                
                handle.finish();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void loadKnownHostsConsole(SSHClient ssh) throws IOException {
        boolean loaded = false;
        final File sshDir = OpenSSHKnownHosts.detectSSHDir();
        if (sshDir != null) {
            for (File loc : Arrays.asList(new File(sshDir, "known_hosts"), new File(sshDir, "known_hosts2"))) {
                try {
                    ssh.addHostKeyVerifier(new MyKnownHostVerifier(loc));
                    loaded = true;
                } catch (IOException e) {
                    // Ignore for now
                }
            }
        }
        if (!loaded) {
            throw new IOException("Could not load known_hosts");
        }
    }

    class MyKnownHostVerifier extends OpenSSHKnownHosts {

        public MyKnownHostVerifier(File khFile) throws IOException {
            super(khFile);
        }

        @Override
        protected boolean hostKeyUnverifiableAction(String hostname, PublicKey key) {
            final KeyType type = KeyType.fromKey(key);

            String msg = String.format("The authenticity of host '%s' can't be established.\n"
                    + "%s key fingerprint is %s.\n\nAre you sure you want to continue connecting?",
                    hostname, type, SecurityUtils.getFingerprint(key));
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);
            System.out.println("Notify");
            if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION) {
                entries().add(new OpenSSHKnownHosts.SimpleEntry(null, hostname, KeyType.fromKey(key), key));
                try {
                    write();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    System.out.printf("## Warning: Could not add '%s' (%s) to the list of known hosts.\n", hostname, type);
                    return true;
                }
                System.out.printf("## Warning: Permanently added '%s' (%s) to the list of known hosts.\n", hostname, type);
                return true;
            } else {
                System.out.println("## Warning: rejecting key");
                return false;
            }
        }

        @Override
        protected boolean hostKeyChangedAction(OpenSSHKnownHosts.HostEntry entry, String hostname, PublicKey key) {
            final KeyType type = KeyType.fromKey(key);
            final String fp = SecurityUtils.getFingerprint(key);
            final String path = getFile().getAbsolutePath();
            String msg = String.format(
                    "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n"
                    + "@    WARNING: REMOTE HOST IDENTIFICATION HAS CHANGED!     @\n"
                    + "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n"
                    + "IT IS POSSIBLE THAT SOMEONE IS DOING SOMETHING NASTY!\n"
                    + "Someone could be eavesdropping on you right now (man-in-the-middle attack)!\n"
                    + "It is also possible that the host key has just been changed.\n"
                    + "The fingerprint for the %s key sent by the remote host is\n"
                    + "%s.\n"
                    + "Please contact your system administrator or"
                    + "add correct host key in %s to get rid of this message.\n",
                    type, fp, path);
            NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return false;
        }

    }
}
