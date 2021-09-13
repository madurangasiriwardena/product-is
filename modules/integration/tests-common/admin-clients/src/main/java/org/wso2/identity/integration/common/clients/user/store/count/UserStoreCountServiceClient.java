/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.identity.integration.common.clients.user.store.count;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.axis2.AxisFault;
import org.wso2.carbon.identity.user.store.count.stub.UserStoreCountServiceStub;
import org.wso2.carbon.identity.user.store.count.stub.dto.PairDTO;
import org.wso2.carbon.integration.common.admin.client.utils.AuthenticateStubUtil;

public class UserStoreCountServiceClient {
    private UserStoreCountServiceStub stub;
    private String service = "UserStoreCountService";

    /**
     * Constructor UserStoreCountServiceClient
     *
     * @param sessionCookie    - session cookie
     * @param backendServerURL - backend server URL
     */
    public UserStoreCountServiceClient(String backendServerURL, String sessionCookie) throws AxisFault {
        String serviceURL = backendServerURL + service;
        stub = new UserStoreCountServiceStub(serviceURL);
        AuthenticateStubUtil.authenticateStub(sessionCookie, stub);

    }

    /**
     * Constructor UserStoreCountServiceClient
     *
     * @param userName         - user name
     * @param backendServerURL - backend server URL
     */
    public UserStoreCountServiceClient(String backendServerURL, String userName, String password) throws AxisFault {
        String serviceURL = backendServerURL + service;
        stub = new UserStoreCountServiceStub(serviceURL);
        AuthenticateStubUtil.authenticateStub(userName, password, stub);

    }

    public Map<String, String> countUsers(String filter) throws Exception {

        try {
            return convertArrayToMap(stub.countUsers(filter));
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

    public Map<String, String> countRoles(String filter) throws Exception {

        try {
            return convertArrayToMap(stub.countRoles(filter));
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

    public Map<String, String> countByClaim(String claimURI, String value) throws Exception {

        try {
            return convertArrayToMap(stub.countClaim(claimURI, value));
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

    public Map<String, String> countByClaims(Map<String, String> claims) throws Exception {

        try {
            return convertArrayToMap(stub.countClaims(convertMapToArray(claims)));
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

    public long countUsersInDomain(String filter, String domain) throws Exception {

        try {
            return stub.countUsersInDomain(filter, domain);
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

    public long countRolesInDomain(String filter, String domain) throws Exception {

        try {
            return stub.countRolesInDomain(filter, domain);
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

    public long countByClaimInDomain(String claimURI, String filter, String domain) throws Exception {

        try {
            return stub.countByClaimInDomain(claimURI, filter, domain);
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

    public long countByClaimsInDomain(PairDTO[] pairDTOs, String domain) throws Exception {

        try {
            return stub.countByClaimsInDomain(pairDTOs, domain);
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

    public Set<String> getCountableUserStores() throws Exception {

        try {
            return new HashSet<>(Arrays.asList(stub.getCountEnabledUserStores()));
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Converts a given array of PairDTOs to a Map
     *
     * @param pairDTOs
     * @return
     */
    private Map<String, String> convertArrayToMap(PairDTO[] pairDTOs) {
        Map<String, String> map = new HashMap<>();
        for (PairDTO pairDTO : pairDTOs) {
            map.put(pairDTO.getKey(), pairDTO.getValue());
        }
        return map;
    }

    /**
     * Converts a given Map to an array of PairDTOs
     *
     * @param claims
     * @return
     */
    private PairDTO[] convertMapToArray(Map<String, String> claims) {
        PairDTO[] pairs = new PairDTO[claims.size()];
        Iterator iterator = claims.entrySet().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            PairDTO pair = new PairDTO();
            pair.setKey((String) entry.getKey());
            pair.setValue((String) entry.getValue());
            pairs[i] = pair;
            i++;
        }

        return pairs;
    }

}

