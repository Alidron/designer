/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.alidron.mdns;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceTypeListener;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author lexa
 */
class ServiceTypeFactory extends ChildFactory<ServiceEvent> implements ServiceTypeListener {
    
    private List<ServiceEvent> toPopulate;
    private final JmDNS jmdns;

    public ServiceTypeFactory(JmDNS jmdns) {
        this.jmdns = jmdns;
    }

    @Override
    protected boolean createKeys(List<ServiceEvent> toPopulate) {
        this.toPopulate = toPopulate;
        try {
            this.jmdns.addServiceTypeListener(this);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return true;
    }

    @Override
    protected Node createNodeForKey(ServiceEvent key) {
        return new ServiceEventNode(this.jmdns, key);
    }

    @Override
    public void serviceTypeAdded(ServiceEvent se) {
        toPopulate.add(se);
    }

    @Override
    public void subTypeForServiceTypeAdded(ServiceEvent se) {
        toPopulate.add(se);
    }

}
