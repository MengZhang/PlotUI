package org.agmip.ui.plotui;

import java.awt.Color;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.DragSource;
import org.apache.pivot.wtk.DropAction;
import org.apache.pivot.wtk.DropTarget;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.LocalManifest;
import org.apache.pivot.wtk.Manifest;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Visual;
import org.apache.pivot.wtk.media.Image;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 *
 * @author Meng Zhang
 */
public class DragDropFactory {

//    private static final Logger LOG = LoggerFactory.getLogger(DragDropFactory.class);

    public static Label[] setLabelDragDrop(Map<String, Object> ns, String... ids) {

        Label[] dragDropLabels = new Label[ids.length];
        for (int i = 0; i < ids.length; i++) {
            dragDropLabels[i] = (Label) ns.get(ids[i]);
        }

        DragSource ds = createLabelDragSource();
        DropTarget dt = createLabelDropTarget();

        for (Label dragDropLabel : dragDropLabels) {
            dragDropLabel.setDragSource(ds);
            dragDropLabel.setDropTarget(dt);
        }

        return dragDropLabels;
    }

    public static ImageView[] setImageViewDragDrop(Map<String, Object> ns, String... ids) {

        ImageView[] dragDropImageViews = new ImageView[ids.length];
        for (int i = 0; i < ids.length; i++) {
            dragDropImageViews[i] = (ImageView) ns.get(ids[i]);
        }

        DragSource ds = createImageViewDragSource();
        DropTarget dt = createImageViewDropTarget();

        for (ImageView dragDropImageView : dragDropImageViews) {
            dragDropImageView.setDragSource(ds);
            dragDropImageView.setDropTarget(dt);
        }

        return dragDropImageViews;
    }

    public static DragSource createLabelDragSource() {
        return new DragSource() {
            private String text = null;
            private LocalManifest content = null;
            private Color color = null;

            @Override
            public boolean beginDrag(Component comp, int x, int y) {
                Label label = (Label) comp;
                this.text = label.getText();
                this.color = (Color) label.getStyles().get("color");

                if (this.text != null) {
//                    if (label.getName().startsWith("gcmCatLabels")) {
////                        System.out.println("OK");
//                        
//                    }
                    label.setText((String) null);
                    this.content = new LocalManifest();
                    this.content.putText(this.text);
                    this.content.putValue("color", this.color);
                    this.content.putValue("source", label);
                }

                return (this.text != null);
            }

            @Override
            public void endDrag(Component comp, DropAction dropAction) {
                Label label = (Label) comp;
                if (dropAction == null || !label.getName().startsWith("gcmCatLabels")) {
                    label.setText(this.text);
                    label.getStyles().put("color", this.color);
                }
                
                this.text = null;
                this.content = null;
                this.color = null;
                
            }

            @Override
            public boolean isNative() {
                return false;
            }

            @Override
            public LocalManifest getContent() {
                return this.content;
            }

            @Override
            public Visual getRepresentation() {
                return null;
            }

            @Override
            public Point getOffset() {
                return null;
            }

            @Override
            public int getSupportedDropActions() {
                return DropAction.MOVE.getMask();
            }
        };
    }

    public static DropTarget createLabelDropTarget() {
        return new DropTarget() {
            @Override
            public DropAction dragEnter(Component comp, Manifest dragContent,
                    int supportedDropActions, DropAction userDropAction) {
                DropAction dropAction = null;

//                Label label = (Label) comp;
                if ( //                        label.getText() == null &&
                        dragContent.containsText()
                        && DropAction.MOVE.isSelected(supportedDropActions)) {
                    dropAction = DropAction.MOVE;
                    comp.getStyles().put("backgroundColor", "white");
                }

                return dropAction;
            }

            @Override
            public void dragExit(Component comp) {
                comp.getStyles().put("backgroundColor", null);
            }

            @Override
            public DropAction dragMove(Component comp, Manifest dragContent,
                    int supportedDropActions, int x, int y, DropAction userDropAction) {
                Label label = (Label) comp;
                return (label.getText() == null
                        && dragContent.containsText() ? DropAction.MOVE : null);
            }

            @Override
            public DropAction userDropActionChange(Component comp, Manifest dragContent,
                    int supportedDropActions, int x, int y, DropAction userDropAction) {
                Label label = (Label) comp;
                return (label.getText() == null
                        && dragContent.containsText() ? DropAction.MOVE : null);
            }

            @Override
            public DropAction drop(Component comp, Manifest dragContent,
                    int supportedDropActions, int x, int y, DropAction userDropAction) {
                DropAction dropAction = null;
                try {
                    Label target = (Label) comp;
                    Label source = (Label) dragContent.getValue("source");
                    if (target != source
                            && dragContent.containsText()) {

                        String targetText = target.getText();
                        Object targetColor = target.getStyles().get("color");
                        if (target.getName().startsWith("gcmCatLabels")) {
                            target.setText(dragContent.getText());
                            target.getStyles().put("color", dragContent.getValue("color"));
                            if (source.getName().startsWith("gcmCatLabels")) {
                                source.setText(targetText);
                                source.getStyles().put("color", targetColor);
                            }
                        } else {
                            if (!source.getName().startsWith("gcmCatLabels")) {
                                target.setText(dragContent.getText());
                                target.getStyles().put("color", dragContent.getValue("color"));
                                source.setText(targetText);
                                source.getStyles().put("color", targetColor);
                            }
                        }
                        dropAction = DropAction.MOVE;

                    }
                } catch (IOException exception) {
                    System.err.println(exception);
                }

                dragExit(comp);

                return dropAction;
            }
        };
    }

