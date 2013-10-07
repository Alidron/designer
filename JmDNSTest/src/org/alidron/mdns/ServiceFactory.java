/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.alidron.mdns;

import java.util.HashMap;
import java.util.List;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author lexa
 */
class ServiceFactory extends ChildFactory<ServiceEvent> implements ServiceListener {
    
    private class ServiceKey {
        private final String name, type;
        private final JmDNS jmdns;
        ServiceKey(ServiceEvent se) {
            this.name = se.getName();
            this.type = se.getType();
            this.jmdns = se.getDNS();
        }

        @Override
        public boolean equals(Object o) {
            if(o == null)
                return false;
            
            if(o instanceof ServiceKey) {
                ServiceKey k = (ServiceKey) o;
                return this.jmdns.equals(k.jmdns) && this.name.equals(k.name) && this.type.equals(k.type);
            }else
                return false;
        }
        
        @Override
        public int hashCode () {
            return this.jmdns.hashCode() + this.name.hashCode() + this.type.hashCode();
        }
    }
    
    private final HashMap<ServiceKey, ServiceEvent> serviceList;
    private final JmDNS jmdns;
    private final ServiceEvent serviceType;

    public ServiceFactory(JmDNS jmdns, ServiceEvent serviceType) {
        this.serviceList = new HashMap<ServiceKey, ServiceEvent>();
        this.jmdns = jmdns;
        this.serviceType = serviceType;
        this.jmdns.addServiceListener(serviceType.getType(), this);
    }

    @Override
    protected boolean createKeys(List<ServiceEvent> toPopulate) {
        toPopulate.addAll(this.serviceList.values());
        return true;
    }

    @Override
    protected Node createNodeForKey(ServiceEvent key) {
        return new ServiceEventNode(this.jmdns, key);
    }

    @Override
    public void serviceAdded(ServiceEvent se) {
        System.out.println("Service Added: "+ se.getType() + " ; " + se.getName());
        this.jmdns.requestServiceInfo(se.getType(), se.getName());
        /*this.serviceList.put(new ServiceKey(se), se);
        refresh(true);*/
    }

    @Override
    public void serviceRemoved(ServiceEvent se) {
        System.out.println("Service Removed: "+ se.getType() + " ; " + se.getName());
        this.serviceList.remove(new ServiceKey(se));
        refresh(true);
    }

    @Override
    public void serviceResolved(ServiceEvent se) {
        System.out.println("Service Resolved: "+ se.getType() + " ; " + se.getName());
        this.serviceList.put(new ServiceKey(se), se);
        refresh(true);
    }
    
}
