package com.gwidgets.providers;


import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.EntityManager;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.jpa.entities.UserAttributeEntity;
import org.keycloak.models.jpa.entities.UserEntity;

public class RegisterEventListenerProvider implements EventListenerProvider  {

    private KeycloakSession session;
    private RealmProvider model;
    //keycloak utility to generate random strings, anything can be used e.g UUID,..
    private SecretGenerator secretGenerator;
    private EntityManager entityManager;

    public RegisterEventListenerProvider(KeycloakSession session) {
        this.session = session;
        this.model = session.realms();
        this.entityManager = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        this.secretGenerator = SecretGenerator.getInstance();
    }

    public void onEvent(Event event) {
        System.out.println(event.getType());
        //we are only interested in the register event
        if (event.getType().equals(EventType.REGISTER)) {
            String userId = event.getUserId();
            addApiKeyAttribute(userId);
        }
        // logout event
        // if (event.getType().equals(EventType.LOGOUT)) {
        //     String userId = event.getUserId();
        //     updateApiKeyAttribute(userId);
        // }
    }

    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        // in case the user is created from admin or rest api
        if (Objects.equals(adminEvent.getResourceType(), ResourceType.USER) && Objects.equals(adminEvent.getOperationType(), OperationType.CREATE)) {
            String userId = adminEvent.getResourcePath().split("/")[1];
            if (Objects.nonNull(userId)) {
                addApiKeyAttribute(userId);
            }
        }
    }



    public void addApiKeyAttribute(String userId) {
        String apiKey = secretGenerator.randomString(50);
        UserEntity userEntity = entityManager.find(UserEntity.class, userId);
        UserAttributeEntity attributeEntity = new UserAttributeEntity();
        attributeEntity.setName("api-key");
        attributeEntity.setValue(apiKey);
        attributeEntity.setUser(userEntity);
        attributeEntity.setId(UUID.randomUUID().toString());
        entityManager.persist(attributeEntity);
    }

    public void updateApiKeyAttribute(String userId) {
        String apiKey = secretGenerator.randomString(50);
        UserEntity userEntity = entityManager.find(UserEntity.class, userId);
        if(findAttributeByName(userEntity.getAttributes(), "api-key") == null) {
            System.out.println("can't find attr!!!");
            return;
        }
        UserAttributeEntity attributeEntity = findAttributeByName(userEntity.getAttributes(), "api-key");
        attributeEntity.setValue(apiKey);
        entityManager.persist(attributeEntity);
    }

    protected UserAttributeEntity findAttributeByName(Collection<UserAttributeEntity> attrList, String searchAttrName) {
        for (UserAttributeEntity  attr: attrList) {
            if(attr.getName().equals(searchAttrName)) {
                return attr;
            }
        }

        return null;
    }

    @Override
    public void close() {

    }
}
