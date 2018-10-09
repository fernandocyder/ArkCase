'use strict';

angular.module('document-details').controller(
        'DocumentDetailsController',
        [ '$scope', '$stateParams', '$sce', '$q', '$timeout', '$window', 'TicketService', 'ConfigService', 'LookupService', 'SnowboundService', 'Authentication', 'EcmService', 'Helper.LocaleService', 'Admin.TranscriptionManagementService', 'MessageService', 'UtilService', 'Util.TimerService',
                'Object.LockingService', 'ObjectService', '$log',
                function($scope, $stateParams, $sce, $q, $timeout, $window, TicketService, ConfigService, LookupService, SnowboundService, Authentication, EcmService, LocaleHelper, TranscriptionManagementService, MessageService, Util, UtilTimerService, ObjectLockingService, ObjectService, $log) {

                    new LocaleHelper.Locale({
                        scope: $scope
                    });

                    $scope.viewerOnly = false;
                    $scope.documentExpand = function() {
                        $scope.viewerOnly = true;
                    };
                    $scope.documentCompress = function() {
                        $scope.viewerOnly = false;
                    };
                    $scope.checkEscape = function(event) {
                        if (27 == event.keyCode) { // 27 is Escape key code
                            $scope.viewerOnly = false;
                        }
                    };

                    $scope.videoAPI = null;

                    $scope.videoExpand = function() {
                        if (!Util.isEmpty($scope.videoAPI)) {
                            $scope.videoAPI.toggleFullScreen();
                        }
                    };

                    $scope.acmTicket = '';
                    $scope.userId = '';
                    $scope.ecmFileProperties = {};
                    $scope.snowboundUrl = '';
                    $scope.ecmFileEvents = [];
                    $scope.ecmFileParticipants = [];
                    $scope.userList = [];
                    $scope.caseInfo = {};
                    $scope.fileInfo = {
                        id: $stateParams['id'],
                        containerId: $stateParams['containerId'],
                        containerType: $stateParams['containerType'],
                        name: $stateParams['name'],
                        selectedIds: $stateParams['selectedIds']
                    };
                    $scope.showVideoPlayer = false;
                    $scope.showPdfJs = false;
                    $scope.transcriptionTabActive = false;

                    var transcriptionConfigurationPromise = TranscriptionManagementService.getTranscribeConfiguration();

                    $scope.getEcmFileActiveVersion = function(ecmFile) {
                        if (Util.isEmpty(ecmFile) || Util.isEmpty(ecmFile.activeVersionTag) || Util.isArrayEmpty(ecmFile.versions)) {
                            return null;
                        }

                        var activeVersion = _.find(ecmFile.versions, function(version) {
                            return ecmFile.activeVersionTag === version.versionTag;
                        });

                        return activeVersion;
                    };

                    $scope.$on('transcribe-data-model', function(event, transcribeObj) {
                        $scope.transcribeObjectModel = transcribeObj;

                        var track = null;
                        var videoElement = angular.element(document.getElementsByTagName("video")[0])[0];
                        if (videoElement) {
                            track = videoElement.addTextTrack("subtitles", "Transcription", $scope.transcribeObjectModel.language);
                            videoElement.addEventListener("play", function() {
                                track.mode = "showing";
                            });
                        }

                        angular.forEach($scope.transcribeObjectModel.transcribeItems, function(value, key) {
                            if (track != null) {
                                addCue(track, value);
                            }
                        });

                        // color the status
                        $scope.colorTranscribeStatus = function() {
                            switch ($scope.transcribeObjectModel.status) {
                            case 'QUEUED':
                                return '#dcce22';
                            case 'PROCESSING':
                                return 'orange';
                            case 'COMPLETED':
                                return '#05a205';
                            case 'FAILED':
                                return 'red';
                            }
                        };

                        if (!Util.isEmpty($stateParams.seconds)) {
                            $scope.playAt($stateParams.seconds);
                        }
                    });

                    var addCue = function(track, value) {
                        var cueAdded = false;
                        try {
                            track.addCue(new VTTCue(value.startTime, value.endTime, value.text));
                            cueAdded = true;
                        } catch (e) {
                            $log.warn("Browser does not support VTTCue");
                        }

                        if (!cueAdded) {
                            try {
                                track.addCue(new TextTrackCue(value.startTime, value.endTime, value.text));
                                cueAdded = true;
                            } catch (e) {
                                $log.warn("Browser does not support TextTrackCue");
                            }
                        }
                    }

                    $scope.playAt = function(seconds) {
                        var videoElement = angular.element(document.getElementsByTagName("video")[0])[0];
                        if (videoElement) {
                            videoElement.pause();
                            videoElement.currentTime = seconds;
                            videoElement.play();
                        }
                    }

                    /**
                     * Builds the snowbound url based on the parameters passed into the controller state and opens the specified document in
                     * an iframe which points to snowbound
                     */
                    $scope.openSnowboundViewer = function() {
                        var viewerUrl = SnowboundService.buildSnowboundUrl($scope.ecmFileProperties, $scope.acmTicket, $scope.userId, $scope.fileInfo, !$scope.editingMode);
                        $scope.documentViewerUrl = $sce.trustAsResourceUrl(viewerUrl);
                    };

                    $scope.$bus.subscribe('update-viewer-opened-versions', function(openedVersions) {
                        $scope.fileInfo.selectedIds = openedVersions.map(function(openedVersion, index) {
                            if (index == 0) {
                                $scope.fileInfo.id = $stateParams['selectedIds'] + ":" + openedVersion.versionTag;
                            }
                            return $stateParams['selectedIds'] + ":" + openedVersion.versionTag;
                        }).join(',');

                        $scope.openSnowboundViewer();
                    });

                    // Obtains authentication token for ArkCase
                    var ticketInfo = TicketService.getArkCaseTicket();

                    // Obtains the currently logged in user
                    var userInfo = Authentication.queryUserInfo();

                    // Obtains a list of all users in ArkCase
                    var totalUserInfo = LookupService.getUsers();

                    // Retrieves the properties from the ecmFileService.properties file (including Snowbound configuration)
                    var ecmFileConfig = LookupService.getConfig("ecmFileService");

                    var formsConfig = LookupService.getConfig("acm-forms");

                    // Retrieves the metadata for the file which is being opened in the viewer
                    var ecmFileInfo = EcmService.getFile({
                        fileId: $stateParams['id']
                    });
                    var ecmFileEvents = EcmService.getFileEvents({
                        fileId: $stateParams['id']
                    });
                    var ecmFileParticipants = EcmService.getFileParticipants({
                        fileId: $stateParams['id']
                    });

                    $q.all([ ticketInfo, userInfo, totalUserInfo, ecmFileConfig, ecmFileInfo.$promise, ecmFileEvents.$promise, ecmFileParticipants.$promise, formsConfig, transcriptionConfigurationPromise ]).then(function(data) {
                        $scope.acmTicket = data[0].data;
                        $scope.userId = data[1].userId;
                        $scope.userList = data[2];
                        $scope.ecmFileProperties = data[3];
                        $scope.editingMode = !$scope.ecmFileProperties['ecm.viewer.snowbound.readonly.initialState'];
                        $scope.ecmFile = data[4];
                        $scope.ecmFileEvents = data[5];
                        $scope.ecmFileParticipants = data[6];
                        $scope.formsConfig = data[7];
                        $scope.transcriptionConfiguration = data[8];

                        // default view == snowbound
                        $scope.view = "modules/document-details/views/document-viewer-snowbound.client.view.html";

                        $scope.transcribeEnabled = $scope.transcriptionConfiguration.data.enabled;

                        $timeout(function() {
                            $scope.$broadcast('document-data', $scope.ecmFile);
                        }, 1000);

                        var key = $scope.ecmFile.fileType + ".name";
                        // Search for descriptive file type in acm-forms.properties
                        $scope.fileType = $scope.formsConfig[key];
                        if ($scope.fileType === undefined) {
                            // If descriptive file type does not exist, fallback to previous raw file type
                            $scope.fileType = $scope.ecmFile.fileType;
                        }

                        $scope.mediaType = $scope.ecmFile.fileActiveVersionMimeType.indexOf("video") === 0 ? "video" : ($scope.ecmFile.fileActiveVersionMimeType.indexOf("audio") === 0 ? "audio" : $scope.ecmFile.fileActiveVersionMimeType.indexOf("application/pdf") === 0 ? "pdf" : "other");

                        if ($scope.mediaType === "video" || $scope.mediaType === "audio") {
                            $scope.config = {
                                sources: [ {
                                    src: $sce.trustAsResourceUrl('api/latest/plugin/ecm/stream/' + $scope.ecmFile.fileId),
                                    type: $scope.ecmFile.fileActiveVersionMimeType
                                } ],
                                theme: "lib/videogular-themes-default/videogular.css",
                                plugins: {
                                    poster: "branding/loginlogo.png"
                                },
                                autoPlay: false
                            };
                            $scope.showVideoPlayer = true;
                            $scope.view = "modules/document-details/views/document-viewer-videogular.client.view.html";
                        } else if ($scope.mediaType === "pdf" && "pdfjs" === $scope.ecmFileProperties['ecm.viewer.pdfViewer']) {
                            $scope.config = {
                                src: $sce.trustAsResourceUrl('api/latest/plugin/ecm/stream/' + $scope.ecmFile.fileId)
                            };
                            $scope.showPdfJs = true;
                            $scope.view = "modules/document-details/views/document-viewer-pdfjs.client.view.html";
                        } else {
                            // Opens the selected document in the snowbound viewer
                            $scope.openSnowboundViewer();
                        }

                        $scope.transcriptionTabActive = $scope.showVideoPlayer && $scope.transcribeEnabled;
                    });

                    $scope.onPlayerReady = function(API) {
                        $scope.videoAPI = API;
                    }

                    $scope.enableEditing = function() {
                        ObjectLockingService.lockObject($scope.ecmFile.fileId, ObjectService.ObjectTypes.FILE, ObjectService.LockTypes.WRITE, true).then(function(lockedFile) {
                            $scope.editingMode = true;
                            $scope.openSnowboundViewer();
                            
                            // count user idle time. When user is idle for more then 1 minute, don't acquire lock
                            $scope._idleSecondsCounter = 0;
                            document.onclick = function() {
                                $scope._idleSecondsCounter = 0;
                            };
                            document.onmousemove = function() {
                                $scope._idleSecondsCounter = 0;
                            };
                            document.onkeypress = function() {
                                $scope._idleSecondsCounter = 0;
                            };                            
                            function incrementIdleSecondsCounter() {
                                $scope._idleSecondsCounter++;
                            }
                            window.setInterval(incrementIdleSecondsCounter, 1000);
                            
                            // Refresh editing lock on timer
                            UtilTimerService.useTimer('refreshFileEditingLock', 60000 // every 1 minute
                            , function() {
                                if ($scope._idleSecondsCounter <= 60) {
                                    ObjectLockingService.lockObject($scope.ecmFile.fileId, ObjectService.ObjectTypes.FILE, ObjectService.LockTypes.WRITE, true).then(undefined, function(errorMessage) {
                                        MessageService.error(errorMessage.data);
                                    });
                                }
                                return true;
                            });
                        }, function(errorMessage) {
                            MessageService.error(errorMessage.data);
                        });

                    }

                    // Release editing lock on window unload, if acquired
                    $window.addEventListener('beforeunload', function() {
                        if ($scope.editingMode) {
                            ObjectLockingService.unlockObject($scope.ecmFile.fileId, ObjectService.ObjectTypes.FILE, ObjectService.LockTypes.WRITE);
                        }
                    });

                } ]);

/**
 * 2018-06-01 David Miller. This block is needed to tell the PDF.js angular module, where the PDF.js library is. Without this, on minified
 * systems the PDF.js viewer will not work. I copied this from the project web page,
 * https://github.com/legalthings/angular-pdfjs-viewer#advanced-configuration.
 * 
 * @param pdfjsViewerConfigProvider
 * @returns
 */
angular.module('document-details').config(function(pdfjsViewerConfigProvider) {
    pdfjsViewerConfigProvider.setWorkerSrc("lib/pdf.js-viewer/pdf.worker.js");
    pdfjsViewerConfigProvider.setCmapDir("lib/pdf.js-viewer/cmaps");
    pdfjsViewerConfigProvider.setImageDir("lib/pdf.js-viewer/images");

    // pdfjsViewerConfigProvider.disableWorker();
    pdfjsViewerConfigProvider.setVerbosity("infos"); // "errors", "warnings" or "infos"
});
