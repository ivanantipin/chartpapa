FROM openjdk:13
ADD ./build/distributions/production-0.0.1-SNAPSHOT.tar /app/
WORKDIR /app
ENTRYPOINT ["/app/production-0.0.1-SNAPSHOT/bin/production"]