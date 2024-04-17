FROM openjdk:21-slim
EXPOSE 8080
WORKDIR /app
COPY target/mongodb-demo*.jar mongodb-demo.jar
ENTRYPOINT [ "java","-jar","mongodb-demo.jar" ]