/**
 * Created by cschlosser on 21/03/2018.
 */
package qupath.AnnotationExchangeExtension;

import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.objects.PathAnnotationObject;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.hierarchy.PathObjectHierarchy;
import qupath.lib.roi.PathObjectToolsAwt;
import qupath.lib.roi.PathROIToolsAwt;
import qupath.lib.roi.PolygonROI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class ImportXMLAnnotation implements PathCommand {


    private QuPathGUI qupath;
    final private static Logger logger = LoggerFactory.getLogger(ImportXMLAnnotation.class);
    private final String commandName = "Import XML Annotation";

    /**
     * @return Returns the commandName assigned to this class
     */
    public String commandName() {
        return commandName;
    }

    public ImportXMLAnnotation(QuPathGUI qupath){

        this.qupath = qupath;

    }

    public void run(){

        QuPathViewer viewer = qupath.getViewer();
        if (viewer == null || viewer.getServer() == null)
        {
            logger.error("No Slide Loaded.");
            return;
        }

        //We use javafx FileChooser directly since the qupath DialogHelper seems to not be effective with file extensions
        FileChooser fileChooser = new FileChooser();
        // Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML Annotation", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);
        File inputFile = fileChooser.showOpenDialog(null );

        if(inputFile == null){
            logger.error("No XML Annotations Selected");
            return;
        }

        boolean succesfulRead  = readXMLAnnotations(inputFile);

    }


    private boolean readXMLAnnotations(File inputFile ) {

        PathObjectHierarchy hierarchy = qupath.getViewer().getHierarchy();

        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // Image Data Root Element
            Document doc;
            try{
                doc = docBuilder.parse(inputFile);
            }
            catch(org.xml.sax.SAXException ex)
            {
                logger.error("Error Parsing XML File");
                return false;
            }
            catch(java.io.IOException ex)
            {
                logger.error("Error Parsing XML File");
                return false;
            }



            //doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("Annotation");


            //Loop over all annotations
            for(int temp = 0; temp<nList.getLength(); temp++) {

                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element annotationElement = (Element) nNode;
                    String annotationColor = annotationElement.getAttribute("Color");
                    String annotationName = annotationElement.getAttribute("Name");
                    String annotationGroup = annotationElement.getAttribute("PartOfGroup");

                    NodeList coordinateList = annotationElement.getElementsByTagName("Coordinate");

                    float[] xPoints = new float[coordinateList.getLength()];
                    float[] yPoints = new float[coordinateList.getLength()];

                    //Loop over all coordinates in annotation
                    for (int coordIndex = 0; coordIndex < coordinateList.getLength(); coordIndex++) {

                        Node coordinateNode = coordinateList.item(coordIndex);

                        if (coordinateNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element coordElement = (Element) coordinateNode;
                            String coordY = coordElement.getAttribute("Y");
                            String coordX = coordElement.getAttribute("X");

                            xPoints[coordIndex] = Float.parseFloat(coordX);
                            yPoints[coordIndex] = Float.parseFloat(coordY);

                        }
                    }

                    PolygonROI annotaionPoly = new PolygonROI(xPoints, yPoints, -1, 0, 0);
                    PathAnnotationObject importedAnnotation = new PathAnnotationObject(annotaionPoly);
                    importedAnnotation.setColorRGB(Integer.parseInt(annotationColor.substring(1), 16));
                    importedAnnotation.setName("Group " + annotationGroup.substring(1));
                    hierarchy.addPathObject(importedAnnotation, true);


                }

            }


            PathROIToolsAwt.CombineOp opSub = PathROIToolsAwt.CombineOp.SUBTRACT;
            PathROIToolsAwt.CombineOp opAdd = PathROIToolsAwt.CombineOp.ADD;


            for(PathObject annotation : new ArrayList<>(hierarchy.getObjects(null,PathAnnotationObject.class))){
                    if (annotation.getDisplayedName().contains("Group 2")) {
                        List<PathObject> subList = new ArrayList();
                        subList.add(annotation);
                        subList.add(annotation.getParent());
                        PathObjectToolsAwt.combineAnnotations(hierarchy, subList, opSub);
                    }

            }

            List<PathObject> addList = new ArrayList();
            for(PathObject annotation : new ArrayList<>(hierarchy.getObjects(null,PathAnnotationObject.class))){
                if(annotation.getDisplayedName().contains("Group 0")){
                        addList.add(annotation);
                    }
            }
            PathObjectToolsAwt.combineAnnotations(  qupath.getViewer().getHierarchy(), addList, opAdd);



        } catch (ParserConfigurationException pce) {
            logger.error("Error Reading XML File");
            return false;
        }

        return true;

    }



}
