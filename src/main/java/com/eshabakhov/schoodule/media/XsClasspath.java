/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.media;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Classpath XSLT transformation.
 *
 * @since 0.0.1
 */
public final class XsClasspath implements Xslt {

    /** Stylesheet resource. */
    private final String resource;

    /** View selected inside the stylesheet. */
    private final String view;

    /**
     * New classpath transformation.
     *
     * @param resource Stylesheet resource
     * @param view View name
     */
    public XsClasspath(final String resource, final String view) {
        this.resource = resource;
        this.view = view;
    }

    @Override
    public String transform(final String xml) {
        final TransformerFactory factory = TransformerFactory.newInstance();
        final Resource located = new ClassPathResource(this.resource);
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            factory.setURIResolver(new XsClasspathResolver(located));
            final StringWriter output = new StringWriter();
            try (
                InputStream input = located.getInputStream();
                StringReader source = new StringReader(xml)
            ) {
                final StreamSource stylesheet = new StreamSource(input);
                stylesheet.setSystemId(this.resource);
                final Transformer transformer = factory.newTransformer(
                    stylesheet
                );
                transformer.setParameter("view", this.view);
                transformer.transform(
                    new StreamSource(source),
                    new StreamResult(output)
                );
            }
            return output.toString();
        } catch (final IOException | TransformerException err) {
            throw new IllegalStateException(
                String.format("Failed to transform %s", this.resource), err
            );
        }
    }
}
