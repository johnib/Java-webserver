'use strict';

// Declare app level module which depends on views, and components
angular.module('myApp', [
        'ngRoute',
        'myApp.view1'
    ])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider
            .when('/results/:fileName', {
                templateUrl: function (params) {
                    //noinspection JSUnresolvedVariable
                    return './Results/' + params.fileName;
                }
            })

            .otherwise({redirectTo: '/view1'});
    }]);
