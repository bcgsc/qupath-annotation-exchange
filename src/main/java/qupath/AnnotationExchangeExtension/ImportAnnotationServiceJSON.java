package qupath.AnnotationExchangeExtension;

import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.plugins.PluginRunnerFX;

import java.io.File;

public class ImportAnnotationServiceJSON implements PathCommand{

    private QuPathGUI qupath;
    final private static Logger logger = LoggerFactory.getLogger(ImportAnnotationServiceJSON.class);
    private final String commandName = "Import JSON Annotation";

    /**
     * @return Returns the commandName assigned to this class
     */
    public String commandName() {
        return commandName;
    }

    public ImportAnnotationServiceJSON(QuPathGUI qupath){
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
        File inputFile = fileChooser.showOpenDialog(null );

        if (inputFile == null) {
            logger.error("No JSON File Selected");
            return;
        }

        PluginRunnerFX runner = new PluginRunnerFX(qupath,false);
        ImportAnnotationServiceJSONPlugin importJSON = new ImportAnnotationServiceJSONPlugin(inputFile);
        importJSON.runPlugin(runner, null);
    }
}
