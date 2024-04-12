package org.fulib.fx;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

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
        private final Map<String, TypeElement> imports = new HashMap<>();

        FxmlVisitor(PrintWriter out) {
            this.out = out;
        }

        @Override
        public void processingInstruction(String target, String data) {
            if (!"import".equals(target)) {
                return;
            }

            if (data.endsWith(".*")) {
                final PackageElement packageElement = processingEnv.getElementUtils().getPackageElement(data.substring(0, data.length() - 2));
                for (Element enclosedElement : packageElement.getEnclosedElements()) {
                    if (enclosedElement instanceof TypeElement typeElement) {
                        imports.put(typeElement.getSimpleName().toString(), typeElement);
                    }
                }
            } else {
                final TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(data);
                if (typeElement != null) {
                    imports.put(typeElement.getSimpleName().toString(), typeElement);
                } else {
                    // TODO warning
                }
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            final String type;
            final String varName = "_" + varCounter++;
            if ("fx:root".equals(qName)) {
                type = attributes.getValue("type");
                out.printf("    final %1$s %2$s = (%1$s) root;%n", type, varName);
            } else {
                type = qName;
                out.printf("    final %1$s %2$s = new %1$s();%n", type, varName);
            }

            final TypeElement typeElement = imports.get(type);

            for (int i = 0; i < attributes.getLength(); i++) {
                final String value = attributes.getValue(i);
                final String name = attributes.getLocalName(i);
                switch (name) {
                    case "fx:id" -> {
                        out.printf("    %1$s.setId(\"%2$s\");%n", varName, value);
                        out.printf("    controller.%1$s = %2$s;%n", value, varName);
                    }
                    case "fx:controller" -> {}
                    case "xmlns" -> {}
                    case "xmlns:fx" -> {}
                    default -> {
                        final ExecutableElement setter = findSetter(typeElement, name);
                        if (setter != null) {
                            final TypeMirror parameterType = setter.getParameters().get(0).asType();
                            final String literalValue = literalValue(value, parameterType);
                            out.printf("    %1$s.%2$s(%3$s);%n", varName, setter.getSimpleName(), literalValue);
                        } else {
                            // TODO warning
                        }
                    }
                }
            }
        }

        private String literalValue(String value, TypeMirror parameterType) {
            return processingEnv.getElementUtils().getConstantExpression(coercePrimitiveValue(value, parameterType));
        }

        private Object coercePrimitiveValue(String value, TypeMirror parameterType) {
            switch (parameterType.getKind()) {
                case BOOLEAN:
                    return value;
                case BYTE:
                    return Byte.parseByte(value);
                case SHORT:
                    return Short.parseShort(value);
                case CHAR:
                    return value.charAt(0);
                case INT:
                    return Integer.parseInt(value);
                case LONG:
                    return Long.parseLong(value);
                case FLOAT:
                    return Float.parseFloat(value);
                case DOUBLE:
                    return Double.parseDouble(value);
                case DECLARED:
                    if ("java.lang.String".equals(parameterType.toString())) {
                        return value;
                    }
            }
            return null;
        }

        private ExecutableElement findSetter(TypeElement typeElement, String name) {
            final String setterName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
            return Stream.iterate(typeElement, f -> f.getSuperclass().getKind() != TypeKind.NONE,f -> (TypeElement) processingEnv.getTypeUtils().asElement(f.getSuperclass()))
                .flatMap(element -> element.getEnclosedElements().stream())
                .filter(element -> element instanceof ExecutableElement)
                .map(element -> (ExecutableElement) element)
                .filter(element -> setterName.equals(element.getSimpleName().toString()) && element.getParameters().size() == 1)
                .findFirst()
                .orElse(null);
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
