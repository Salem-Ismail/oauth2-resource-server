package com.salem.resourceserver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.verifiedpermissions.VerifiedPermissionsClient;
import software.amazon.awssdk.services.verifiedpermissions.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// delegates ownership decisions to Amazon Verified Permissions.
// builds a PARC request, sends it to AVP, and returns the allow/deny verdict for the app to enforce
@Service
public class AuthorizationService {

    private final VerifiedPermissionsClient avpClient;
    private final String policyStoreId;

    // logs each authorization decision as a structured audit event
    private static final Logger log = LoggerFactory.getLogger(AuthorizationService.class);

    // policyStoreId is injected from application.properties; the AVP client resolves
    // credentials and region automatically from the environment (outside repo)
    public AuthorizationService(@Value("${avp.policy-store-id}") String policyStoreId) {
        this.policyStoreId = policyStoreId;
        this.avpClient = VerifiedPermissionsClient.create();
    }

    // asks AVP whether actingUser may read requestedUserId's status. Returns true if ALLOW
    public boolean isAllowed(String actingUser, String requestedUserId) {

        // principal: the user making the request (from the acting_user claim)
        EntityIdentifier principal = EntityIdentifier.builder()
                .entityType("ResourceServer::User")
                .entityId(actingUser)
                .build();

        // action: the operation being attempted
        ActionIdentifier action = ActionIdentifier.builder()
                .actionType("ResourceServer::Action")
                .actionId("readStatus")
                .build();

        // resource: the status being accessed
        EntityIdentifier resource = EntityIdentifier.builder()
                .entityType("ResourceServer::Status")
                .entityId(requestedUserId)
                .build();

        // resource entity data: a status for user X is owned by user X
        // This owner attribute is what the Cedar policy evaluates (resource.owner == principal)
        EntityItem resourceEntity = EntityItem.builder()
                .identifier(resource)
                .attributes(java.util.Map.of(
                        "owner", AttributeValue.builder()
                                .entityIdentifier(EntityIdentifier.builder()
                                        .entityType("ResourceServer::User")
                                        .entityId(requestedUserId)
                                        .build())
                                .build()
                ))
                .build();

        // assemble the full authorization request
        IsAuthorizedRequest request = IsAuthorizedRequest.builder()
                .policyStoreId(policyStoreId)
                .principal(principal)
                .action(action)
                .resource(resource)
                .entities(EntitiesDefinition.builder()
                        .entityList(resourceEntity)
                        .build())
                .build();

        // call AVP and return true only on an explicit ALLOW
        IsAuthorizedResponse response = avpClient.isAuthorized(request);
        boolean allowed = response.decision() == Decision.ALLOW;

        // audit log: every authorization decision for traceability
        log.info("Authorization decision: principal={} action=readStatus resource={} decision={}",
                actingUser, requestedUserId, allowed ? "ALLOW" : "DENY");

        return allowed;
    }
}