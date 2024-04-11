package org.fulib.fx;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
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
        try (final Reader reader = resource.openReader(true)) {
            final XMLEventReader xmlReader = XMLInputFactory.newInstance().createXMLEventReader(reader);

            boolean hasRoot = false;
            while (xmlReader.hasNext()) {
                final XMLEvent event = xmlReader.nextEvent();
                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    System.out.println(startElement);
                    if (!hasRoot) {
                        hasRoot = true;
                        if ("fx".equals(startElement.getName().getPrefix()) && "root".equals(startElement.getName().getLocalPart())) {
                            final String rootType = startElement.getAttributeByName(QName.valueOf("type")).getValue();
                            out.printf("    final %1$s _0 = (%1$s) root;%n", rootType);
                        } else {
                            final String rootType = startElement.getName().getLocalPart();
                            out.printf("    final %1$s _0 = new %1$s();%n", rootType);
                        }
                    }
                }
            }

            if (hasRoot) {
                out.println("    return _0;");
            } else {
                out.println("    return null;");
            }
        } catch (XMLStreamException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
