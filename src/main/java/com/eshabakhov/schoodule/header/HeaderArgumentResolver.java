/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.header;

import com.eshabakhov.schoodule.Header;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves a representation {@link Header} from the HTTP Accept header.
 *
 * @since 0.0.1
 */
@Component
public final class HeaderArgumentResolver
    implements HandlerMethodArgumentResolver {

    /** Vendor media subtype prefix. */
    private final String vendor;

    /**
     * New header argument resolver.
     */
    public HeaderArgumentResolver() {
        this.vendor = "vnd.schoodule.";
    }

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return Header.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
        final MethodParameter parameter,
        final ModelAndViewContainer container,
        final NativeWebRequest request,
        final WebDataBinderFactory factory
    ) throws HttpMediaTypeNotAcceptableException {
        final String accepted = request.getHeader(HttpHeaders.ACCEPT);
        final List<MediaType> types;
        if (accepted == null || accepted.isBlank()) {
            types = new ArrayList<>(List.of(MediaType.TEXT_HTML));
        } else {
            types = new ArrayList<>(MediaType.parseMediaTypes(accepted));
        }
        types.sort(
            Comparator.comparingDouble(MediaType::getQualityValue)
                .reversed().thenComparing(
                    (first, second) -> {
                        final int order;
                        if (first.isMoreSpecific(second)) {
                            order = -1;
                        } else if (second.isMoreSpecific(first)) {
                            order = 1;
                        } else {
                            order = 0;
                        }
                        return order;
                    }
                )
        );
        return types.stream()
            .flatMap(this::headers)
            .findFirst()
            .orElseThrow(() -> new HttpMediaTypeNotAcceptableException(types));
    }

    private Stream<Header<?>> headers(final MediaType type) {
        final Stream<Header<?>> headers;
        if (
            MediaType.TEXT_HTML.isCompatibleWith(type)
                || MediaType.ALL.equals(type)
        ) {
            headers = Stream.of(new HrHtml());
        } else if (this.vendor(type, "+json")) {
            headers = Stream.of(new HrJson(type.getSubtype()));
        } else if (this.vendor(type, "+xml")) {
            headers = Stream.of(new HrXml(type.getSubtype()));
        } else {
            headers = Stream.empty();
        }
        return headers;
    }

    private boolean vendor(final MediaType type, final String suffix) {
        return "application".equals(type.getType())
            && type.getSubtype().startsWith(this.vendor)
            && type.getSubtype().endsWith(suffix);
    }
}
