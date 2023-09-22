#dockerfile
FROM ubuntu:latest

#Creating a user that would own everything in our project dir
RUN groupadd --gid 1000 tguser \
    && useradd --uid 1000 --gid tguser --shell /bin/bash --create-home tguser

#Setting up environment
RUN apt-get update -y && \
yes | apt-get upgrade && \
#Install TDLib dependencies
yes | apt-get install zlib1g-dev && \
yes | apt-get install libssl-dev && \
#Install Java
apt-get install openjdk-11-jdk -y

#Setting up WORKDIR
WORKDIR /home
#RUN chmod 777 /Dnipro-exchange

RUN mkdir -p log/
RUN mkdir -p tdlib/
RUN mkdir -p tdlib/log/

RUN chown tguser -R tdlib/
RUN chown tguser tdlib/log/
RUN chown tguser log/
USER tguser

#Add jar file generated via mvn 'cleaninstall spring-boot:repackage'
ADD Rate-parser-1.0-SNAPSHOT.jar tg-spring.jar
#Add TDLib
ADD libtdjni.so /TDLib/libtdjni.so

#Expose ports outside of our docker image
EXPOSE 8080

#Start application
CMD java -cp tg-spring.jar -Dspring.profiles.active=prod -Djava.library.path="/TDLib" -jar tg-spring.jar --api.token=huyiCb9YZN30LFOcz5H7obc4Eu3WRfV0nMx3usjRUEKm53TObh4pxuQRdfs0u9zrql3jBRtJCMGt5xyl

#/usr/lib/jvm/java-11-openjdk-amd64