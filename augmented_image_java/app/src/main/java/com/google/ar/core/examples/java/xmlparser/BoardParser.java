package com.google.ar.core.examples.java.xmlparser;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
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

    public BoardParser(File boardFile) {
        this.board = boardFile;
    }

    public Map<String, BoardDto> parseBoard() throws ParserConfigurationException, IOException, SAXException {
        Map<String, BoardDto> boardInfo = new HashMap<>();

        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (Exception e) {
            System.out.println("Document Builder failed");
            return null;
        }
        // Set the custom EntityResolver
        builder.setEntityResolver(new CustomEntityResolver());
        try {
            doc = builder.parse(board);
        } catch (Exception e) {
            System.out.println("Parse failed with error " + e);
            return null;
        }
        doc.getDocumentElement().normalize();

        // element retrieval
        NodeList elementList = doc.getElementsByTagName("element");
        for (int i = 0; i < elementList.getLength(); i++) {
            BoardDto currentBoardPart = new BoardDto();
            String currentPartName = "";
            Node elementNode = elementList.item(i);
            Element element = (Element) elementNode;
            currentPartName = element.getAttribute("name");
            currentBoardPart.x = Float.parseFloat(element.getAttribute("x"));
            currentBoardPart.y = Float.parseFloat(element.getAttribute("y"));
            currentBoardPart.device_package = element.getAttribute("package");
            NodeList partAttributesList = element.getElementsByTagName("attribute");
            for (int k = 0; k < partAttributesList.getLength(); k++) {
                Element attributeElement = (Element) partAttributesList.item(k);
                if (attributeElement.getAttribute("name").equals("MPN")) {
                    currentBoardPart.mpn = attributeElement.getAttribute("name");
                    if (!currentPartName.equals("")) {
                        boardInfo.put(currentPartName, currentBoardPart);
                    }
                }
            }
        }
        return boardInfo;
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
