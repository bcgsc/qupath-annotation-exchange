package qupath.AnnotationExchangeExtension;

import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.extensions.QuPathExtension;
import qupath.lib.gui.icons.PathIconFactory;

import java.net.URL;


/**
 * Created by cschlosser on 01/06/2017.
 */
public class AnnotationExchangeExtension implements QuPathExtension{

    final private static Logger logger = LoggerFactory.getLogger(AnnotationExchangeExtension.class);


    public static void addQuPathCommands(final QuPathGUI qupath) {
        ImportXMLAnnotation importXMLAnnotation = new ImportXMLAnnotation(qupath);
        ImportAnnotationServiceJSON importJSONAnnotation = new ImportAnnotationServiceJSON(qupath);
        ExportAnnotationServiceJSON exportJSONAnnotation = new ExportAnnotationServiceJSON(qupath);
        // PropagateAnnotations propagateAnnotations = new PropagateAnnotations(qupath);


        // Add buttons to toolbar
        qupath.addToolbarSeparator();

        try {
            ImageView imageView = new ImageView(getAnnotationExchangeIcon(QuPathGUI.iconSize, QuPathGUI.iconSize));
            Button btnAnnotationExchange = new Button();
            btnAnnotationExchange.setGraphic(imageView);
            btnAnnotationExchange.setTooltip(new Tooltip("LMD Contour Export"));
            ContextMenu popup = new ContextMenu();
            popup.getItems().addAll(

                    QuPathGUI.createMenuItem(QuPathGUI.createCommandAction(importXMLAnnotation, "Import XML Annotation", PathIconFactory.createNode(QuPathGUI.iconSize, QuPathGUI.iconSize, PathIconFactory.PathIcons.ANNOTATIONS), null)),
                    QuPathGUI.createMenuItem(QuPathGUI.createCommandAction(importJSONAnnotation, "Import JSON Annotation", PathIconFactory.createNode(QuPathGUI.iconSize, QuPathGUI.iconSize, PathIconFactory.PathIcons.ANNOTATIONS), null)),
                    QuPathGUI.createMenuItem(QuPathGUI.createCommandAction(exportJSONAnnotation, "Export JSON Annotation", PathIconFactory.createNode(QuPathGUI.iconSize, QuPathGUI.iconSize, PathIconFactory.PathIcons.ANNOTATIONS), null))
                    // QuPathGUI.createMenuItem(QuPathGUI.createCommandAction(propagateAnnotations, "Propagate Annotations", PathIconFactory.createNode(QuPathGUI.iconSize, QuPathGUI.iconSize, PathIconFactory.PathIcons.ANNOTATIONS), null))
            );
            btnAnnotationExchange.setOnMouseClicked(e -> {
                popup.show(btnAnnotationExchange, e.getScreenX(), e.getScreenY());
            });


            qupath.addToolbarButton(btnAnnotationExchange);
        } catch (Exception e) {
            logger.error("Error adding toolbar buttons", e);
            qupath.addToolbarCommand("Import XML Annotation", importXMLAnnotation, PathIconFactory.createNode(QuPathGUI.iconSize, QuPathGUI.iconSize, PathIconFactory.PathIcons.ANNOTATIONS));
            qupath.addToolbarCommand("Import JSON Annotation", importJSONAnnotation, PathIconFactory.createNode(QuPathGUI.iconSize, QuPathGUI.iconSize, PathIconFactory.PathIcons.ANNOTATIONS));
            qupath.addToolbarCommand("Export JSON Annotation",exportJSONAnnotation, PathIconFactory.createNode(QuPathGUI.iconSize, QuPathGUI.iconSize, PathIconFactory.PathIcons.ANNOTATIONS));
            // qupath.addToolbarCommand("Propagate Annotations", propagateAnnotations, PathIconFactory.createNode(QuPathGUI.iconSize, QuPathGUI.iconSize, PathIconFactory.PathIcons.ANNOTATIONS));
        }



        Menu menuExtension = qupath.getMenu("Extensions>Annotations Exchange", true);
        QuPathGUI.addMenuItems(menuExtension,
                QuPathGUI.createMenuItem(QuPathGUI.createCommandAction(importXMLAnnotation, "Import XML Annotation", PathIconFactory.createNode(QuPathGUI.iconSize, QuPathGUI.iconSize, PathIconFactory.PathIcons.ANNOTATIONS), null)),
                QuPathGUI.createMenuItem(QuPathGUI.createCommandAction(importJSONAnnotation, "Import JSON Annotation", PathIconFactory.createNode(QuPathGUI.iconSize, QuPathGUI.iconSize, PathIconFactory.PathIcons.ANNOTATIONS), null)),
                QuPathGUI.createMenuItem(QuPathGUI.createCommandAction(exportJSONAnnotation, "Export JSON Annotation", PathIconFactory.createNode(QuPathGUI.iconSize, QuPathGUI.iconSize, PathIconFactory.PathIcons.ANNOTATIONS), null))
                // QuPathGUI.createMenuItem(QuPathGUI.createCommandAction(propagateAnnotations, "Propagate Annotations", PathIconFactory.createNode(QuPathGUI.iconSize, QuPathGUI.iconSize, PathIconFactory.PathIcons.ANNOTATIONS), null))
        );

    }

    /**
     * Try to read the LMDExport icon from its jar.
     *
     * @param width
     * @param height
     * @return
     */
    public static Image getAnnotationExchangeIcon(final int width, final int height) {
        try {
            URL in = AnnotationExchangeExtension.class.getResource("AnnotationExchange.gif");
            return new Image(in.toString(), width, height, true, true);
//            InputStream in = AnnotationExchangeExtension.class.getResourceAsStream("/AnnotationExchange.gif");
//            BufferedImage buffIcon = ImageIO.read(in);
//            ImageView icon = new ImageView( SwingFXUtils.toFXImage(buffIcon, null) );
//            icon.setPreserveRatio(false);
//            icon.setFitHeight(height);
//            icon.setFitWidth(width);
//            return icon.snapshot(null,null);
        } catch (Exception e) {
            logger.error("Unable to load Annotation Exchange icon!", e);
        }
        return null;
    }


    @Override
    public void installExtension(QuPathGUI qupath) {
        addQuPathCommands(qupath);
    }

    @Override
    public String getName() {
        return "Annotation Exchange Extension";
    }

    @Override
    public String getDescription() {
        return "Allows imports of JSON files generated from the GSC Online Annotation Viewer";
    }


}
