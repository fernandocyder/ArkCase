'use strict';

angular.module('complaints').controller('Complaints.ParticipantsController', ['$scope', '$stateParams', '$q', '$translate', '$modal'
    , 'StoreService', 'UtilService', 'ConfigService', 'Complaint.InfoService', 'LookupService'
    , 'Object.LookupService', 'Helper.UiGridService', 'Helper.ObjectBrowserService'
    , function ($scope, $stateParams, $q, $translate, $modal
        , Store, Util, ConfigService, ComplaintInfoService, LookupService
        , ObjectLookupService, HelperUiGridService, HelperObjectBrowserService) {

        var gridHelper = new HelperUiGridService.Grid({scope: $scope});

        var promiseConfig = ConfigService.getComponentConfig("complaints", "participants").then(function (config) {
            gridHelper.addDeleteButton(config.columnDefs, "grid.appScope.deleteRow(row.entity)");
            gridHelper.setColumnDefs(config);
            gridHelper.setBasicOptions(config);
            gridHelper.disableGridScrolling(config);
            gridHelper.addGridApiHandler(function (gridApi) {
                $scope.gridApi.edit.on.afterCellEdit($scope, function (rowEntity, colDef, newValue, oldValue) {
                    if (newValue == oldValue) {
                        return;
                    }

                    //
                    // Fix participant names selection
                    //
                    if (HelperUiGridService.Lookups.PARTICIPANT_TYPES === colDef.lookup) {
                        if ("*" === newValue) {
                            rowEntity.acm$_participantNames = [
                                {id: "*", name: "*"}
                            ];
                        } else if ("owning group" === newValue) {
                            rowEntity.acm$_participantNames = $scope.participantGroups;
                        } else {
                            rowEntity.acm$_participantNames = $scope.participantUsers;
                        }

                        $scope.$apply();
                    }

                    //
                    // Save changes
                    //
                    if (!Util.isEmpty(rowEntity.participantType) && !Util.isEmpty(rowEntity.participantLdapId)) {
                        $scope.updateRow(rowEntity);
                    }
                });
            });


            $q.all([promiseTypes, promiseUsers, promiseGroups]).then(function (data) {
                $scope.gridOptions.enableRowSelection = false;    //need to turn off for inline edit
                //$scope.gridOptions.enableCellEdit = true;
                //$scope.gridOptions.enableCellEditOnFocus = true;
                for (var i = 0; i < $scope.config.columnDefs.length; i++) {
                    if (HelperUiGridService.Lookups.PARTICIPANT_TYPES == $scope.config.columnDefs[i].lookup) {
                        $scope.gridOptions.columnDefs[i].enableCellEdit = true;
                        $scope.gridOptions.columnDefs[i].editableCellTemplate = "ui-grid/dropdownEditor";
                        $scope.gridOptions.columnDefs[i].editDropdownIdLabel = "type";
                        $scope.gridOptions.columnDefs[i].editDropdownValueLabel = "name";
                        $scope.gridOptions.columnDefs[i].editDropdownOptionsArray = $scope.participantTypes;
                        $scope.gridOptions.columnDefs[i].cellFilter = "mapKeyValue: col.colDef.editDropdownOptionsArray:'type':'name'";


                    } else if (HelperUiGridService.Lookups.PARTICIPANT_NAMES == $scope.config.columnDefs[i].lookup) {
                        //$scope.gridOptions.columnDefs[i].enableCellEdit = true;
                        //$scope.gridOptions.columnDefs[i].editableCellTemplate = "ui-grid/dropdownEditor";
                        //$scope.gridOptions.columnDefs[i].editDropdownValueLabel = "name";
                        //$scope.gridOptions.columnDefs[i].editDropdownRowEntityOptionsArrayPath = "acm$_participantNames";
                        //$scope.gridOptions.columnDefs[i].cellFilter = "mapKeyValue: row.entity.acm$_participantNames:'id':'name'";
                        $scope.gridOptions.columnDefs[i].cellTemplate = "<div class='ui-grid-cell-contents' ng-click='grid.appScope.pickParticipant(row.entity)'>{{row.entity[col.field] | mapKeyValue: row.entity.acm$_participantNames:'id':'name'}}</div>";
                    }
                }
            });

            return config;
        });

        $scope.pickParticipant = function (rowEntity) {
            var params = {};
            if (rowEntity.acm$_participantNames == $scope.participantUsers) {
                params.header = $translate.instant("complaints.comp.participants.dialogUserPicker.header");
                params.filter = '"Object Type": USER';
                params.config = Util.goodMapValue($scope.config, "dialogUserPicker");

            } else if (rowEntity.acm$_participantNames == $scope.participantGroups) {
                params.header = $translate.instant("complaints.comp.participants.dialogGroupPicker.header");
                params.filter = '"Object Type": GROUP';
                params.config = Util.goodMapValue($scope.config, "dialogGroupPicker");

            } else { //if ("*" == Util.goodValue(rowEntity.participantType)) {
                return;
            }

            var modalInstance = $modal.open({
                templateUrl: "modules/complaints/views/components/complaint-participant-picker.dialog.html"
                , controller: 'Complaints.ParticipantPickerController'
                , animation: true
                , size: 'lg'
                , resolve: {
                    params: function () {
                        return params;
                    }
                }
            });
            modalInstance.result.then(function (selected) {
                if (!Util.isEmpty(selected)) {
                    rowEntity.participantLdapId = selected.object_id_s;
                    $scope.updateRow(rowEntity);
                }
            });
        };

        var promiseTypes = ObjectLookupService.getParticipantTypes().then(
            function (participantTypes) {
                $scope.participantTypes = participantTypes;
                return participantTypes;
            }
        );

        var promiseUsers = LookupService.getUsersBasic().then(
            function (participantUsers) {
                $scope.participantUsers = participantUsers;
                return participantUsers;
            }
        );

        var promiseGroups = ObjectLookupService.getGroups().then(
            function (participantGroups) {
                $scope.participantGroups = participantGroups;
                return participantGroups;
            }
        );

        var updateGridData = function (data) {
            $q.all([promiseTypes, promiseUsers, promiseGroups, promiseConfig]).then(function () {
                var participants = data.participants;
                _.each(participants, function (participant) {
                    if ("*" === participant.participantType) {
                        participant.acm$_participantNames = [
                            {id: "*", name: "*"}
                        ];
                    } else if ("owning group" === participant.participantType) {
                        participant.acm$_participantNames = $scope.participantGroups;
                    } else {
                        participant.acm$_participantNames = $scope.participantUsers;
                    }
                });
                $scope.gridOptions.data = participants;
                $scope.complaintInfo = data;
                //gridHelper.hidePagingControlsIfAllDataShown(participants.length);
            });
        };

        var currentObjectId = HelperObjectBrowserService.getCurrentObjectId();
        if (Util.goodPositive(currentObjectId, false)) {
            ComplaintInfoService.getComplaintInfo(currentObjectId).then(function (complaintInfo) {
                updateGridData(complaintInfo);
                return complaintInfo;
            });
        }

        $scope.$on('object-refreshed', function (e, complaintInfo) {
            updateGridData(complaintInfo);
        });


        $scope.addNew = function () {
            var lastPage = $scope.gridApi.pagination.getTotalPages();
            $scope.gridApi.pagination.seek(lastPage);
            $scope.gridOptions.data.push({});
        };
        $scope.updateRow = function (rowEntity) {
            var complaintInfo = Util.omitNg($scope.complaintInfo);
            ComplaintInfoService.saveComplaintInfo(complaintInfo).then(
                function (complaintSaved) {
                    //if participant is newly added, fill incomplete values with the latest
                    if (Util.isEmpty(rowEntity.id)) {
                        var participants = Util.goodMapValue(complaintSaved, "participants", []);
                        var participantAdded = _.find(participants, {
                            participantType: rowEntity.participantType,
                            participantLdapId: rowEntity.participantLdapId
                        });
                        if (participantAdded) {
                            rowEntity = _.merge(rowEntity, participantAdded);
                        }
                    }
                    return complaintSaved;
                }
            );
        };
        $scope.deleteRow = function (rowEntity) {
            gridHelper.deleteRow(rowEntity);

            var id = Util.goodMapValue(rowEntity, "id", 0);
            if (0 < id) {    //do not need to call service when deleting a new row
                var complaintInfo = Util.omitNg($scope.complaintInfo);
                ComplaintInfoService.saveComplaintInfo(complaintInfo);
            }

        };
    }
]);


angular.module('directives').controller('Complaints.ParticipantPickerController', ['$scope', '$modalInstance', 'params'
        , function ($scope, $modalInstance, params) {
            $scope.modalInstance = $modalInstance;
            $scope.header = params.header;
            $scope.filter = params.filter;
            $scope.config = params.config;
        }
    ]
);
