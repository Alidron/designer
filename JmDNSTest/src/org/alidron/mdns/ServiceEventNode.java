/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.alidron.mdns;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.swing.Action;
import org.alidron.mdns.RefreshAction.Refreshable;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author lexa
 */
public class ServiceEventNode extends AbstractNode {

    private final JmDNS jmdns;
    private final ServiceEvent event;
    private final InstanceContent ic;

    private final Refreshable refreshable = new Refreshable() {

        @Override
        public void refresh() {
            ServiceEventNode.this.refresh();
        }

    };

    public ServiceEventNode(JmDNS jmdns, ServiceEvent event) {
        this(jmdns, event, new InstanceContent());
    }

    public ServiceEventNode(JmDNS jmdns, ServiceEvent event, InstanceContent ic) {
        super(getChildren(jmdns, event), new AbstractLookup(ic));
        this.jmdns = jmdns;
        this.event = event;
        this.ic = ic;

        if (isServiceType(event)) {
            this.ic.add(this.refreshable);
        }
    }

    private static Children getChildren(JmDNS jmdns, ServiceEvent event) {
        if (isServiceType(event)) {
            return Children.create(new ServiceFactory(jmdns, event), true);
        } else {
            return Children.LEAF;
        }
    }

    private static boolean isServiceType(ServiceEvent event) {
        return (event.getName() == null) || (event.getName().isEmpty());
    }

    private void refresh() {
        setChildren(getChildren(this.jmdns, this.event));
    }

    @Override
    public String getDisplayName() {
        if (isServiceType(event)) {
            return event.getType();
        } else {
            return event.getName();
        }
    }

    @Override
    public Action[] getActions(boolean context) {
        @SuppressWarnings("unchecked") // Because f*ck it, that's why! :-|| See http://docs.oracle.com/javase/tutorial/java/generics/capture.html
        List<Action> actions = (List<Action>) Utilities.actionsForPath("Actions/MDnsActions");
        return actions.toArray(new Action[actions.size()]);
    }
    
    private static <T> Property<T> makeROReflectionProperty(Object instance, Class<T> valueType, String getter, String propName) throws NoSuchMethodException {
        Property<T> prop = new PropertySupport.Reflection<T>(instance, valueType, getter, null);
        prop.setName(propName);
        return prop;
    }

    private class ServiceTypePropertySet extends PropertySet {

        ServiceTypePropertySet() {
            super("Properties", "Service type properties", "Service type properties.");
        }

        @Override
        public Property<?>[] getProperties() {
            try {
                return new Property[]{makeROReflectionProperty(ServiceEventNode.this.event, String.class, "getType", "Service type")};
            } catch (NoSuchMethodException ex) {
                Exceptions.printStackTrace(ex);
                return new Property[0];
            }
        }

    }

    private class ServicePropertySet extends PropertySet {

        ServicePropertySet() {
            super("Properties", "Service properties", "Service properties.");
        }

