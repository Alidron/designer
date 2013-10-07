/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.shapesample;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import javax.swing.JComponent;
import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.general.IconNodeWidget;
import org.openide.util.ImageUtilities;

/**
 *
 * @author lexa
 */
public class GraphSceneImpl extends GraphScene<MyNode, String> {
    
    private LayerWidget mainLayer;
    
    private WidgetAction editorAction = ActionFactory.createInplaceEditorAction(new LabelTextFieldEditor());

    public GraphSceneImpl() {
        this.mainLayer = new LayerWidget(this);
        addChild(this.mainLayer);
        
        getActions().addAction(ActionFactory.createAcceptAction(new AcceptProvider() {

            @Override
            public ConnectorState isAcceptable(Widget widget, Point point, Transferable transferable) {
                Image dragImage = getImageFromTransferable(transferable);
                JComponent view = getView();
                Graphics2D g2 = (Graphics2D) view.getGraphics();
                Rectangle visRect = view.getVisibleRect();
                view.paintImmediately(visRect.x, visRect.y, visRect.width, visRect.height);
                g2.drawImage(dragImage,
                        AffineTransform.getTranslateInstance(point.getLocation().getX(),
                                point.getLocation().getY()),
                        null);
                return ConnectorState.ACCEPT;
            }

            @Override
            public void accept(Widget widget, Point point, Transferable transferable) {
                Image image = getImageFromTransferable(transferable);
                Widget w = GraphSceneImpl.this.addNode(new MyNode(image));
                w.setPreferredLocation(widget.convertLocalToScene(point));
            }

        }));
        
        getActions().addAction(ActionFactory.createZoomAction());
        getActions().addAction(ActionFactory.createPanAction());
    }
    
    private Image getImageFromTransferable(Transferable transferable) {
        Object o = null;
        try {
            o = transferable.getTransferData(DataFlavor.imageFlavor);
        } catch (IOException ex) {
        } catch (UnsupportedFlavorException ex) {
        }
        return o instanceof Image ? (Image) o : ImageUtilities.loadImage("org/netbeans/shapesample/palette/shape1.png");
    }

    @Override
    protected Widget attachNodeWidget(MyNode node) {
        IconNodeWidget widget = new IconNodeWidget(this);
        widget.setImage(node.getImage());
        widget.setLabel(Long.toString(node.hashCode()));
        widget.getLabelWidget().getActions().addAction(this.editorAction);
        widget.getActions().addAction(createSelectAction());
        widget.getActions().addAction(ActionFactory.createMoveAction());
        widget.getActions().addAction(createObjectHoverAction());
        mainLayer.addChild(widget);
        return widget;
    }

    @Override
    protected Widget attachEdgeWidget(String edge) {
        return null;
    }

    @Override
    protected void attachEdgeSourceAnchor(String edge, MyNode oldSourceNode, MyNode sourceNode) {
        
    }

    @Override
    protected void attachEdgeTargetAnchor(String edge, MyNode oldTargetNode, MyNode targetNode) {
        
    }
    
}
