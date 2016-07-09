var swaggerTest = require('swagger-test');

var swaggerSpec = "swagger.yaml";// load a Swagger specification as a JavaScript object
var xamples = swaggerTest.parse(swaggerSpec);

var preq = require('preq');

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

