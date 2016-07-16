var swaggerTest = require('swagger-test');

var swaggerSpec = "swagger.yaml";// load a Swagger specification as a JavaScript object
var xamples = swaggerTest.parse(swaggerSpec);

var preq = require('preq');

var java = require('java');
var mvn = require('node-java-maven');

mvn(function(err, mvnResults) {
  if (err) {
    return console.error('could not resolve maven dependencies', err);
  }
  mvnResults.classpath.forEach(function(c) {
    console.log('adding ' + c + ' to classpath');
    java.classpath.push(c);
  });

  var Version = java.import('org.apache.lucene.util.Version');
});

describe('specification-driven tests', function () {
  xamples.forEach(function (xample) {
    it(xample.description, function() {
      return preq[xample.request.method](xample.request)
      .then(function (response) {
        assert.deepEqual(response, xample.response);
      });
    });
  });
});

