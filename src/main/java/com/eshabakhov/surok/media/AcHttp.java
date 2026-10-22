/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.media;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;

/**
 * Response representation selected by HTTP Accept from supported media types.
 *
 * @since 0.0.1
 */
public final class AcHttp implements Accepted {

    /** HTTP request. */
    private final HttpServletRequest request;

    /** Supported response media types in preference order. */
    private final List<MediaType> supported;

    /**
     * New accepted representation.
     *
     * @param request HTTP request
     * @param resources Resource names with versions, with HTML preferred by default
     */
    public AcHttp(final HttpServletRequest request, final String... resources) {
        this(
            request,
            Stream.concat(
                Stream.of(MediaType.TEXT_HTML),
                Arrays.stream(resources).flatMap(
                    resource -> Stream.of(
                        MediaType.parseMediaType(
                            String.format("application/vnd.surok.%s+json", resource)
                        ),
                        MediaType.parseMediaType(
                            String.format("application/vnd.surok.%s+xml", resource)
                        )
                    )
                )
            ).toList()
        );
    }

    /**
     * New accepted representation with an explicit set of supported formats.
     *
     * @param request HTTP request
     * @param supported Supported media types in preference order
     */
    public AcHttp(final HttpServletRequest request, final List<MediaType> supported) {
        this.request = request;
        this.supported = new ArrayList<>(supported);
    }

    @Override
    public MediaType type() {
        final List<MediaType> accepted;
        try {
            accepted = new ArrayList<>(
                MediaType.parseMediaTypes(this.request.getHeader(HttpHeaders.ACCEPT))
            );
        } catch (final InvalidMediaTypeException exception) {
            throw new ResponseStatusException(
                HttpStatus.NOT_ACCEPTABLE, "Invalid Accept header", exception
            );
        }
        if (accepted.isEmpty()) {
            accepted.add(MediaType.ALL);
        }
        return this.supported.stream()
            .filter(candidate -> AcHttp.quality(candidate, accepted) > 0).max(
                Comparator.comparingDouble(candidate -> AcHttp.quality(candidate, accepted))
            ).orElseThrow(
                () -> new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Unsupported Accept header"
                )
            );
    }

    private static double quality(final MediaType candidate, final List<MediaType> accepted) {
        return accepted.stream().filter(wanted -> wanted.isCompatibleWith(candidate)).max(
            Comparator.comparingInt(
                (MediaType wanted) -> {
                    final int specificity;
                    if (wanted.isWildcardType()) {
                        specificity = 0;
                    } else if (wanted.isWildcardSubtype()) {
                        specificity = 1;
                    } else {
                        specificity = 2;
                    }
                    return specificity;
                }
            ).thenComparingDouble(MediaType::getQualityValue)
        ).map(MediaType::getQualityValue).orElse(0.0);
    }
}
