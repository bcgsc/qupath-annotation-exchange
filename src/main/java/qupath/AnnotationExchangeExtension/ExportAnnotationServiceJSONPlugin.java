package qupath.AnnotationExchangeExtension;

import com.google.gson.*;
import qupath.lib.common.ColorTools;
import qupath.lib.geom.Point2;
import qupath.lib.images.ImageData;
import qupath.lib.objects.*;
import qupath.lib.objects.helpers.PathObjectTools;
import qupath.lib.plugins.AbstractPlugin;
import qupath.lib.plugins.PluginRunner;
import qupath.lib.roi.PathROIToolsAwt;
import qupath.lib.roi.PolygonROI;
import qupath.lib.roi.interfaces.PathShape;

import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.*;
import java.util.List;

public class ExportAnnotationServiceJSONPlugin extends AbstractPlugin<BufferedImage> {

    private File annotationFile;
    private String lastMessage = "";
    private String fileName = "";

    public ExportAnnotationServiceJSONPlugin( File annotationFile) {
        this.annotationFile = annotationFile;
    }

    public ExportAnnotationServiceJSONPlugin(File annotationFile, String fileName) {
        this.annotationFile = annotationFile;
        this.fileName = fileName;
    }


    @Override
    protected void addWorkflowStep(final ImageData<BufferedImage> imageData, final String arg) {
        // Do nothin
    }

    @Override
    public String getName() {
        return "Export Annotation Service JSON";
    }

    @Override
    public String getDescription() {
        return "Plugin for exporting JSON Data Made in Spotvision / Annotation Service ";
    }

    @Override
    public String getLastResultsDescription() {
        return lastMessage;
    }

    @Override
    protected boolean parseArgument(ImageData<BufferedImage> imageData, String arg) {
        return true;
    }

    public Collection<Class<? extends PathObject>> getSupportedParentObjectClasses() {
        List<Class<? extends PathObject>> list = new ArrayList<>(1);
        list.add(PathAnnotationObject.class);
        return list;
    }

    @Override
    protected Collection<? extends PathObject> getParentObjects(PluginRunner<BufferedImage> runner) {
        return Collections.singleton(runner.getHierarchy().getRootObject());
    }

