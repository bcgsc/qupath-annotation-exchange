package qupath.AnnotationExchangeExtension;

import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.gui.helpers.DisplayHelpers;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.objects.PathAnnotationObject;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.helpers.PathObjectTools;
import qupath.lib.plugins.PluginRunnerFX;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by cschlosser on 25/06/2018.
 */
public class ExportAnnotationServiceJSON implements PathCommand{

    private QuPathGUI qupath;
    final private static Logger logger = LoggerFactory.getLogger(ImportAnnotationServiceJSON.class);
    private final String commandName = "Export JSON Annotation";

    /**
     * @return Returns the commandName assigned to this class
     */
    public String commandName() {
        return commandName;
    }

    public ExportAnnotationServiceJSON(QuPathGUI qupath){
        this.qupath = qupath;
    }

    public Collection<Class<? extends PathObject>> getSupportedParentObjectClasses() {
        List<Class<? extends PathObject>> list = new ArrayList<>(1);
        list.add(PathAnnotationObject.class);
        return list;
    }

    public void run(){
        QuPathViewer viewer = qupath.getViewer();
        if (viewer == null || viewer.getServer() == null) {
            logger.error("No Slide Loaded.");
            return;
        }

        PluginRunnerFX runner = new PluginRunnerFX(qupath,false);

        Collection<PathObject> selectedObjects = runner.getImageData()
            .getHierarchy()
            .getSelectionModel()
            .getSelectedObjects();

        Collection<Class<? extends PathObject>> supported = getSupportedParentObjectClasses();

        Collection<? extends PathObject> objects = PathObjectTools.getSupportedObjects(selectedObjects, supported);

        if (objects.size() == 0) {
            DisplayHelpers.showErrorMessage(
                "No Annotations Selected",
                "Please select annotations to export in the \"Annotations\" tab"
            );
            return;
        }

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
            "JSON Annotation",
            "*.json"
        );
        fileChooser.getExtensionFilters().add(extFilter);
        /**
         * The annotation service code looks for files with the slide's name to import the annotation, so the file must
         * contain the slide name.
         */
        final String slideName = qupath.getViewer().getServer().getDisplayedImageName();
        fileChooser.setInitialFileName(slideName + ".svs.annotations");
        File inputFile = fileChooser.showSaveDialog(null );

        if (inputFile == null) {
            logger.error("No JSON File Selected");
            return;
        }

        ExportAnnotationServiceJSONPlugin exportJSON = new ExportAnnotationServiceJSONPlugin(inputFile, slideName);
        exportJSON.runPlugin(runner, null);
    }
}
