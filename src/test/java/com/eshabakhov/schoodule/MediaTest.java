/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule;

import com.eshabakhov.schoodule.media.MdJson;
import com.eshabakhov.schoodule.media.MdThymeleaf;
import com.eshabakhov.schoodule.media.MdXml;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.StringWriter;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

/**
 * Tests media printing.
 *
 * @since 0.0.1
 */
@SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")
final class MediaTest {

    @Test
    void keepsJsonMediaImmutable() {
        final Media<ObjectNode> initial = new MdJson();
        final Media<ObjectNode> changed = initial.with("id", 1L);
        Assertions.assertFalse(initial.print().has("id"));
        Assertions.assertTrue(changed.print().has("id"));
    }

    @Test
    void printsSelectedJsonFields() {
        final ObjectNode printed = new MdJson().include("id")
            .with("id", 1L)
            .with("description", "Hidden")
            .print();
        Assertions.assertTrue(printed.has("id"));
        Assertions.assertFalse(printed.has("description"));
    }

    @Test
    void printsSelectedXmlFields() throws Exception {
        final Source printed = new MdXml().include("id")
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
        final ModelAndView printed = new MdThymeleaf(
            "federal-curriculums/details", "curriculum"
        ).with("id", 1L).print();
        Assertions.assertEquals(
            "federal-curriculums/details",
            printed.getViewName()
        );
        Assertions.assertEquals(
            1L,
            ((Map<?, ?>) printed.getModel().get("curriculum")).get("id")
        );
    }
}
