package com.msn.msncars.auth;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
    Used to manually extract authorities from JWT (JwtAuthenticationConverter is responsible for converting JWT into Authentication)
    https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html#oauth2resourceserver-jwt-authorization-extraction
 */
@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    private final String principalClaimName = "sub";

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> authorities = Stream.concat(
                jwtGrantedAuthoritiesConverter.convert(jwt).stream(), // default authorities that spring would add
                extractResourceRoles(jwt).stream() // our custom authorities
        ).collect(Collectors.toSet());

        return new JwtAuthenticationToken(jwt, authorities, jwt.getClaimAsString(this.principalClaimName));
    }


    /*
        Logic that defines how to extract roles from jwt
     */
    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        if (jwt.getClaim("resource_access") == null) {
            return Set.of();
        }
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");

        if (resourceAccess.get("MSNcars") == null) {
            return Set.of();
        }
        Map<String, Object> resource = (Map<String, Object>) resourceAccess.get("MSNcars");

        if (resource.get("roles") == null) {
            return Set.of();
        }
        Collection<String> resourceRoles = (Collection<String>) resource.get("roles");

        return resourceRoles
                .stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
    }

}
