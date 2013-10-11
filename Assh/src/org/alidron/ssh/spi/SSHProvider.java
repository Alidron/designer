/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.alidron.ssh.spi;

import java.io.IOException;
import net.schmizz.sshj.SSHClient;
import org.openide.util.Lookup;

/**
 *
 * @author lexa
 */
public abstract class SSHProvider {
    
    /**
     * Return an {@link net.schmizz.sshj.SSHClient SSHClient} object already connected and authentified.
     * Handle prompting the user for username and password.
     * DO NOT invoke this function on the AWT thread! Use a
     * {@link org.openide.util.RequestProcessor RequestProcessor} or an
     * {@link java.util.concurrent.ExecutorService ExecutorService} instead.
     * 
     * @param server The hostname/ip of the SSH server to connect to.
     * @param port The port of the SSH server to connect to.
     * @return {@link net.schmizz.sshj.SSHClient SSHClient} object or null if user cancelled the operation.
     * @throws java.io.IOException
     */
    public abstract SSHClient connect(String server, int port) throws IOException;
    
    /**
     * Static method to obtain the SSHProvider service.
     * @return the SSHProvider in the system.
     */
    public static SSHProvider getDefault() {
        return Lookup.getDefault().lookup(SSHProvider.class);
    }
    
}
