/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule;

import com.eshabakhov.schoodule.media.MdJson;
import com.eshabakhov.schoodule.media.MdThymeleaf;
import com.eshabakhov.schoodule.media.MdXml;
import com.eshabakhov.schoodule.media.MdXslt;
import com.eshabakhov.schoodule.media.XsClasspath;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
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
        final String printed = new MdXml("curriculum").include("id")
            .with("id", 1L)
            .with("description", "Hidden")
            .print();
        Assertions.assertTrue(printed.startsWith("<curriculum>"));
        Assertions.assertFalse(printed.contains("response"));
        Assertions.assertTrue(printed.contains("<id>1</id>"));
        Assertions.assertFalse(printed.contains("description"));
    }

    @Test
    void printsCollectionItemsUnderInterfaceName() throws Exception {
        Assertions.assertEquals(
            "<curriculums><curriculum><id>1</id></curriculum></curriculums>",
            new MdXml("curriculums").with(
                new PrItems(
                    "curriculum", List.of(new MediaTest.PrId())
                )
            ).print()
        );
    }

    @Test
    void printsThymeleafView() {
        final ModelAndView printed = new MdThymeleaf(
            "schools/details", "school"
        ).with("id", 1L).print();
        Assertions.assertEquals(
            "schools/details",
            printed.getViewName()
        );
        Assertions.assertEquals(
            1L,
            ((Map<?, ?>) printed.getModel().get("school")).get("id")
        );
    }

    @Test
    void transformsXmlIntoHtml() {
        final String printed = new MdXslt(
            new MdXml("schoodule"),
            new XsClasspath("xslt/federal-curriculums.xsl", "list")
        ).print();
        Assertions.assertTrue(
            printed.contains("id=\"federal-curriculums-results\"")
        );
        Assertions.assertTrue(printed.contains("data-sort=\"title\""));
        Assertions.assertTrue(printed.contains("/js/federal-curriculums.js"));
    }

    @Test
    void transformsCurriculumXmlIntoHtml() {
        final String printed = new XsClasspath(
            "xslt/federal-curriculum.xsl", "details"
        ).transform(
            """
            <schoodule><curriculums><curriculum><id>1</id>
            <title>Plan</title></curriculum></curriculums></schoodule>
            """
        );
        Assertions.assertTrue(printed.contains("class=\"school-info\""));
        Assertions.assertTrue(printed.contains("/js/federal-curriculum.js"));
    }

    @Test
    void transformsRequirementsXmlIntoHtml() {
        final String printed = new XsClasspath(
            "xslt/federal-curriculum-requirements.xsl", "list"
        ).transform(
            """
            <schoodule><curriculums><curriculum><id>1</id>
            <title>Plan</title><requirements/></curriculum>
            </curriculums></schoodule>
            """
        );
        Assertions.assertTrue(
            printed.contains("id=\"requirements-results\"")
        );
        Assertions.assertTrue(
            printed.contains("/js/federal-curriculum-requirements.js")
        );
    }

    /** Printable identifier. */
    private static final class PrId implements Printable {

        @Override
        public <T> Media<T> print(final Media<T> media) {
            return media.with("id", 1L);
        }
    }
}
