module.exports = function(config) {
    config.set({
        basePath: '../../..',
        frameworks: ['browserify','jasmine'],
        files: [
            'src/main/javascript/*.js',
            'src/test/javascript/*.js'
        ],
         preprocessors: {
              'test/**/*.js': [ 'browserify' ]
            },
        exclude: ['src/test/javascript/karma.conf*.js'],
        browserify: {
              debug: true,
              transform: [ 'brfs' ]
            },
        reporters: ['progress'],
        port: 9876,
        logLevel: config.LOG_INFO,
        browsers: ['PhantomJS'],
        singleRun: false,
        autoWatch: true,
        plugins: [
            'karma-jasmine',
            'karma-phantomjs-launcher'
        ]
    });
};