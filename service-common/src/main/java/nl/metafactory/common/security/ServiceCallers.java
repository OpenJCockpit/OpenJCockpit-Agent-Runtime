package nl.metafactory.common.security;

import org.springframework.security.oauth2.jwt.Jwt;

public final class ServiceCallers {

    private ServiceCallers() {
    }

    public static boolean is(Jwt jwt, String expectedClientId) {
        if (jwt == null || expectedClientId == null || expectedClientId.isBlank()) {
            return false;
        }
        return expectedClientId.equals(jwt.getClaimAsString("azp"));
    }

    public static String clientId(Jwt jwt) {
        return jwt == null ? null : jwt.getClaimAsString("azp");
    }
}
