package com.google.ar.core.examples.java.xmlparser;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class BoardParser {
    private File board;
    private DocumentBuilder builder;
    private Document doc;

    private BoardDto boardInfo;

    private Map<String, BoardPartDto>  boardPartsInfo;

    public BoardParser(File boardFile) {
        this.board = boardFile;
    }

    public boolean parseBoard() throws ParserConfigurationException, IOException, SAXException {
        boardInfo = new BoardDto();
        boardPartsInfo = new HashMap<>();
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (Exception e) {
            System.out.println("Document Builder failed");
            return false;
        }
        // Set the custom EntityResolver
        builder.setEntityResolver(new CustomEntityResolver());
        try {
            doc = builder.parse(board);
        } catch (Exception e) {
            System.out.println("Parse failed with error " + e);
            return false;
        }
        doc.getDocumentElement().normalize();
        // board width and height retrieval
        NodeList dimensionList = doc.getElementsByTagName("wire");
        for (int i = 0; i < dimensionList.getLength(); i++) {
            Element wireElement = (Element) dimensionList.item(i);
            if (Integer.parseInt(wireElement.getAttribute("layer")) == 20) {
                if (Float.parseFloat(wireElement.getAttribute("x1")) > boardInfo.getWidth()) {
                    boardInfo.setWidth(Float.parseFloat(wireElement.getAttribute("x1")));
                }
                if (Float.parseFloat(wireElement.getAttribute("x2")) > boardInfo.getWidth()) {
                    boardInfo.setWidth(Float.parseFloat(wireElement.getAttribute("x1")));
                }
                if (Float.parseFloat(wireElement.getAttribute("y1")) > boardInfo.getHeight()) {
                    boardInfo.setHeight(Float.parseFloat(wireElement.getAttribute("y1")));
                }
                if (Float.parseFloat(wireElement.getAttribute("y2")) > boardInfo.getHeight()) {
                    boardInfo.setHeight(Float.parseFloat(wireElement.getAttribute("y1")));
                }
            }
        }

        // element retrieval
        NodeList elementList = doc.getElementsByTagName("element");
        for (int i = 0; i < elementList.getLength(); i++) {
            BoardPartDto currentBoardPart = new BoardPartDto();
            String currentPartName = "";
            Element element = (Element) elementList.item(i);
            currentPartName = element.getAttribute("name");
            currentBoardPart.x = Float.parseFloat(element.getAttribute("x"));
            currentBoardPart.z = Float.parseFloat(element.getAttribute("y"));
            currentBoardPart.device_package = element.getAttribute("package");
            NodeList partAttributesList = element.getElementsByTagName("attribute");
            for (int k = 0; k < partAttributesList.getLength(); k++) {
                Element attributeElement = (Element) partAttributesList.item(k);
                if (attributeElement.getAttribute("name").equals("MPN")) {
                    currentBoardPart.mpn = attributeElement.getAttribute("name");
                    if (!currentPartName.equals("")) {
                        boardPartsInfo.put(currentPartName, currentBoardPart);
                    }
                }
            }
        }
        return true;
    }

    public BoardDto getBoardInfo() {
        return boardInfo;
    }

    public Map<String, BoardPartDto> getBoardPartsInfo() {
        return boardPartsInfo;
    }

    private static class CustomEntityResolver implements EntityResolver {
        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            if (systemId.contains("eagle.dtd")) {
                // Return a dummy DTD input source to avoid FileNotFoundException
                return new InputSource(new StringReader(""));
            }
            // Return null to use the default resolution mechanism
            return null;
        }
    }
}
