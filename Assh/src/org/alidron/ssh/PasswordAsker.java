/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.alidron.ssh;

import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author lexa
 */
public class PasswordAsker implements PasswordFinder {

    private int counter = 0;

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="PZLA_PREFER_ZERO_LENGTH_ARRAYS", justification="Returning null means user cancelled")
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

}
