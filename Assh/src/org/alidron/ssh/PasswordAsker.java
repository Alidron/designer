/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.alidron.ssh;

import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;
import org.netbeans.api.keyring.Keyring;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 *
 * @author lexa
 */
public class PasswordAsker implements PasswordFinder {

    private int counter = 0;
    private boolean cancelled = false;

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(
            value="PZLA_PREFER_ZERO_LENGTH_ARRAYS",
            justification="Returning null means user cancelled")
    public char[] reqPassword(Resource<?> rsrc) {
        if(cancelled)
            return null;
        
        String key = "ssh://" + rsrc.getDetail().toString();
        char[] pwd = Keyring.read(key);
        
        if((pwd == null) || (counter > 0)) {
            PasswordAskerForm form = this.promptForPassword(rsrc);
            if(form != null) {
                pwd = form.getInputPassword();
                if(form.shouldSavePassword())
                    Keyring.save(key, pwd.clone(), "Alidron credential for " + key);
            }
        }
        
        if(pwd != null)
            counter++;
        else
            cancelled = true;
        
        return pwd;
    }
    
    private PasswordAskerForm promptForPassword(Resource<?> rsrc) {
        PasswordAskerForm form = new PasswordAskerForm();
        String title = String.format("Password for %s", rsrc.getDetail().toString());
        
        DialogDescriptor input = new DialogDescriptor(form, title);
        if (DialogDisplayer.getDefault().notify(input) != DialogDescriptor.OK_OPTION) {
            return null;
        }
        
        return form;
    }

    @Override
    public boolean shouldRetry(Resource<?> rsrc) {
        return counter < 3;
    }

}
