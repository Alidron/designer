/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.alidron.ssh;

import java.io.File;
import java.io.IOException;
import java.security.PublicKey;
import net.schmizz.sshj.common.KeyType;
import net.schmizz.sshj.common.SecurityUtils;
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;

/**
 *
 * @author lexa
 */
public class OpenSSHKnownHostsInteractive extends OpenSSHKnownHosts {

        public OpenSSHKnownHostsInteractive(File khFile) throws IOException {
            super(khFile);
        }

        @Override
        protected boolean hostKeyUnverifiableAction(String hostname, PublicKey key) {
            final KeyType type = KeyType.fromKey(key);

            String msg = String.format("The authenticity of host '%s' can't be established.%n"
                    + "%s key fingerprint is %s.%n%nAre you sure you want to continue connecting?",
                    hostname, type, SecurityUtils.getFingerprint(key));
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);
            if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION) {
                entries().add(new OpenSSHKnownHosts.SimpleEntry(null, hostname, KeyType.fromKey(key), key));
                try {
                    write();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    System.out.printf("## Warning: Could not add '%s' (%s) to the list of known hosts.%n", hostname, type); // TODO: replace with logger
                    return true;
                }
                System.out.printf("## Warning: Permanently added '%s' (%s) to the list of known hosts.%n", hostname, type); // TODO: replace with logger
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected boolean hostKeyChangedAction(OpenSSHKnownHosts.HostEntry entry, String hostname, PublicKey key) {
            final KeyType type = KeyType.fromKey(key);
            final String fp = SecurityUtils.getFingerprint(key);
            final String path = getFile().getAbsolutePath();
            String msg = String.format(
                    "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@%n"
                    + "@    WARNING: REMOTE HOST IDENTIFICATION HAS CHANGED!     @%n"
                    + "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@%n"
                    + "IT IS POSSIBLE THAT SOMEONE IS DOING SOMETHING NASTY!%n"
                    + "Someone could be eavesdropping on you right now (man-in-the-middle attack)!%n"
                    + "It is also possible that the host key has just been changed.%n"
                    + "The fingerprint for the %s key sent by the remote host is%n"
                    + "%s.%n"
                    + "Please contact your system administrator or"
                    + "add correct host key in %s to get rid of this message.%n",
                    type, fp, path);
            NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return false;
        }
    
}
