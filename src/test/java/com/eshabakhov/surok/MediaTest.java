/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok;

import com.eshabakhov.surok.federal.curriculum.input.CrInput;
import com.eshabakhov.surok.federal.curriculum.input.CrInputYear;
import com.eshabakhov.surok.media.MdJson;
import com.eshabakhov.surok.media.MdXml;
import com.eshabakhov.surok.media.MdXslt;
import com.eshabakhov.surok.media.XsClasspath;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
            "<curriculums><curriculum><year>2026/2027</year></curriculum></curriculums>",
            new MdXml("curriculums").with(
                new PrItems(
                    "curriculum", List.of(new CrInputYear(new CrInput(), "2026/2027"))
                )
            ).print()
        );
    }

    @Test
    void transformsXmlIntoHtml() {
        final String printed = new MdXslt(
            new MdXml("surok"),
            new XsClasspath("xslt/federal-curriculums.xsl", "list")
        ).print();
        Assertions.assertTrue(
            printed.contains("id=\"federal-curriculums-results\"")
        );
        Assertions.assertTrue(printed.contains("data-sort=\"title\""));
        Assertions.assertTrue(printed.contains("/js/federal/curriculums.js"));
    }

    @Test
    void transformsCurriculumXmlIntoHtml() {
        final String printed = new XsClasspath(
            "xslt/federal-curriculum.xsl", "details"
        ).transform(
            """
            <surok><curriculums><curriculum><id>1</id>
            <title>Plan</title></curriculum></curriculums></surok>
            """
        );
        Assertions.assertTrue(printed.contains("class=\"school-info\""));
        Assertions.assertTrue(printed.contains("/js/federal/curriculum.js"));
    }

    @Test
    void transformsRequirementsXmlIntoHtml() {
        final String printed = new XsClasspath(
            "xslt/federal-curriculum-requirements.xsl", "list"
        ).transform(
            """
            <surok><curriculums><curriculum><id>1</id>
            <title>Plan</title><requirements/></curriculum>
            </curriculums></surok>
            """
        );
        Assertions.assertTrue(
            printed.contains("id=\"requirements-results\"")
        );
        Assertions.assertTrue(
            printed.contains("/js/federal/curriculum-requirements.js")
        );
    }
}
