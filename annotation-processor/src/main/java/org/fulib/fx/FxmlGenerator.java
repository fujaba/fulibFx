package org.fulib.fx;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;

public class FxmlGenerator {
    private final ProcessingEnvironment processingEnv;

    public FxmlGenerator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public void generateFxmlImports(PrintWriter out, FileObject resource) {
        try (final Reader reader = resource.openReader(true)) {
            final XMLEventReader xmlReader = XMLInputFactory.newInstance().createXMLEventReader(reader);
            while (xmlReader.hasNext()) {
                final XMLEvent event = xmlReader.nextEvent();
                if (event.isProcessingInstruction()
                    && event instanceof ProcessingInstruction processingInstruction
                    && "import".equals(processingInstruction.getTarget())
                ) {
                    // TODO FXMLs can also have wildcard imports. That could lead to name clashes.
                    out.printf("import %s;%n", processingInstruction.getData());
                }
            }
        } catch (XMLStreamException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void generateRenderFxml(PrintWriter out, FileObject resource) {
        try (final InputStream inputStream = resource.openInputStream()) {
            final SAXParserFactory factory = SAXParserFactory.newInstance();

            // https://rules.sonarsource.com/java/RSPEC-2755
            // prevent XXE, completely disable DOCTYPE declaration:
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            final SAXParser saxParser = factory.newSAXParser();

            final DefaultHandler handler = new FxmlVisitor(out);

            saxParser.parse(inputStream, handler);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    class FxmlVisitor extends DefaultHandler {
        private final PrintWriter out;
        private int varCounter;

        FxmlVisitor(PrintWriter out) {
            this.out = out;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            System.out.println("startElement: " + qName + " " + attributes);

            if ("fx:root".equals(qName)) {
                final String rootType = attributes.getValue("type");
                out.printf("    final %1$s _%2$d = (%1$s) root;%n", rootType, varCounter++);
            } else {
                final String rootType = qName;
                out.printf("    final %1$s _%2$d = new %1$s();%n", rootType, varCounter++);
            }
        }

        @Override
        public void endDocument() {
            if (varCounter > 0) {
                out.println("    return _0;");
            } else {
                out.println("    return null;");
            }
        }
    }
}
