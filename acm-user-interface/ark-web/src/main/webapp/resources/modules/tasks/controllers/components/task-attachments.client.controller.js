'use strict';

angular.module('tasks').controller('Tasks.AttachmentsController', ['$scope', '$stateParams', '$modal', 'UtilService', 'HelperService', 'CallLookupService',
    function ($scope, $stateParams, $modal, Util, Helper, CallLookupService) {
        $scope.$emit('req-component-config', 'attachments');
        $scope.$on('component-config', function (e, componentId, config) {
            if ('attachments' == componentId) {
                $scope.config = config;
            }
        });


        CallLookupService.getFileTypes().then(
            function (fileTypes) {
                $scope.fileTypes = $scope.fileTypes || [];
                $scope.fileTypes = $scope.fileTypes.concat(Util.goodArray(fileTypes));
                return fileTypes;
            }
        );


        $scope.objectType = Helper.ObjectTypes.TASK;
        $scope.objectId = $stateParams.id;
        $scope.$on('task-retrieved', function (e, data) {
            $scope.taskInfo = data;
        });

    }
]);