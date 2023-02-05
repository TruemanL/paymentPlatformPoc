FROM gradle:jdk17-focal as TEMP_BUILD_IMAGE
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME

# only download dependencies first so docker can cache dependencies layer
COPY build.gradle.kts settings.gradle.kts $APP_HOME
# the "|| true" is meant to silently ignore expected failure due to no source code copied at this stage
RUN gradle clean assemble --no-daemon > /dev/null 2>&1 || true

COPY . $APP_HOME
RUN gradle clean assemble --no-daemon

# actual container
FROM amazoncorretto:17
ENV ARTIFACT_NAME=paymentPlatformPoc-0.0.1-SNAPSHOT.jar
ENV APP_HOME=/usr/app/

WORKDIR $APP_HOME
COPY --from=TEMP_BUILD_IMAGE $APP_HOME/build/libs/$ARTIFACT_NAME .

EXPOSE 9090
ENTRYPOINT exec java -jar ${ARTIFACT_NAME}
