/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.alidron.testssh;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.alidron.ssh.spi.SSHProvider;
import org.alidron.testssh.spi.SSHCookie;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
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
                try {
                    runMySSH();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }

    private void runMySSH() throws IOException {
        ProgressHandle handle = ProgressHandleFactory.createHandle("Test SSH Connection");
        handle.start(6);
        
        handle.progress("SSH connect", 1);
        SSHProvider sshProvider = Lookup.getDefault().lookup(SSHProvider.class);
        SSHClient ssh = sshProvider.connect(context.getServer(), context.getPort());
        try {
            handle.progress("Start session", 2);
            Session session = ssh.startSession();
            try {
                handle.progress("Send command: ping -c 1 google.com", 3);
                final Session.Command cmd = session.exec("ping -c 1 google.com");
                System.out.println(IOUtils.readFully(cmd.getInputStream()).toString());
                handle.progress("Join on command", 4);
                cmd.join(5, TimeUnit.SECONDS);
                System.out.println("\n** exit status: " + cmd.getExitStatus());
            } finally {
                handle.progress("Closing session", 5);
                session.close();
            }
        } finally {
            handle.progress("SSH disconnect", 6);
            ssh.disconnect();
            handle.finish();
        }
        
        NotifyDescriptor nd = new NotifyDescriptor.Message("SSH test connection succeeded!", NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notify(nd);
    }

}
