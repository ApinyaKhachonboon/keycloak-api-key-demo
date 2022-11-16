package com.gwidgets.resources;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Stream;

public class ApiKeyResource {

    private KeycloakSession session;

    private final String realmName;

    public ApiKeyResource(KeycloakSession session) {
        this.session = session;
        String envRealmName = System.getenv("REALM_NAME");
        this.realmName = Objects.isNull(envRealmName) || Objects.equals(System.getenv(envRealmName), "")? "myrealm": envRealmName;
    }

    @GET
    @Produces("application/json")
    public Response checkApiKey(@QueryParam("apiKey") String apiKey) {
        Stream<UserModel> result = session.users().searchForUserByUserAttributeStream(session.realms().getRealmByName(realmName), "api-key", apiKey);
        return result.count() > 0 ? Response.ok().type(MediaType.APPLICATION_JSON).build(): Response.status(401).type(MediaType.APPLICATION_JSON).build();
        // System.out.println(session.users().getUsersStream(session.realms().getRealm(realmName),1, null));
        // System.out.println(session.realms().getRealmByName(realmName));
        // HashMap<String, String> map = new HashMap<>();
        // map.put("realmName", realmName);
        // map.put("apiKey", apiKey);
        // return map;
    }
}
