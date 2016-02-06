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
            results.forEach(function (result) {
                console.log(result);
            });
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
                .catch(deferred.reject);

            return deferred.promise;
        }

    }])

    .controller('View1Ctrl', ['$scope', 'CrawlServerExecutor', function ($scope, CrawlServerExecutor) {

        $scope.crawling = false;


        $scope.crawl = function (config) {
            updateCrawlingStatus();

            if (!config.url) {
                config.url = "google.com"; // default crawling address
            }

            CrawlServerExecutor.crawl(config)
                .then(updateResults)
                .catch(function (reason) {
                    console.error(reason);
                })
                .finally(updateCrawlingStatus);
        };


        $scope.reset = function () {
            $scope.config = {
                portScan: false,
                ignoreRobots: false,
                url: ""
            };

            $scope.results = [];
        };


        var updateResults = function (results) {
            $scope.results = results;
        };

        var getResults = function () {
            CrawlServerExecutor.getResults()
                .then(updateResults);
        };

        var updateCrawlingStatus = function () {
            $scope.crawling = !$scope.crawling;
        };

        $scope.reset();
        getResults();

    }]);