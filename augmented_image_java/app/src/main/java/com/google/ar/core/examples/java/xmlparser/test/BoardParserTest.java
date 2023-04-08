package com.google.ar.core.examples.java.xmlparser.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.ar.core.examples.java.xmlparser.BoardDto;
import com.google.ar.core.examples.java.xmlparser.BoardParser;

import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

public class BoardParserTest {
    @Test
    public void parserTest() throws ParserConfigurationException, IOException, SAXException {
        String workingDir = System.getProperty("user.dir");
        System.out.println("Current working directory: " + workingDir);

        File boardFile = new File("src/main/java/com/google/ar/core/examples/java/xmlparser/test/sab1.xml");
        assertTrue(boardFile.exists());
        BoardParser parser = new BoardParser(boardFile);
        Map<String, BoardDto> boardInfo = parser.parseBoard();
        assertEquals(boardInfo.size(), 35);
        System.out.println(boardInfo);
    }
}