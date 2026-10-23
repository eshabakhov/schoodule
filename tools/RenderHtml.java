/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
import java.io.StringWriter;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import javax.tools.ToolProvider;
import javax.xml.XMLConstants;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/** Renders complete XSLT pages for HTMLHint using only the project's JDK. */
final class RenderHtml {
    public static void main(final String[] args) throws Exception {
        final Path output = Path.of("target/html-lint");
        Files.createDirectories(output);
        try (var previous = Files.list(output)) {
            for (final Path file : previous.filter(path -> path.toString().endsWith(".html")).toList()) {
                Files.delete(file);
            }
        }
        final Path classes = Path.of("target/html-renderer");
        Files.createDirectories(classes);
        final int compiled = ToolProvider.getSystemJavaCompiler().run(
            null, null, null, "-d", classes.toString(),
            "src/main/java/com/eshabakhov/surok/media/HtmlOutput.java"
        );
        if (compiled != 0) {
            throw new IllegalStateException("Cannot compile the shared HTML output formatter");
        }
        final var factory = TransformerFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "file");
        final var pattern = Pattern.compile("\\$view\\s*=\\s*'([^']+)'");
        try (
            var loader = new URLClassLoader(new java.net.URL[]{classes.toUri().toURL()});
            var stylesheets = Files.list(Path.of("src/main/resources/xslt"))
        ) {
            @SuppressWarnings("unchecked")
            final var formatter = (UnaryOperator<String>) loader.loadClass(
                "com.eshabakhov.surok.media.HtmlOutput"
            ).getConstructor().newInstance();
            for (final Path path : stylesheets.filter(file -> file.toString().endsWith(".xsl")).sorted().toList()) {
                final String source = Files.readString(path);
                if (!Pattern.compile("<xsl:template\\s+match=\"/\"").matcher(source).find()) {
                    continue;
                }
                final var views = new LinkedHashSet<String>();
                views.add(path.getFileName().toString().equals("authentication.xsl") ? "login" : "list");
                final var matches = pattern.matcher(source);
                while (matches.find()) {
                    if (!matches.group(1).endsWith("-fragment")) {
                        views.add(matches.group(1));
                    }
                }
                final var template = factory.newTemplates(new StreamSource(path.toFile()));
                for (final String view : views) {
                    final var transformer = template.newTransformer();
                    transformer.setParameter("view", view);
                    final var html = new StringWriter();
                    transformer.transform(
                        new StreamSource(Path.of("tools/html-fixture.xml").toFile()),
                        new StreamResult(html)
                    );
                    Files.writeString(
                        output.resolve(path.getFileName() + "-" + view + ".html"),
                        formatter.apply(html.toString())
                    );
                }
            }
        }
    }
}
