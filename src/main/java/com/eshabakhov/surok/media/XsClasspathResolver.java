/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.media;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import org.springframework.core.io.Resource;

/**
 * Resolves imported XSL stylesheets relative to a classpath resource.
 *
 * @since 0.0.1
 */
public final class XsClasspathResolver implements URIResolver {

    /** Root stylesheet resource. */
    private final Resource root;

    /**
     * New resolver.
     *
     * @param root Root stylesheet resource
     */
    public XsClasspathResolver(final Resource root) {
        this.root = root;
    }

    @Override
    public Source resolve(final String href, final String base)
        throws TransformerException {
        try (
            InputStream input = this.root.createRelative(href).getInputStream()
        ) {
            final StreamSource source = new StreamSource(
                new StringReader(
                    new String(
                        input.readAllBytes(), StandardCharsets.UTF_8
                    )
                )
            );
            source.setSystemId(href);
            return source;
        } catch (final IOException err) {
            throw new TransformerException(
                String.format("Failed to resolve stylesheet %s", href), err
            );
        }
    }
}