    @Override
    protected void addRunnableTasks(ImageData<BufferedImage> imageData, PathObject parentObject, List<Runnable> tasks) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                boolean successfulRead = writeJSONAnnotations(annotationFile,imageData);
                if(!successfulRead){lastMessage = "JSON annotations not successfully read";}
            }
        };
        tasks.add(runnable);
    }

    @Override
    protected void preprocess(final PluginRunner<BufferedImage> pluginRunner) {}

    @Override
    protected void postprocess(final PluginRunner<BufferedImage> pluginRunner) {}

    private boolean writeJSONAnnotations(File outputFile, ImageData<BufferedImage> imageData ) {
        //Make filter for supported objects (annotations in this case)
        Collection<Class<? extends PathObject>> supported = getSupportedParentObjectClasses();

        //Get the set of selected objects
        Collection<PathObject> selectedObjects = imageData
                .getHierarchy()
                .getSelectionModel()
                .getSelectedObjects();

        //Filter selected objects for annotations
        Collection<? extends PathObject> objects = PathObjectTools.getSupportedObjects(selectedObjects, supported);

        /**
         * The data-structure of the exported JSON:
         * [
         *   {
         *     "SourceSlide": "name-of-file.svs"
         *   },
         *   {
         *     "dictionaries": [
         *       [
         *         {
         *           "uid": "some-uid",
         *           "name": "some-name",
         *           "imgCoords": [
         *             {
         *               "x": 0.0,
         *               "y": 0.0
         *             },
         *             // ...
         *           ],
         *           // http://paperjs.org/reference/path/
         *           "path": [
         *             "Path",
         *             {
         *               "applyMatrix": true,
         *               "data": {
         *                 "id": "some-uid"
         *               },
         *               "segments": [
         *                 // http://paperjs.org/reference/segment/#segment
         *                 [
         *                   [0.0, 0.0],
         *                   [0.0, 0.0],
         *                   [0.0, 0.0]
         *                 ],
         *                 // ...
         *               ],
         *               "closed": true,
         *               "fillColor": [0.0, 0.0, 0.0, 0.0],
         *               "strokeColor": [0.0, 0.0, 0.0],
         *               "strokeWidth": 50
         *             }
         *           ],
         *           "zoom": 0,
         *           "context": [],
         *           "dictionary": "default"
         *         }
         *       ]
         *     ]
         *   }
         * ]
         */
        try {
            JsonArray arrayToExport = new JsonArray();
            JsonArray dictionariesArray = new JsonArray();

            int count = 0;
            for(PathObject annotation : objects) {
                PathShape pathShape = (PathShape)annotation.getROI();
                Area area = PathROIToolsAwt.getArea(pathShape);
                PolygonROI[][] annotationPolygons = PathROIToolsAwt.splitAreaToPolygons(area);

                for(int i = 0; i<annotationPolygons[1].length; i++) {
                    count++;
                    JsonObject jsonAnnotation = new JsonObject();
                    jsonAnnotation.addProperty("uid", count);
                    jsonAnnotation.addProperty("name", (annotation.getPathClass() == null) ? "Unclassified" : annotation.getPathClass().toString());

                    JsonArray pathCoords = new JsonArray();

                    for (Point2 point : annotationPolygons[1][i].getPolygonPoints()) {
                        JsonArray segment = new JsonArray();
                        JsonArray pathCoordPoint = new JsonArray();
                        pathCoordPoint.add(point.getX());
                        pathCoordPoint.add(point.getY());
                        segment.add(pathCoordPoint);
                        /**
                         * In order to mimic the data-structure of a PaperJS.segment, there needs to be two additional
                         * arrays
                         *
                         * Since this data is not used, they can contain zeroed coordinates
                         *
                         * http://paperjs.org/reference/segment/#segment
                         */
                        JsonArray zeroArray = new JsonArray();
                        zeroArray.add(0.0);
                        zeroArray.add(0.0);
                        segment.add(zeroArray);
                        segment.add(zeroArray);

                        pathCoords.add(segment);
                    }

                    JsonArray path = new JsonArray();
                    path.add("Path");
                    JsonObject pathProperties = new JsonObject();
                    pathProperties.addProperty("applyMatrix", true);
                    pathProperties.add("segments", pathCoords);
                    pathProperties.addProperty("closed", true);
                    JsonArray fillColour = new JsonArray();

                    JsonArray strokeColor = new JsonArray();

                    /**
                     * PathObject.color is null by default, thus the color of the annotation needs to be manually set if
                     * the user used the default color of RGB(255, 0, 0) (i.e. Red)
                     */
                    final int annotationRGB = annotation.getColorRGB() != null
                        ? annotation.getColorRGB()
                        : 16711680;
                    final double redValue = (double) (ColorTools.red(annotationRGB)) / 255.0;
                    final double greenValue = (double) (ColorTools.green(annotationRGB)) / 255.0;
                    final double blueValue = (double) (ColorTools.blue(annotationRGB)) / 255.0;
                    fillColour.add(redValue);
                    fillColour.add(greenValue);
                    fillColour.add(blueValue);
                    fillColour.add(0.5);
                    strokeColor.add(redValue);
                    strokeColor.add(greenValue);
                    strokeColor.add(blueValue);

                    pathProperties.add("strokeColor", strokeColor);

                    pathProperties.add("fillColor", fillColour);
                    pathProperties.addProperty("strokeScaling", false);

                    path.add(pathProperties);
                    jsonAnnotation.add("path", path);

                    jsonAnnotation.addProperty("zoom", 1.0);
                    JsonArray context = new JsonArray();
                    jsonAnnotation.add("context", context);
                    jsonAnnotation.addProperty("dictionary", "imported");

                    dictionariesArray.add(jsonAnnotation);
                }
                JsonObject sourceSlide = new JsonObject();
                sourceSlide.addProperty("SourceSlide", this.fileName + ".svs");
                arrayToExport.add(sourceSlide);
                JsonObject dictionaries = new JsonObject();
                JsonArray arrayOfAnnotations = new JsonArray();
                arrayOfAnnotations.add(dictionariesArray);
                dictionaries.add("dictionaries", arrayOfAnnotations);
                arrayToExport.add(dictionaries);
            }

            Gson gson = new GsonBuilder().create();
            Writer writer = new FileWriter(outputFile);
            gson.toJson(arrayToExport,writer);
            writer.close();
        } catch(java.io.IOException ex){
            lastMessage = "Error Reading JSON File";
            return false;
        }
        return true;
    }
}
