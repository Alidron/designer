/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.alidron.mdns;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "MDnsActions",
        id = "org.alidron.mdns.RefreshAction"
)
@ActionRegistration(
        displayName = "#CTL_RefreshAction"
)
@Messages("CTL_RefreshAction=Refresh")
public final class RefreshAction extends AbstractAction {
    
    public interface Refreshable {
        
        public void refresh();
        
    }
    
    private final Refreshable refreshable;
    
    public RefreshAction(Refreshable refreshable) {
        this.refreshable = refreshable;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try{
            this.refreshable.refresh();
        }catch(Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
