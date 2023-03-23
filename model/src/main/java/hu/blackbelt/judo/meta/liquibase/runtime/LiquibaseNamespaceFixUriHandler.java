package hu.blackbelt.judo.meta.liquibase.runtime;

/*-
 * #%L
 * Judo :: Liquibase :: Model
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import com.google.common.io.ByteStreams;
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

public class LiquibaseNamespaceFixUriHandler extends URIHandlerImpl {

    URIHandler parentUriHandler;

    public LiquibaseNamespaceFixUriHandler(URIHandler parentUriHandler) {
        this.parentUriHandler = parentUriHandler;
    }

    @Override
    public boolean canHandle(URI uri) {
        return parentUriHandler.canHandle(uri);
    }

    @Override
    public OutputStream createOutputStream(URI uri, Map<?, ?> options) throws IOException {
        return fixUriOutputStream(parentUriHandler.createOutputStream(uri, options));
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

    private static void transformXmlDocumentWithXslt(InputStream xmlDocument, OutputStream outputStream) throws TransformerException {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        transformer = tFactory.newTransformer(new StreamSource(new StringReader(LIQUIBASE_FIX_NS_XSLT)));
        StreamSource xmlSource = new StreamSource(xmlDocument);
        transformer.transform(xmlSource, new StreamResult(outputStream));
    }


    public static OutputStream fixUriOutputStream(final OutputStream outputStream) {
        return new ByteArrayOutputStream() {
            @Override
            public void flush() throws IOException {
                try {
                    if (toByteArray().length > 0) {
                        transformXmlDocumentWithXslt(new ByteArrayInputStream(toByteArray()), outputStream);
                        outputStream.flush();
                        reset();
                    }
                } catch (TransformerException e) {
                    throw new IOException("Could not transform XML stream", e);
                }
                super.flush();
            }
        };

    }
}
