var HtmlScreenshotReporter = require(process.env['USERPROFILE'] + '/node_modules/protractor-jasmine2-screenshot-reporter');
var utils = require('../util/utils.js');
var Objects = require('../json/Objects.json');
var reporter = new HtmlScreenshotReporter({
    dest: 'target/screenshots_' + utils.returnToday("_"),
    filename: 'AutoTestRun-report.html'
});
exports.config = {
    //seleniumAddress: 'http://localhost:4444/wd/hub',
    directConnect: true,
    defaultTimeoutInterval: 200000,

    cababilities: {
        'browserName': 'chrome'
    },

    framework: 'jasmine2',
    //next 3 lines are for run on selenium GRID, the path should be changed to take drivers from ACM configuraion project
    //not working for safari browser, should be investigated why? 
    //seleniumAddress: 'http://localhost:4444/wd/hub',
    //seleniumArgs: '-Dwebdriver.ie.driver='+process.env['USERPROFILE']+'/AppData/Roaming/npm/node_modules/protractor/selenium/IEDriverServer_x64_2.52.0.exe',
    //seleniumArgs: '-Dwebdriver.safari.driver='+process.env['USERPROFILE']+'/AppData/Roaming/npm/node_modules/protractor/selenium/SafariDriver.safariextz',
    // Capabilities to be passed to the webdriver instance.
    multiCapabilities: [{
        'browserName': 'chrome',
        'maxInstances': 5
    }],
    //if you want to run in paralel comment previous line and uncomment all above
    //    }, {
    //          'browserName': 'internet explorer',
    //          'maxInstances': 5,
    //          'version': '11'
    //     }, {
    //            'browserName': 'firefox',
    //            'maxInstances': 5

    //      }, {
    //      'browserName': 'safari' ,
    //     'maxInstances': 5
    // }],

    specs: [

        //any test can be run with command "protractor conf.js, just place it here"
       
    ],
        //any suite can be run with command "protractor conf.js --suite=selected"

      
    suites: {

        smoke: ['../test_spec/smoke/*.spec.js'],
        regression: ['../test_spec/regression/*.spec.js'],
        functional: ['../test_spec/functional/*.spec.js'],
        all: ['../test_spec/*/*.spec.js'],
        selected: [ '../test_spec/smoke/smoke_case_test.spec.js'],
    },

    jasmineNodeOpts: {
        showColors: true,

        defaultTimeoutInterval: 1200000

    },
    beforeLaunch: function() {
        return new Promise(function(resolve) {
            reporter.beforeLaunch(resolve);
        });
    },
    onPrepare: function() {
        jasmine.getEnv().addReporter(reporter);

        browser.driver.manage().window().maximize();
        browser.driver.get(Objects.siteurl);
        browser.manage().timeouts().setScriptTimeout(90000);
        browser.manage().timeouts().pageLoadTimeout(40000);
    },
    afterLaunch: function(exitCode) {
        return new Promise(function(resolve) {
            reporter.afterLaunch(resolve.bind(this, exitCode));
        });
    }
};