        @Override
        public Property<?>[] getProperties() {
            try {
                ServiceInfo info = ServiceEventNode.this.event.getInfo();
                List<Property> propList = new ArrayList<Property>();
                
                propList.add(makeROReflectionProperty(ServiceEventNode.this.event, String.class, "getType", "Service type"));
                propList.add(makeROReflectionProperty(ServiceEventNode.this.event, String.class, "getName", "Service name"));
                propList.add(makeROReflectionProperty(info, String.class, "getApplication", "Application"));
                propList.add(makeROReflectionProperty(info, String.class, "getDomain", "Domain"));
                propList.add(new PropertySupport.ReadOnly<String>("Host addresses", String.class, "Host addresses", "Host addresses") {
                    @Override
                    public String getValue() throws IllegalAccessException, InvocationTargetException {
                        return Arrays.toString(ServiceEventNode.this.event.getInfo().getHostAddresses());
                    }
                });
                propList.add(new PropertySupport.ReadOnly<String>("Inet addresses", String.class, "Inet addresses", "Inet addresses") {
                    @Override
                    public String getValue() throws IllegalAccessException, InvocationTargetException {
                        return Arrays.toString(ServiceEventNode.this.event.getInfo().getInetAddresses());
                    }
                });
                propList.add(makeROReflectionProperty(info, String.class, "getNiceTextString", "Domain"));
                propList.add(makeROReflectionProperty(info, Integer.class, "getPort", "Port"));
                propList.add(makeROReflectionProperty(info, Integer.class, "getPriority", "Priority"));
                propList.add(makeROReflectionProperty(info, String.class, "getProtocol", "protocol"));
                propList.add(makeROReflectionProperty(info, String.class, "getQualifiedName", "Qualified name"));
                propList.add(makeROReflectionProperty(info, String.class, "getServer", "Server"));
                propList.add(makeROReflectionProperty(info, String.class, "getSubtype", "Sub type"));
                propList.add(makeROReflectionProperty(info, String.class, "getTypeWithSubtype", "Full type"));
                propList.add(makeROReflectionProperty(info, Integer.class, "getWeight", "Weight"));
                propList.add(new PropertySupport.ReadOnly<String>("URLs", String.class, "URLs", "URLs") {
                    @Override
                    public String getValue() throws IllegalAccessException, InvocationTargetException {
                        return Arrays.toString(ServiceEventNode.this.event.getInfo().getURLs());
                    }
                });
                
                return propList.toArray(new Property<?>[0]);
            } catch (NoSuchMethodException ex) {
                Exceptions.printStackTrace(ex);
                return new Property[0];
            }
        }

    }

    private class SpecificServicePropertySet extends PropertySet {

        SpecificServicePropertySet() {
            super("Properties", "Specific service properties", "Specific service properties.");
        }

        @Override
        public Property<?>[] getProperties() {
            ServiceInfo info = ServiceEventNode.this.event.getInfo();
            List<Property> propList = new ArrayList<Property>();
            
            Enumeration<String> e = info.getPropertyNames();
            while(e.hasMoreElements()) {
                final String propName = e.nextElement();
                propList.add(new PropertySupport.ReadOnly<String>(propName, String.class, propName, propName) {
                    @Override
                    public String getValue() throws IllegalAccessException, InvocationTargetException {
                        return ServiceEventNode.this.event.getInfo().getPropertyString(propName);
                    }
                });
            }
            
            return propList.toArray(new Property<?>[0]);
        }
    }

    private class JmDNSPropertySet extends PropertySet {

        JmDNSPropertySet() {
            super("Local Properties", "From connected network", "Details about the network interface by which we obtained this service type.");
        }

        @Override
        public Property<?>[] getProperties() {
            try {
                JmDNS jmdns = ServiceEventNode.this.event.getDNS();
                List<Property> propList = new ArrayList<Property>();
                
                propList.add(makeROReflectionProperty(jmdns, String.class, "getName", "JmDNS instance name"));
                propList.add(makeROReflectionProperty(jmdns, String.class, "getHostName", "Hostname"));
                propList.add(new PropertySupport.ReadOnly<String>("Interface address", String.class, "Interface address", "Interface address") {
                    @Override
                    public String getValue() throws IllegalAccessException, InvocationTargetException {
                        try {
                            return ServiceEventNode.this.event.getDNS().getInterface().toString();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                            return "Error";
                        }
                    }
                });

                return propList.toArray(new Property<?>[0]);
            } catch (NoSuchMethodException ex) {
                Exceptions.printStackTrace(ex);
                return new Property[0];
            }
        }

    }

    @Override
    public PropertySet[] getPropertySets() {
        if (isServiceType(event)) {
            return new PropertySet[]{new ServiceTypePropertySet(), new JmDNSPropertySet()};
        } else {
            return new PropertySet[]{new ServicePropertySet(), new SpecificServicePropertySet(), new JmDNSPropertySet()};
        }
    }

    /*@Override
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
     }*/
}
