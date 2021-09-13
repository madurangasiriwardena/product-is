/*
 *Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */
package org.wso2.identity.integration.common.clients.usermgt.remote;

import java.rmi.RemoteException;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.wso2.carbon.um.ws.api.stub.ArrayOfString;
import org.wso2.carbon.um.ws.api.stub.ClaimDTO;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.um.ws.api.stub.PermissionDTO;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.identity.integration.common.clients.AuthenticateStub;

public class RemoteUserStoreManagerServiceClient {

    private final String serviceName = "RemoteUserStoreManagerService";
    private RemoteUserStoreManagerServiceStub remoteUserStoreManagerServiceStub;

    public RemoteUserStoreManagerServiceClient(String backEndUrl, String sessionCookie)
            throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        remoteUserStoreManagerServiceStub = new RemoteUserStoreManagerServiceStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, remoteUserStoreManagerServiceStub);
    }

    public RemoteUserStoreManagerServiceClient(String backEndUrl, String userName, String password)
            throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        remoteUserStoreManagerServiceStub = new RemoteUserStoreManagerServiceStub(endPoint);
        AuthenticateStub.authenticateStub(userName, password, remoteUserStoreManagerServiceStub);
    }

    /**
     * Adds a role to the system.
     *
     * @param roleName    The role name
     * @param userList    the list of the users.
     * @param permissions The permissions of the role.
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public void addRole(String roleName, String[] userList, PermissionDTO[] permissions)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            remoteUserStoreManagerServiceStub.addRole(roleName, userList, permissions);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Add a user to the user store.
     *
     * @param userName              User name of the user
     * @param credential            The credential/password of the user
     * @param roleList              The roles that user belongs
     * @param claimValues           Properties of the user
     * @param profileName           The name of the profile where claims should be added
     * @param requirePasswordChange Require the password change
     * @throws org.wso2.carbon.user.api.UserStoreException
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     */
    public void addUser(String userName, String credential, String[] roleList, ClaimValue[] claimValues,
                        String profileName, boolean requirePasswordChange)
            throws UserStoreException, RemoteException,
                   RemoteUserStoreManagerServiceUserStoreExceptionException {

        try {
            remoteUserStoreManagerServiceStub.addUser(userName, credential, roleList, claimValues,
                    profileName, requirePasswordChange);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Set a single user claim value
     *
     * @param userName    The user name
     * @param claimURI    The claim URI
     * @param claimValue  The value
     * @param profileName The profile name, can be null. If null the default profile is considered
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public void setUserClaimValue(String userName, String claimURI, String claimValue, String profileName)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            remoteUserStoreManagerServiceStub.setUserClaimValue(userName, claimURI, claimValue, profileName);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Set many user claim values
     *
     * @param userName    The user name
     * @param claims      Array of claims against values
     * @param profileName The profile name, can be null. If null the default profile is considered
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public void setUserClaimValues(String userName, ClaimValue[] claims, String profileName)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            remoteUserStoreManagerServiceStub.setUserClaimValues(userName, claims, profileName);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * validate whether the user is authenticated
     *
     * @param userName   The user name
     * @param credential The credential of a user
     * @return If the value is true the provided credential match with the user name. False is
     * returned for invalid credential, invalid user name and mismatching credential with
     * user name.
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException An unexpected exception has occured
     * @throws java.rmi.RemoteException                                          An unexpected exception has occured
     */
    public boolean authenticate(String userName, String credential)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            return remoteUserStoreManagerServiceStub.authenticate(userName, credential);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Update the credential/password of the user
     *
     * @param userName      The user name
     * @param newCredential The new credential/password
     * @param oldCredential The old credential/password
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public void updateCredential(String userName, String newCredential, String oldCredential)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            remoteUserStoreManagerServiceStub.updateCredential(userName, newCredential, oldCredential);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Update credential/password by the admin of another user
     *
     * @param userName      The user name
     * @param newCredential The new credential/password
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public void updateCredentialByAdmin(String userName, String newCredential)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            remoteUserStoreManagerServiceStub.updateCredentialByAdmin(userName, newCredential);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Updates roles of a user
     *
     * @param userName    The user name of the user where role list is updated
     * @param deleteRoles The array of role names to be removed
     * @param newRoles    The array of role names to be added
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public void updateRoleListOfUser(String userName, String[] deleteRoles, String[] newRoles)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            remoteUserStoreManagerServiceStub.updateRoleListOfUser(userName, deleteRoles, newRoles);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Updates users in a Role
     *
     * @param roleName    The role name to be updated
     * @param deleteUsers The array of user names to be deleted
     * @param newUsers    The array of of user names to be added
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public void updateUserListOfRole(String roleName, String[] deleteUsers, String[] newUsers)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            remoteUserStoreManagerServiceStub.updateUserListOfRole(roleName, deleteUsers, newUsers);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Update the role name of given role
     *
     * @param oldRoleName old role name
     * @param newRoleName new role name
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public void updateRoleName(String oldRoleName, String newRoleName)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            remoteUserStoreManagerServiceStub.updateRoleName(oldRoleName, newRoleName);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Checks whether the role name is in the user store
     *
     * @param roleName role name
     * @return true if exists, false otherwise
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public boolean isExistingRole(String roleName)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            return remoteUserStoreManagerServiceStub.isExistingRole(roleName);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Checks whether the user is in the user store
     *
     * @param userName
     * @return
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public boolean isExistingUser(String userName)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            return remoteUserStoreManagerServiceStub.isExistingUser(userName);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * @return
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public boolean isReadOnly() throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            return remoteUserStoreManagerServiceStub.isReadOnly();
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Retrieves a list of user names upto a maximum limit
     *
     * @param filter   The string to filter out user
     * @param maxLimit The max item limit. If -1 then system maximum limit will be used. If the
     *                 given value is greater than the system configured max limit it will be resetted to
     *                 the system configured max limit.
     * @return An arry of user names
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public String[] listUsers(String filter, int maxLimit)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            return remoteUserStoreManagerServiceStub.listUsers(filter, maxLimit);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }


    public String[] getProfileNames(String userName)
            throws RemoteException, RemoteUserStoreManagerServiceUserStoreExceptionException {

        try {
            return remoteUserStoreManagerServiceStub.getProfileNames(userName);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Retrieve all available profile names
     *
     * @return array of profile names
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public String[] getAllProfileNames()
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            return remoteUserStoreManagerServiceStub.getAllProfileNames();
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Get roles of a user
     *
     * @param userName
     * @return
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public String[] getRoleListOfUser(String userName)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            return remoteUserStoreManagerServiceStub.getRoleListOfUser(userName);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Get all role names
     *
     * @return
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public String[] getRoleNames() throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            return remoteUserStoreManagerServiceStub.getRoleNames();
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Retrieve hybrid roles
     *
     * @return
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public String[] getHybridRoles() throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            return remoteUserStoreManagerServiceStub.getHybridRoles();
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Get user claim value in the profile.
     *
     * @param userName    user name
     * @param claim       The claim URI
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @return claim value
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public String getUserClaimValue(String userName, String claim, String profileName)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            return remoteUserStoreManagerServiceStub.getUserClaimValue(userName, claim, profileName);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Get all claim values of the user in the profile.
     *
     * @param userName    user name
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @return An array of claims
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public ClaimDTO[] getUserClaimValues(String userName, String profileName)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            return remoteUserStoreManagerServiceStub.getUserClaimValues(userName, profileName);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }


    /**
     * Get user claim values in the profile.
     *
     * @param userName    The user name
     * @param claims      The claim URIs
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @return Array of claim values
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     */
    public ClaimValue[] getUserClaimValuesForClaims(String userName, String[] claims,
                                                    String profileName)
            throws RemoteException, RemoteUserStoreManagerServiceUserStoreExceptionException {

        try {
            return remoteUserStoreManagerServiceStub.getUserClaimValuesForClaims(userName, claims, profileName);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }


    /**
     * Returns the user id if available
     *
     * @param userName user name
     * @return id
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public int getUserId(String userName)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            return remoteUserStoreManagerServiceStub.getUserId(userName);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Retrieves a list of user names for given user claim value
     *
     * @param claimUri    claim uri
     * @param claimValue  claim value
     * @param profileName profile name, can be null. If null the default profile is considered.
     * @return An array of user names
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public String[] getUserList(String claimUri, String claimValue, String profileName)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            return remoteUserStoreManagerServiceStub.getUserList(claimUri, claimValue, profileName);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * get list of users belong to given role
     *
     * @param roleName role name
     * @return array of users
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public String[] getUserListOfRole(String roleName)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            return remoteUserStoreManagerServiceStub.getUserListOfRole(roleName);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }


    /**
     * Function to get password expiration time of a given user
     *
     * @param userName user name
     * @return The password expiration time
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public long getPasswordExpirationTime(String userName)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            return remoteUserStoreManagerServiceStub.getPasswordExpirationTime(userName);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * NOTE : getProperties is not implemented in service implementation
     *
     * @param tenant
     * @return
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public ArrayOfString[] getProperties(OMElement tenant)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            return remoteUserStoreManagerServiceStub.getProperties(tenant);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Retrieve the tenant id associated with the user store manager
     *
     * @return
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public int getTenantId() throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            return remoteUserStoreManagerServiceStub.getTenantId();
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Get tenant id of the given user
     * NOTE : This method works only if the tenant is super tenant
     *
     * @param userName
     * @return
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException If the realm is not super tenant's this method should throw exception
     * @throws java.rmi.RemoteException
     */
    public int getTenantIdofUser(String userName)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            return remoteUserStoreManagerServiceStub.getTenantIdofUser(userName);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }


    /**
     * Delete the user with the given user name
     *
     * @param username user name
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public void deleteUser(String username)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            remoteUserStoreManagerServiceStub.deleteUser(username);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Delete the role with the given role name
     *
     * @param roleName The role name
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public void deleteRole(String roleName)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            remoteUserStoreManagerServiceStub.deleteRole(roleName);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Delete a single user claim value
     *
     * @param userName    The user name
     * @param claimUri    Name of the claim
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public void deleteUserClaimValue(String userName, String claimUri, String profileName)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            remoteUserStoreManagerServiceStub.deleteUserClaimValue(userName, claimUri, profileName);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Delete a single user claim values
     *
     * @param userName    The user name
     * @param claims      array of claim URIs
     * @param profileName The profile name, can be null. If null the default profile is considered.
     * @throws org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws java.rmi.RemoteException
     */
    public void deleteUserClaimValues(String userName, String[] claims, String profileName)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        try {
            remoteUserStoreManagerServiceStub.deleteUserClaimValues(userName, claims, profileName);
        } finally {
            remoteUserStoreManagerServiceStub._getServiceClient().cleanupTransport();
        }
    }
}
