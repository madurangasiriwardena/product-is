package org.wso2.identity.integration.common.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Stub;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.integration.common.admin.client.utils.AuthenticateStubUtil;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;
import org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.user.mgt.stub.types.carbon.UIPermissionNode;
import org.wso2.carbon.user.mgt.stub.types.carbon.UserRealmInfo;

import javax.activation.DataHandler;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class UserManagementClient {
    private static final int LIMIT = 100;
    private final Log log = LogFactory.getLog(UserManagementClient.class);
    private final String serviceName = "UserAdmin";
    private UserAdminStub userAdminStub;

    public UserManagementClient(String backendURL, String sessionCookie) throws AxisFault {
        String endPoint = backendURL + serviceName;
        userAdminStub = new UserAdminStub(endPoint);
        AuthenticateStubUtil.authenticateStub(sessionCookie, userAdminStub);
    }

    public UserManagementClient(String backendURL, String userName, String password)
            throws AxisFault {
        String endPoint = backendURL + serviceName;
        userAdminStub = new UserAdminStub(endPoint);
        AuthenticateStubUtil.authenticateStub(userName, password, userAdminStub);
    }

    public Stub getServiceStub() {
        return this.userAdminStub;
    }

    public static ClaimValue[] toADBClaimValues(
            ClaimValue[] claimValues) {
        if (claimValues == null) {
            return new ClaimValue[0];
        }
        ClaimValue[] values = new ClaimValue[claimValues.length];
        for (ClaimValue cvalue : claimValues) {
            ClaimValue value = new ClaimValue();
            value.setClaimURI(cvalue.getClaimURI());
            value.setValue(cvalue.getValue());
        }
        return values;
    }

    public void addRole(String roleName, String[] userList, String[] permissions) throws
            RemoteException,
            UserAdminUserAdminException {

        try {
            userAdminStub.addRole(roleName, userList, permissions, false);
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public void addRole(String roleName, String[] userList, String[] permissions,
                        boolean isSharedRole) throws RemoteException, UserAdminUserAdminException {

        try {
            userAdminStub.addRole(roleName, userList, permissions, isSharedRole);
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public void addUser(String userName, String password, String[] roles,
                        String profileName) throws RemoteException, UserAdminUserAdminException {

        try {
            userAdminStub.addUser(userName, password, roles, null, profileName);
        }  finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public void deleteRole(String roleName) throws RemoteException, UserAdminUserAdminException {

        try {
            userAdminStub.deleteRole(roleName);
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public void deleteUser(String userName) throws RemoteException, UserAdminUserAdminException {

        try {
            userAdminStub.deleteUser(userName);
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public void updateUserListOfRole(String roleName, String[] addingUsers,
                                     String[] deletingUsers)
            throws UserAdminUserAdminException, RemoteException {
        List<FlaggedName> updatedUserList = new ArrayList<FlaggedName>();
        if (addingUsers != null) {
            for (String addUser : addingUsers) {
                FlaggedName fName = new FlaggedName();
                fName.setItemName(addUser);
                fName.setSelected(true);
                updatedUserList.add(fName);
            }
        }
        //add deleted users to the list
        if (deletingUsers != null) {
            for (String deletedUser : deletingUsers) {
                FlaggedName fName = new FlaggedName();
                fName.setItemName(deletedUser);
                fName.setSelected(false);
                updatedUserList.add(fName);
            }
        }
        //call userAdminStub to update user list of role

        try {
            userAdminStub.updateUsersOfRole(roleName, updatedUserList.toArray(
                    new FlaggedName[updatedUserList.size()]));
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
        //if delete users in retrieved list, fail
        if (deletingUsers != null) {
            for (String deletedUser : deletingUsers) {
                FlaggedName[] verifyingList;
                try {
                    verifyingList = userAdminStub.getUsersOfRole(roleName, deletedUser, LIMIT);
                    assert (!verifyingList[0].getSelected());
                } finally {
                    userAdminStub._getServiceClient().cleanupTransport();
                }
            }
        }
        if (addingUsers != null) {
            //if all added users are not in list fail
            for (String addingUser : addingUsers) {
                try {
                    FlaggedName[] verifyingList = userAdminStub.getUsersOfRole(roleName, addingUser, LIMIT);
                    assert (verifyingList[0].getSelected());
                } finally {
                    userAdminStub._getServiceClient().cleanupTransport();
                }
            }
        }

    }

    public boolean roleNameExists(String roleName)
            throws RemoteException, UserAdminUserAdminException {
        FlaggedName[] roles;
        try {
            roles = userAdminStub.getAllRolesNames(roleName, LIMIT);
            for (FlaggedName role : roles) {
                if (role.getItemName().equals(roleName)) {
                    log.info("Role name " + roleName + " already exists");
                    return true;
                }
            }
            return false;
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Lists all roles caught by wither with in limit
     */
    public FlaggedName[] listRoles(String filter, int limit)
            throws RemoteException, UserAdminUserAdminException {

        try {
            return userAdminStub.getAllRolesNames(filter, limit);
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Lists all users with in filter and limit
     */
    public String[] listUsers(String filter, int limit)
            throws RemoteException, UserAdminUserAdminException {

        try {
            return userAdminStub.listUsers(filter, limit);
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public FlaggedName[] listAllUsers(String filter, int limit) throws RemoteException,
            UserAdminUserAdminException {

        try {
            return userAdminStub.listAllUsers(filter, limit);
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public FlaggedName[] listUsersByClaim(ClaimValue value, String filter, int limit) throws RemoteException,
            UserAdminUserAdminException {

        try {
            return userAdminStub.listUserByClaim(value, filter, limit);
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }
    public boolean userNameExists(String roleName, String userName)
            throws RemoteException, UserAdminUserAdminException {

        FlaggedName[] users;
        try {
            users = userAdminStub.getUsersOfRole(roleName, "*", LIMIT);

            for (FlaggedName user : users) {
                if (user.getItemName().equals(userName)) {
                    log.info("User name " + userName + " already exists");
                    return true;
                }
            }
            return false;
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public Boolean hasMultipleUserStores() throws RemoteException, UserAdminUserAdminException {

        try {
            return userAdminStub.hasMultipleUserStores();
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public void addInternalRole(String roleName, String[] userList, String[] permissions)
            throws RemoteException, UserAdminUserAdminException {

        try {
            userAdminStub.addInternalRole(roleName, userList, permissions);
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public FlaggedName[] getAllRolesNames(String filter, int limit)
            throws RemoteException, UserAdminUserAdminException {

        try {
            return userAdminStub.getAllRolesNames(filter, limit);
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public void addRemoveUsersOfRole(String roleName, String[] newUsers, String[] deletedUsers)
            throws RemoteException, UserAdminUserAdminException {

        try {
            userAdminStub.addRemoveUsersOfRole(roleName, newUsers, deletedUsers);
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public void addRemoveRolesOfUser(String userName, String[] newRoles, String[] deletedRoles)
            throws RemoteException, UserAdminUserAdminException {

        try {
            userAdminStub.addRemoveRolesOfUser(userName, newRoles, deletedRoles);
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public FlaggedName[] getUsersOfRole(String roleName, String filter, int limit)
            throws RemoteException, UserAdminUserAdminException {

        try {
            return userAdminStub.getUsersOfRole(roleName, filter, limit);
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public FlaggedName[] getRolesOfUser(String userName, String filter, int limit)
            throws RemoteException, UserAdminUserAdminException {

        try {
            return userAdminStub.getRolesOfUser(userName, filter, limit);
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public void updateUsersOfRole(String roleName, FlaggedName[] userList)
            throws RemoteException, UserAdminUserAdminException {

        try {
            userAdminStub.updateUsersOfRole(roleName, userList);
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public void updateRolesOfUser(String userName, String[] newUserList)
            throws RemoteException, UserAdminUserAdminException {

        try {
            userAdminStub.updateRolesOfUser(userName, newUserList);
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public void changePassword(String userName, String newPassword)
            throws RemoteException, UserAdminUserAdminException {

        try {
            userAdminStub.changePassword(userName, newPassword);
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public void updateRoleName(String roleName, String newRoleName)
            throws RemoteException, UserAdminUserAdminException {

        try {
            userAdminStub.updateRoleName(roleName, newRoleName);
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public void changePasswordByUser(String userName, String oldPassword, String newPassword)
            throws RemoteException, UserAdminUserAdminException {

        try {
            userAdminStub.changePasswordByUser(userName, oldPassword, newPassword);
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public FlaggedName[] getAllSharedRoleNames(String filter, int limit)
            throws RemoteException, UserAdminUserAdminException {

        try {
            return userAdminStub.getAllSharedRoleNames(filter, limit);
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public UIPermissionNode getAllUIPermissions()
            throws RemoteException, UserAdminUserAdminException {

        try {
            return userAdminStub.getAllUIPermissions();
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public void setRoleUIPermission(String roleName, String[] rawResources)
            throws RemoteException, UserAdminUserAdminException {

        try {
            userAdminStub.setRoleUIPermission(roleName, rawResources);
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public UIPermissionNode getRolePermissions(String roleName)
            throws RemoteException, UserAdminUserAdminException {

        try {
            return userAdminStub.getRolePermissions(roleName);
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public FlaggedName[] getRolesOfCurrentUser()
            throws RemoteException, UserAdminUserAdminException {

        try {
            return userAdminStub.getRolesOfCurrentUser();
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public FlaggedName[] listUserByClaim(ClaimValue claimValue, String filter, int maxLimit)
            throws RemoteException, UserAdminUserAdminException {

        try {
            return userAdminStub.listUserByClaim(claimValue, filter, maxLimit);
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public UserRealmInfo getUserRealmInfo()
            throws RemoteException, UserAdminUserAdminException {

        try {
            return userAdminStub.getUserRealmInfo();
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public boolean isSharedRolesEnabled()
            throws RemoteException, UserAdminUserAdminException {

        try {
            return userAdminStub.isSharedRolesEnabled();
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public void bulkImportUsers(String userStoreDomain, String filename, DataHandler handler, String defaultPassword)
            throws RemoteException, UserAdminUserAdminException {

        try {
            userAdminStub.bulkImportUsers(userStoreDomain, filename, handler, defaultPassword);
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public HashSet<String> getUserList() throws RemoteException, UserAdminUserAdminException {

        try {
            return new HashSet<>(Arrays.asList(userAdminStub.listUsers("*", LIMIT)));
        } finally {
            userAdminStub._getServiceClient().cleanupTransport();
        }
    }
}