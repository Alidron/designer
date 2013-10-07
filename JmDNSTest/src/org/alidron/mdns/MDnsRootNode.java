/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.alidron.mdns;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.jmdns.JmDNS;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 *
 * @author lexa
 */
public class MDnsRootNode extends AbstractNode {
    
    private final JmDNS jmdns;
    
    private static JmDNS createJmDNS() throws UnknownHostException, IOException {
        InetAddress addr = InetAddress.getLocalHost();
        String hostname = InetAddress.getByName(addr.getHostName()).toString();
        return JmDNS.create(addr, hostname);
    }

    public MDnsRootNode() throws IOException {
        this(createJmDNS());
    }
    
    private MDnsRootNode(JmDNS jmdns) {
        super(Children.create(new ServiceTypeFactory(jmdns), true));
        this.jmdns = jmdns;
    }
    
    @Override
    public String getDisplayName() {
        return "mDNS";
    }

    /*@Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("ch/cern/en/ice/fwRDBAPIInspector/TraceTree/nodes/icons/database.png");
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
    @Override
    public Action[] getActions(boolean bln) {
        List<? extends Action> actions = Utilities.actionsForPath("Actions/DBSchemasActions");
        return actions.toArray(new Action[actions.size()]);
    }*/

    @Override
    public PropertySet[] getPropertySets() {
        return new PropertySet[0];
    }

    @Override
    public boolean canRename() {
        return false;
    }

    @Override
    public boolean canDestroy() {
        return false;
    }

    @Override
    public boolean canCopy() {
        return false;
    }

    @Override
    public boolean canCut() {
        return false;
    }
}
