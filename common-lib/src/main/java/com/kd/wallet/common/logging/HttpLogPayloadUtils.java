package com.kd.wallet.common.logging;

import java.util.regex.Pattern;

public final class HttpLogPayloadUtils {

    private static final Pattern SENSITIVE_JSON_FIELD = Pattern.compile(
            "(?i)(\"(?:password|passwordHash|accessToken|refreshToken|token|authorization)\"\\s*:\\s*\")([^\"]*)(\")"
    );
    private static final Pattern SENSITIVE_FORM_FIELD = Pattern.compile(
            "(?i)((?:^|[?&\\s])(?:password|passwordHash|accessToken|refreshToken|token|authorization)=)([^&\\s]+)"
    );

    private HttpLogPayloadUtils() {
    }

    public static String sanitizeBody(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        String sanitized = SENSITIVE_JSON_FIELD.matcher(body).replaceAll("$1***$3");
        return SENSITIVE_FORM_FIELD.matcher(sanitized).replaceAll("$1***");
    }
}
