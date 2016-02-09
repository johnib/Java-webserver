'use strict';

angular.module('myApp.view1', ['ngRoute'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/view1', {
            templateUrl: 'view1/view1.html',
            controller: 'View1Ctrl'
        });
    }])

    .service('CrawlServerExecutor', ['$http', '$q', function ($http, $q) {

        //TODO: put server address
        var serverUrl = "";

        function logResults(results) {
            if (results) {
                results.forEach(function (result) {
                    console.log(result);
                });
            } else {
                console.log("No history");
            }
        }

        this.getResults = function () {
            console.log("Retrieving history");
            var deferred = $q.defer();

            $http.get(serverUrl + '/get-history')
                .then(function (res) {
                    deferred.resolve(res.data.results);
                    logResults(res.data.results);
                })
                .catch(deferred.reject);


            return deferred.promise;
        };

        this.crawl = function (crawlConfig) {
            console.log("Executing crawler on ");
            console.log(crawlConfig);

            var deferred = $q.defer();

            $http.post(serverUrl + '/crawl', crawlConfig)
                .then(function (res) {
                    console.log("Received crawling information");
                    deferred.resolve(res.data.results);
                    logResults(res.data.results);
                })
                .catch(function (reason) {
                    deferred.reject(reason);
                });

            return deferred.promise;
        }

    }])

    .controller('View1Ctrl', ['$scope', 'CrawlServerExecutor', function ($scope, CrawlServerExecutor) {

        $scope.crawl = function (config) {
            toggleCrawlingStatus();

            if (!config.url) {
                config.url = "google.com"; // default crawling address
            }

            CrawlServerExecutor.crawl(config)
                .then(function (results) {
                    updateResults(results);
                    setFinish();
                })
                .catch(function (reason) {
                    //TODO: let the user know crawling is in progress
                    console.error(reason);
                    setFailure();
                })
                .finally(toggleCrawlingStatus);
        };


        $scope.reset = function () {
            $scope.config = {
                portScan: false,
                ignoreRobots: false,
                url: "google.com"
            };

            $scope.results = [];
            $scope.crawling = false; // locks the crawl button from being pressed
            $scope.finished = false; // shows green ack message after crawling is done.
            $scope.failure = false; // shows error message if crawling already in progress
        };


        var updateResults = function (results) {
            $scope.results = results;
        };

        var getResults = function () {
            CrawlServerExecutor.getResults()
                .then(updateResults);
        };

        var toggleCrawlingStatus = function () {
            $scope.crawling = !$scope.crawling;
        };

        var setFailure = function () {
            $scope.failure = true;
        };

        var setFinish = function () {
            $scope.finished = true;
        };

        $scope.reset();
        getResults();

    }]);