    public static DragSource createImageViewDragSource() {
        return new DragSource() {
            private Image image = null;
            private Point offset = null;
            private LocalManifest content = null;

            @Override
            public boolean beginDrag(Component comp, int x, int y) {
                ImageView imageView = (ImageView) comp;
                this.image = imageView.getImage();

                if (this.image != null) {
                    imageView.setImage((Image) null);
                    this.content = new LocalManifest();
                    this.content.putImage(this.image);
                    this.offset = new Point(x - (imageView.getWidth() - this.image.getWidth()) / 2,
                            y - (imageView.getHeight() - this.image.getHeight()) / 2);
                }

                return (this.image != null);
            }

            @Override
            public void endDrag(Component comp, DropAction dropAction) {
                if (dropAction == null) {
                    ImageView imageView = (ImageView) comp;
                    imageView.setImage(this.image);
                }

                this.image = null;
                this.offset = null;
                this.content = null;
            }

            @Override
            public boolean isNative() {
                return false;
            }

            @Override
            public LocalManifest getContent() {
                return this.content;
            }

            @Override
            public Visual getRepresentation() {
                return this.image;
            }

            @Override
            public Point getOffset() {
                return this.offset;
            }

            @Override
            public int getSupportedDropActions() {
                return DropAction.MOVE.getMask();
            }
        };

    }

    public static DropTarget createImageViewDropTarget() {
        return new DropTarget() {
            @Override
            public DropAction dragEnter(Component comp, Manifest dragContent,
                    int supportedDropActions, DropAction userDropAction) {
                DropAction dropAction = null;

                ImageView imageView = (ImageView) comp;
                if (imageView.getImage() == null
                        && dragContent.containsImage()
                        && DropAction.MOVE.isSelected(supportedDropActions)) {
                    dropAction = DropAction.MOVE;
                    comp.getStyles().put("backgroundColor", "#f0e68c");
                }

                return dropAction;
            }

            @Override
            public void dragExit(Component comp) {
                comp.getStyles().put("backgroundColor", null);
            }

            @Override
            public DropAction dragMove(Component comp, Manifest dragContent,
                    int supportedDropActions, int x, int y, DropAction userDropAction) {
                ImageView imageView = (ImageView) comp;
                return (imageView.getImage() == null
                        && dragContent.containsImage() ? DropAction.MOVE : null);
            }

            @Override
            public DropAction userDropActionChange(Component comp, Manifest dragContent,
                    int supportedDropActions, int x, int y, DropAction userDropAction) {
                ImageView imageView = (ImageView) comp;
                return (imageView.getImage() == null
                        && dragContent.containsImage() ? DropAction.MOVE : null);
            }

            @Override
            public DropAction drop(Component comp, Manifest dragContent,
                    int supportedDropActions, int x, int y, DropAction userDropAction) {
                DropAction dropAction = null;

                ImageView imageView = (ImageView) comp;
                if (imageView.getImage() == null
                        && dragContent.containsImage()) {
                    try {
                        imageView.setImage(dragContent.getImage());
                        dropAction = DropAction.MOVE;
                    } catch (IOException exception) {
                        System.err.println(exception);
                    }
                }

                dragExit(comp);

                return dropAction;
            }
        };
    }
}
