var gulp = require('gulp');
var Server = require('karma').Server;


var concat         = require('gulp-concat');
var concatVendor   = require('gulp-concat-vendor');
var uglify         = require('gulp-uglify');
var minify         = require('gulp-minify-css')
var mainBowerFiles = require('main-bower-files');
var inject         = require('gulp-inject');
var runSequence    = require('gulp-run-sequence');
var gzip           = require('gulp-gzip');
var clone          = require('gulp-clone');
var series         = require('stream-series');

var vendorJs;
var vendorCss;

gulp.task('lib-js-files', function () {
    vendorJs = gulp.src(mainBowerFiles('**/*.js'),{ base: 'bower_components' })
        .pipe(concatVendor('lib.min.js'))
        .pipe(uglify())
        .pipe(gulp.dest('src/main/webapp/resources/vendor/js'));

    vendorJs.pipe(clone())
        .pipe(gzip())
        .pipe(gulp.dest('src/main/webapp/resources/vendor/js'));
});

gulp.task('lib-css-files', function () {
    vendorCss = gulp.src(mainBowerFiles('**/*.css'), {base: 'bower_components'})
        .pipe(concat('lib.min.css'))
        .pipe(minify())
        .pipe(gulp.dest('src/main/webapp/resources/vendor/css'));

    vendorCss.pipe(clone())
        .pipe(clone())
        .pipe(gzip())
        .pipe(gulp.dest('src/main/webapp/resources/vendor/css'));
});

gulp.task('index', function () {
    var target = gulp.src("src/main/webapp/index.html");
    var sources = gulp.src(['src/main/webapp/resources/js/*.js', 'src/main/webapp/resources/css/*.css'], {read: false});
    return target.pipe(inject(series(vendorJs, vendorCss, sources), {relative: true}))
        .pipe(gulp.dest('src/main/webapp'));
});

gulp.task('copyFonts', function() {
    gulp.src(mainBowerFiles('**/dist/fonts/*.{ttf,woff,woff2,eof,svg}'))
        .pipe(gulp.dest('src/main/webapp/resources/vendor/fonts'));
});

/**
 * Run test once and exit
 */
gulp.task('test', function (done) {
  new Server({
    configFile: __dirname + '/src/test/javascript/karma.conf.js',
    singleRun: true
  }, done).start();
});

/**
 * Watch for file changes and re-run tests on each change
 */
gulp.task('tdd', function (done) {
  new Server({
    configFile: __dirname + '/src/test/javascript/karma.conf.js'
  }, done).start();
});

// Default Task
gulp.task('default', function () {
    runSequence('lib-js-files', 'lib-css-files', 'index', 'copyFonts');
});
