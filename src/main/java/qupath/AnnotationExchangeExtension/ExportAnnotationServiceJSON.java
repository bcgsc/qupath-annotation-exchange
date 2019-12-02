package qupath.AnnotationExchangeExtension;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.imagej.detect.tissue.SimpleTissueDetection2;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.objects.PathAnnotationObject;
import qupath.lib.objects.classes.PathClassFactory;
import qupath.lib.objects.hierarchy.PathObjectHierarchy;
import qupath.lib.plugins.PluginRunnerFX;
import qupath.lib.roi.LineROI;
import qupath.lib.roi.PointsROI;
import qupath.lib.roi.PolygonROI;


import java.io.File;
import java.io.FileReader;


/**
 * Created by cschlosser on 25/06/2018.
 */
public class ExportAnnotationServiceJSON implements PathCommand{


    private QuPathGUI qupath;
    final private static Logger logger = LoggerFactory.getLogger(ImportAnnotationServiceJSON.class);


    public ExportAnnotationServiceJSON(QuPathGUI qupath){

        this.qupath = qupath;

    }

    public void run(){

        QuPathViewer viewer = qupath.getViewer();
        if (viewer == null || viewer.getServer() == null)
        {
            logger.error("No Slide Loaded.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON Annotation", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);
        //The annotation service code looks for files with the slide's name to import the annotation, so the file must contain the slide name.
        fileChooser.setInitialFileName(qupath.getViewer().getServer().getDisplayedImageName() + "_imported");
        File inputFile = fileChooser.showSaveDialog(null );

        if(inputFile == null){
            logger.error("No JSON File Selected");
            return;
        }

        PluginRunnerFX runner = new PluginRunnerFX(qupath,false);
        ExportAnnotationServiceJSONPlugin exportJSON = new ExportAnnotationServiceJSONPlugin(inputFile);
        exportJSON.runPlugin(runner, null);

    }





}
