/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule;

import com.eshabakhov.schoodule.header.HeaderArgumentResolver;
import com.eshabakhov.schoodule.header.HrHtml;
import com.eshabakhov.schoodule.header.HrJson;
import com.eshabakhov.schoodule.header.HrXml;
import com.eshabakhov.schoodule.media.MdJson;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.StringWriter;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.ModelAndView;

/**
 * Tests media selection and printing.
 *
 * @since 0.0.1
 */
@SuppressWarnings({
    "PMD.UnitTestContainsTooManyAsserts",
    "UnusedMethod"
})
final class MediaTest {

    @Test
    void resolvesHtmlHeader() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.ACCEPT, "text/html");
        Assertions.assertEquals(
            HrHtml.class,
            new HeaderArgumentResolver().resolveArgument(
                MediaTest.parameter(),
                null,
                new ServletWebRequest(request),
                null
            ).getClass()
        );
    }

    @Test
    void resolvesVendorJsonHeader() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(
            HttpHeaders.ACCEPT,
            "application/vnd.schoodule.federal-curriculum.simple.v1+json"
        );
        Assertions.assertEquals(
            HrJson.class,
            new HeaderArgumentResolver().resolveArgument(
                MediaTest.parameter(),
                null,
                new ServletWebRequest(request),
                null
            ).getClass()
        );
    }

    @Test
    void printsSelectedJsonFields() {
        final ObjectNode printed = new HrJson(
            "vnd.schoodule.federal-curriculum.simple.v1+json"
        ).media("", "")
            .with("id", 1L)
            .with("description", "Hidden")
            .print();
        Assertions.assertTrue(printed.has("id"));
        Assertions.assertFalse(printed.has("description"));
    }

    @Test
    void keepsMdJsonImmutable() {
        final Media<ObjectNode> initial = new MdJson();
        final Media<ObjectNode> changed = initial.with("id", 1L);
        Assertions.assertFalse(initial.print().has("id"));
        Assertions.assertTrue(changed.print().has("id"));
    }

    @Test
    void printsSelectedXmlFields() throws Exception {
        final Source printed = new HrXml(
            "vnd.schoodule.federal-curriculum.simple.v1+xml"
        ).media("", "")
            .with("id", 1L)
            .with("description", "Hidden")
            .print();
        final StringWriter output = new StringWriter();
        TransformerFactory.newInstance().newTransformer().transform(
            printed,
            new StreamResult(output)
        );
        Assertions.assertTrue(output.toString().contains("<id>1</id>"));
        Assertions.assertFalse(output.toString().contains("description"));
    }

    @Test
    void printsThymeleafView() {
        final ModelAndView printed = new HrHtml()
            .media("federal-curriculums/details", "curriculum")
            .with("id", 1L)
            .print();
        Assertions.assertEquals(
            "federal-curriculums/details",
            printed.getViewName()
        );
        Assertions.assertEquals(
            1L,
            ((Map<?, ?>) printed.getModel().get("curriculum"))
                .get("id")
        );
    }

    private static MethodParameter parameter() throws NoSuchMethodException {
        return new MethodParameter(
            Endpoint.class.getDeclaredMethod("print", Header.class),
            0
        );
    }

    /**
     * Test endpoint fixture.
     *
     * @since 0.0.1
     */
    @FunctionalInterface
    private interface Endpoint {

        /**
         * Accepts a representation header.
         *
         * @param header Representation header
         */
        void print(Header<?> header);
    }
}
