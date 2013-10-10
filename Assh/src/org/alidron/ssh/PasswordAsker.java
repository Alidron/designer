/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.alidron.ssh;

import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;
import org.netbeans.api.keyring.Keyring;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author lexa
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="PZLA_PREFER_ZERO_LENGTH_ARRAYS", justification="Returning null means user cancelled")
public class PasswordAsker implements PasswordFinder {

    private int counter = 0;
    private boolean cancelled = false;

    @Override
    public char[] reqPassword(Resource<?> rsrc) {
        if(cancelled)
            return null;
        
        String key = "ssh://" + rsrc.getDetail().toString();
        char[] pwd = Keyring.read(key);
        
        if((pwd == null) || (counter > 0)) {
            pwd = this.promptForPassword(rsrc);
            if(pwd != null) {
                Keyring.save(key, pwd.clone(), "Alidron credential for " + key);
            }
        }
        
        if(pwd != null)
            counter++;
        else
            cancelled = true;
        
        return pwd;
    }
    
    private char[] promptForPassword(Resource<?> rsrc) {
        String txt = "Password (warning, clear text): ";
        String title = String.format("Password for %s", rsrc.getDetail().toString());
        
        NotifyDescriptor.InputLine input = new NotifyDescriptor.InputLine(txt, title);
        if (DialogDisplayer.getDefault().notify(input) != NotifyDescriptor.OK_OPTION) {
            return null;
        }
        
        return input.getInputText().toCharArray();
    }

    @Override
    public boolean shouldRetry(Resource<?> rsrc) {
        return counter < 3;
    }

}
