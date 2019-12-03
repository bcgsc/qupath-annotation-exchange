package qupath.AnnotationExchangeExtension;

import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.Tooltip;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.gui.extensions.QuPathExtension;
import qupath.lib.gui.icons.PathIconFactory;

import java.net.URL;

public class AnnotationExchangeExtension implements QuPathExtension{

    final private static Logger logger = LoggerFactory.getLogger(AnnotationExchangeExtension.class);

    /**
     * Returns an instance of a MenuItem using the `QuPathGUI.createMenuItem` function, which requires a call to
     * `QuPathGUI.createCommandAction`
     *
     * @param pathCommand An instance of the class to execute when the user clicks the corresponding menu item
     * @param menuItemName The name of the menu item that will be displayed to the user
     * @return Processed MenuItem from QuPathGUI.createMenuItem
     */
    private static MenuItem addQuPathMenuItem(
            PathCommand pathCommand,
            String menuItemName
    ) {
        return QuPathGUI.createMenuItem(
                QuPathGUI.createCommandAction(
                        pathCommand,
                        menuItemName,
                        PathIconFactory.createNode(
                            QuPathGUI.iconSize,
                            QuPathGUI.iconSize,
                            PathIconFactory.PathIcons.ANNOTATIONS
                        ),
                        null
                )
        );
    };

    public static void addQuPathCommands(final QuPathGUI qupath) {
        ImportXMLAnnotation importXMLAnnotation = new ImportXMLAnnotation(qupath);
        ImportAnnotationServiceJSON importJSONAnnotation = new ImportAnnotationServiceJSON(qupath);
        ExportAnnotationServiceJSON exportJSONAnnotation = new ExportAnnotationServiceJSON(qupath);

        // Add buttons to toolbar
        qupath.addToolbarSeparator();

        try {
            ImageView imageView = new ImageView(getAnnotationExchangeIcon(QuPathGUI.iconSize, QuPathGUI.iconSize));
            Button btnAnnotationExchange = new Button();
            btnAnnotationExchange.setGraphic(imageView);
            btnAnnotationExchange.setTooltip(new Tooltip("LMD Contour Export"));
            ContextMenu popup = new ContextMenu();
            popup.getItems().addAll(
                addQuPathMenuItem(importXMLAnnotation, importXMLAnnotation.commandName()),
                addQuPathMenuItem(importJSONAnnotation, importJSONAnnotation.commandName()),
                addQuPathMenuItem(exportJSONAnnotation, exportJSONAnnotation.commandName())
            );
            btnAnnotationExchange.setOnMouseClicked(e -> {
                popup.show(btnAnnotationExchange, e.getScreenX(), e.getScreenY());
            });

            qupath.addToolbarButton(btnAnnotationExchange);
        } catch (Exception e) {
            logger.error("Error adding toolbar buttons", e);
            qupath.addToolbarCommand(importXMLAnnotation.commandName(), importXMLAnnotation, PathIconFactory.createNode(QuPathGUI.iconSize, QuPathGUI.iconSize, PathIconFactory.PathIcons.ANNOTATIONS));
            qupath.addToolbarCommand(importJSONAnnotation.commandName(), importJSONAnnotation, PathIconFactory.createNode(QuPathGUI.iconSize, QuPathGUI.iconSize, PathIconFactory.PathIcons.ANNOTATIONS));
            qupath.addToolbarCommand(exportJSONAnnotation.commandName(),exportJSONAnnotation, PathIconFactory.createNode(QuPathGUI.iconSize, QuPathGUI.iconSize, PathIconFactory.PathIcons.ANNOTATIONS));
        }

        Menu menuExtension = qupath.getMenu("Extensions>Annotations Exchange", true);
        QuPathGUI.addMenuItems(
            menuExtension,
            addQuPathMenuItem(importXMLAnnotation, importXMLAnnotation.commandName()),
            addQuPathMenuItem(importJSONAnnotation, importJSONAnnotation.commandName()),
            addQuPathMenuItem(exportJSONAnnotation, exportJSONAnnotation.commandName())
        );
    }

    /**
     * Return the image that is the icon for the AnnotationExchange extension
     *
     * @param width Width of the returned icon in pixels
     * @param height Height of the returned icon in pixels
     * @return The icon associated with AnnotationExchange, formatted with the given width and height attributes
     */
    public static Image getAnnotationExchangeIcon(final int width, final int height) {
        try {
            URL in = AnnotationExchangeExtension.class.getResource("AnnotationExchange.gif");
            return new Image(in.toString(), width, height, true, true);
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
