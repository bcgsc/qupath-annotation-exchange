package qupath.AnnotationExchangeExtension;

import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.plugins.PluginRunnerFX;

import java.io.File;

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

    public void run(){
        QuPathViewer viewer = qupath.getViewer();
        if (viewer == null || viewer.getServer() == null) {
            logger.error("No Slide Loaded.");
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
        fileChooser.setInitialFileName(qupath.getViewer().getServer().getDisplayedImageName() + "_imported");
        File inputFile = fileChooser.showSaveDialog(null );

        if (inputFile == null) {
            logger.error("No JSON File Selected");
            return;
        }

        PluginRunnerFX runner = new PluginRunnerFX(qupath,false);
        ExportAnnotationServiceJSONPlugin exportJSON = new ExportAnnotationServiceJSONPlugin(inputFile);
        exportJSON.runPlugin(runner, null);
    }
}
