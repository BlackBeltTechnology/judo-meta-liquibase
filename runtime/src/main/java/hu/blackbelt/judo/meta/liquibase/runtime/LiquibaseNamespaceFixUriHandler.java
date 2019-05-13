package hu.blackbelt.judo.meta.liquibase.runtime;

import com.google.common.io.ByteStreams;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.URIHandler;
import org.eclipse.emf.ecore.resource.impl.URIHandlerImpl;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.Map;

@RequiredArgsConstructor
public class LiquibaseNamespaceFixUriHandler extends URIHandlerImpl {

    @NonNull
    URIHandler parentUriHandler;

    @Override
    public boolean canHandle(URI uri) {
        return parentUriHandler.canHandle(uri);
    }

    @Override
    public OutputStream createOutputStream(URI uri, Map<?, ?> options) throws IOException {
        return new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                super.close();

                // Copy byte buffer
                OutputStream outputStream = parentUriHandler.createOutputStream(uri, options);

                try {
                    InputStream inputStream = transformXmlDocumentWithXslt(new ByteArrayInputStream(toByteArray()));
                    ByteStreams.copy(inputStream, outputStream);
                    outputStream.flush();
                    outputStream.close();
                } catch (TransformerException e) {
                    throw new IOException("Could not transform XML stream", e);
                }
           }
        };
    }

    @Override
    public InputStream createInputStream(URI uri, Map<?, ?> options) throws IOException {
        return parentUriHandler.createInputStream(uri, options);
    }

    @Override
    public void delete(URI uri, Map<?, ?> options) throws IOException {
        parentUriHandler.delete(uri, options);
    }

    @Override
    public boolean exists(URI uri, Map<?, ?> options) {
        return parentUriHandler.exists(uri, options);
    }

    @Override
    public Map<String, ?> getAttributes(URI uri, Map<?, ?> options) {
        return parentUriHandler.getAttributes(uri, options);
    }

    @Override
    public void setAttributes(URI uri, Map<String, ?> attributes, Map<?, ?> options) throws IOException {
        parentUriHandler.setAttributes(uri, attributes, options);
    }

    private static final String LIQUIBASE_FIX_NS_XSLT =
            "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n"+
            "    <xsl:output indent=\"yes\"/>\n"+
            "\n"+
            "    <xsl:template match=\"@*|text()|comment()|processing-instruction()\">\n"+
            "        <xsl:copy>\n"+
            "            <xsl:apply-templates select=\"@*|node()\"/>\n"+
            "        </xsl:copy>\n"+
            "    </xsl:template>\n"+
            "\n"+
            "    <xsl:template match=\"/*\">\n"+
            "        <databaseChangeLog " +
            "            xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\" \n"+
            "            xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n"+
            "            xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.1.xsd\">\n" +
            "            <xsl:apply-templates select=\"@*|node()\"/>\n"+
            "        </databaseChangeLog>\n"+
            "    </xsl:template>\n"+
            "\n"+
            "    <xsl:template match=\"*\">\n"+
            "        <xsl:element name=\"{local-name()}\">\n"+
            "            <xsl:apply-templates select=\"@*|node()\"/>\n"+
            "        </xsl:element>\n"+
            "    </xsl:template>\n"+
            "\n"+
            "</xsl:stylesheet>";

    private static InputStream transformXmlDocumentWithXslt(InputStream xmlDocument) throws TransformerException {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        transformer = tFactory.newTransformer(new StreamSource(new StringReader(LIQUIBASE_FIX_NS_XSLT)));
        StreamSource xmlSource = new StreamSource(xmlDocument);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(xmlSource, new StreamResult(baos));
        return new ByteArrayInputStream(baos.toByteArray());
    }


}
