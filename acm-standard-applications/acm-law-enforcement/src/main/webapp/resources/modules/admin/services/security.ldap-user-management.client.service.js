'use strict';

angular.module('admin').factory('Admin.LdapUserManagementService', [ '$resource', '$http', function($resource, $http) {
    return ({
        queryGroupsByDirectory : queryGroupsByDirectory,
        queryAdhocGroups : queryAdhocGroups,
        getFilteredAuthorizedGroups : getFilteredAuthorizedGroups,
        getAllAuthorizedGroups : getAllAuthorizedGroups,
        getFilteredUnauthorizedGroups : getFilteredUnauthorizedGroups,
        getAllUnauthorizedGroups : getAllUnauthorizedGroups,
        addGroupsToUser : addGroupsToUser,
        removeGroupsFromUser : removeGroupsFromUser,
        cloneUser : cloneUser,
        deleteUser : deleteUser,
        getFilteredUsersByWord : getFilteredUsersByWord,
        getNUsers : getNUsers
    });

    function queryGroupsByDirectory(directory, n) {
        return $http({
            method : 'GET',
            url : 'api/latest/users/' + directory + '/groups',
            params : {
                n : (n ? n : 10000)
            }
        });
    }

    function queryAdhocGroups() {
        return $http({
            method : 'GET',
            url : 'api/latest/users/groups/adhoc',
            params : {
                n : 10000
            }
        });
    }

    function getFilteredAuthorizedGroups(data) {
        return $http({
            method : 'GET',
            url : 'api/latest/users/getFilteredAuthorizedGroups',
            params : {
                n : (data.n ? data.n : 20),
                q : data.member_id.key,
                fq : data.filterWord
            }
        });
    }

    function getAllAuthorizedGroups(data) {
        return $http({
            method : 'GET',
            url : 'api/latest/users/getAllAuthorizedGroups',
            params : {
                n : (data.n ? data.n : 20),
                q : data.member_id.key
            }
        });
    }

    function getFilteredUnauthorizedGroups(data) {
        return $http({
            method : 'GET',
            url : 'api/latest/users/getFilteredUnauthorizedGroups',
            params : {
                n : (data.n ? data.n : 20),
                q : data.member_id.key,
                fq : data.filterWord
            }
        });
    }

    function getAllUnauthorizedGroups(data) {
        return $http({
            method : 'GET',
            url : 'api/latest/users/getAllUnauthorizedGroups',
            params : {
                n : (data.n ? data.n : 20),
                q : data.member_id.key
            }
        });
    }

    function addGroupsToUser(user, groups, directory) {
        var url = 'api/latest/ldap/' + directory + '/manage/' + user + '/groups';
        return $http({
            method : 'PUT',
            url : url,
            data : groups
        });
    }

    function removeGroupsFromUser(user, groups, directory) {
        var groupNames = {};
        groupNames['groupNames'] = groups;
        var url = 'api/latest/ldap/' + directory + '/manage/' + user + '/groups';
        return $http({
            method : 'DELETE',
            url : url,
            params : groupNames
        });
    }

    function cloneUser(data) {
        var url = 'api/latest/ldap/' + data.selectedUser.directory + '/users/' + data.selectedUser.key;
        return $http({
            method : 'POST',
            url : url,
            data : data.user
        });
    }

    function deleteUser(user) {
        var url = 'api/latest/ldap/' + user.directory + '/users/' + user.key;
        return $http({
            method : 'DELETE',
            url : url
        });
    }

    /**
     * @ngdoc method
     * @name getFilteredUsersByWord
     * @methodOf services.service:LookupService
     *
     * @description
     * Filtered list 20 users from start position start POUBAV DESCRIPTION + IME NA METHOD!!!!!
     *
     * @returns {Object} An object returned by $resource
     */
    function getFilteredUsersByWord(data) {
        var url = 'api/latest/ldap/getFilteredUsers/search';
        return $http({
            method : 'GET',
            url : url,
            params : {
                fq : data.filterWord,
                n : (data.n ? data.n : 20),
                start : 0
            }
        });
    }

    /**
     * @ngdoc method
     * @name getNUsers
     * @methodOf services.service:LookupService
     *
     * @description
     * List of N users from start position 0 to end possition n POUBAV DESCRIPTION + IME NA METHOD!!!!!
     * Default n is 20
     *
     * @returns {Object} An object returned by $resource
     */
    function getNUsers(data) {
        var url = 'api/latest/ldap/getNUsers/search';
        return $http({
            method : 'GET',
            url : url,
            params : {
                n : (data.n ? data.n : 20),
                start : 0
            }
        });
    }
} ]);
