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

    File annotationFile;
    String lastMessage = "";

    public ExportAnnotationServiceJSONPlugin( File annotationFile) {
        this.annotationFile = annotationFile;
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
    protected void preprocess(final PluginRunner<BufferedImage> pluginRunner){
    }


    @Override
    protected void postprocess(final PluginRunner<BufferedImage> pluginRunner) {
    }



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


        //The JSON File imported by the annotation service has the following structure:
        // <JSON Array>                             (Array of annotations)
        //  <JSON Object>                           (Annotation)
        //      uid: <int>
        //      name: <String>
        //      <JSON Array> imgCoords              (Actual coordinates of annotation on image)
        //          <JSON Object>                   (Point)
        //              x: <Double>
        //              y: <Double>
        //              ...
        //      <JSON Array> path                   (Describes information for painting the annotation in Paper.js)
        //          0:Path
        //          1:<JSON Object>                 (Path Information)
        //              applyMatrix:<Boolean>
        //              <JSON Array> segments       (These are the points used to paint the annotation)
        //                  <JSON Object>           (Point)
        //                      x: <Double>
        //                      y: <Double>
        //              closed:<Boolean>            (Whether the path is closed or not)
        //              <JSON Array> fillColor
        //                  0:<Double>              (red color)
        //                  1:<Double>              (green color)
        //                  2:<Double>              (blue color)
        //                  3:<Double>              (alpha)
        //              <JSON Array> strokeColor
        //                  0:<Double>              (red color)
        //                  1:<Double>              (green color)
        //                  2:<Double>              (blue color)
        //              strokeScaling:<Boolean>
        //      zoom:<Double>                       (What zoom level the annotation was drawn at. Not important for us)
        //      <JSON Array> context                (This array determines which annotations are within which other annotations. Not important for now but may be needed as more sophisticated paths need to be exported)
        //      dictionary:<String>                 (Which dictionary these annotations belong to. For now we always use "imported"
        //  <JSON Object>                           (Annotation)
        //  ...
        //  ...


        try {

            JsonArray annotationLayout = new JsonArray();



            int count = 0;
            for(PathObject annotation : objects)
            {
                PathShape pathShape = (PathShape)annotation.getROI();
                Area area = PathROIToolsAwt.getArea(pathShape);
                PolygonROI[][] annotationPolygons = PathROIToolsAwt.splitAreaToPolygons(area);

                for(int i = 0; i<annotationPolygons[1].length; i++) {

                    count++;
                    JsonObject jsonAnnotation = new JsonObject();
                    jsonAnnotation.addProperty("uid", count);
                    jsonAnnotation.addProperty("name", (annotation.getPathClass() == null) ? "Unclassified" : annotation.getPathClass().toString());

                    JsonArray imgCoords = new JsonArray();
                    JsonArray pathCoords = new JsonArray();


                    for (Point2 point : annotationPolygons[1][i].getPolygonPoints()) {
                        JsonObject annotationPoint = new JsonObject();
                        annotationPoint.addProperty("x", point.getX());
                        annotationPoint.addProperty("y", point.getY());
                        imgCoords.add(annotationPoint);

                        //It appears that to convert between the image coordinates and the coordinates used to draw the annotation, we divide the image coordinates
                        //in both dimensions by the total *width* of the image and then multiply by a factor of 1000.

                        JsonObject pathCoordPoint = new JsonObject();
                        pathCoordPoint.addProperty("0", point.getX() / imageData.getServer().getWidth() * 1000);
                        pathCoordPoint.addProperty("1", point.getY() / imageData.getServer().getWidth() * 1000);
                        pathCoords.add(pathCoordPoint);
                    }

                    jsonAnnotation.add("imgCoords", imgCoords);

                    JsonArray path = new JsonArray();
                    path.add("Path");
                    JsonObject pathProperties = new JsonObject();
                    pathProperties.addProperty("applyMatrix", true);
                    pathProperties.add("segments", pathCoords);
                    pathProperties.addProperty("closed", true);
                    JsonArray fillColour = new JsonArray();

                    if (annotation.getPathClass() == null) {
                        fillColour.add(1);
                        fillColour.add(0);
                        fillColour.add(0);
                        fillColour.add(0.5);
                    } else {
                        int annotationRGB = annotation.getPathClass().getColor();
                        fillColour.add((double) (ColorTools.red(annotationRGB)) / 255.0);
                        fillColour.add((double) (ColorTools.green(annotationRGB)) / 255.0);
                        fillColour.add((double) (ColorTools.blue(annotationRGB)) / 255.0);
                        fillColour.add(0.5);
                    }

                    pathProperties.add("fillColor", fillColour);
                    JsonArray strokeColor = new JsonArray();
                    strokeColor.add(0);
                    strokeColor.add(0);
                    strokeColor.add(0);
                    pathProperties.add("strokeColor", strokeColor);
                    pathProperties.addProperty("strokeScaling", false);

                    path.add(pathProperties);
                    jsonAnnotation.add("path", path);

                    jsonAnnotation.addProperty("zoom", 1.0);
                    JsonArray context = new JsonArray();
                    jsonAnnotation.add("context", context);
                    jsonAnnotation.addProperty("dictionary", "imported");

                    annotationLayout.add(jsonAnnotation);
                }

            }


            Gson gson = new GsonBuilder().create();
            Writer writer = new FileWriter(outputFile);
            gson.toJson(annotationLayout,writer);
            writer.close();


        } catch(java.io.IOException ex){
            lastMessage = "Error Reading JSON File";
            return false;
        }

        return true;

    }


}
