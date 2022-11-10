## cip-email-validation

### Summary

Backend server for validating a given email

The default port for cip-email-frontend is 6180
The default port for cip-email is port 6181
The default port for cip-email-validation is port 6182
The default port for cip-email-verification is port 6183
The default port for cip-email-stubs is port 6199

### Testing

#### Unit tests

    sbt clean test

#### Integration tests

    sbt clean it:test

### Running app

    sm --start CIP_EMAIL_VALIDATION_ALL

Run the services against the current versions in dev, stop the CIP_EMAIL_VALIDATION service and start manually

    sm --start CIP_EMAIL_VALIDATION_ALL -r
    sm --stop CIP_EMAIL_VALIDATION
    cd cip-email-validation
    sbt run 6182

For reference here are the details for running each of the services individually

    cd cip-email-frontend
    sbt run
 
    cd cip-email
    sbt run

    cd cip-email-validation
    sbt run

    cd cip-email-verification
    sbt run

### Curl microservice (for curl microservice build jobs)

#### Validate

    -XPOST -H "Content-type: application/json" -H "Authorization: fI2zfC5z-xuvDBT7riTHoOssnmqNRJ1IRnBixf8MThPepxTaRoi7pYaTZ9YKG3IP5DdshgpQ5" -d '{
	    "email": "<email>"
    }' 'https://cip-email-validation.protected.mdtp/customer-insight-platform/email/validate'

### License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
