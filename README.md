# paymentPlatformPoc

To run this springboot project, you can either 1. Use docker for everything, or 2. Use docker just for the postgres db container and IntelliJ to run/debug the project.

For 1. Use docker for everything 

    Make sure you're in the project's root directory and run `docker-compose up --build`
    If you want to hide console logs, you can add a "-d" flag as well.

For 2. Use docker just for the postgres db container and IntelliJ to run/debug the project. 

    a. cd dockerDbForLocalTesting
    b. docker-compose up --build
    c. Open up IntelliJ
    d. trigger the gradle build command via IntelliJ
    e. Right clicking on PaymentPlatformPocApplication and select "Run 'PaymentPlatformPocApplication'

Once the application is running, you can test it using grpcurl (https://yidongnan.github.io/grpc-spring-boot-starter/en/server/testing.html#grpcurl)
or use a grpcgui tool like https://github.com/gusaul/grpcox

Note that if you're running the grpcgui tool from IntelliJ, the access host/port for this application will be localhost:9090

whereas if you're running the grpcgui tool from docker, the access host/port for this application will be host.docker.internal:9090
