'use strict';

angular.module('search').controller('Search.FacetsController', ['$scope',
    function ($scope) {
        $scope.$emit('req-component-config', 'facets');

        $scope.config = null;
        $scope.gridOptions = {};
        $scope.$on('component-config', applyConfig);
        function applyConfig(e, componentId, config) {
            if (componentId == 'facets') {
                $scope.config = config;
            }
        }
    }
]);