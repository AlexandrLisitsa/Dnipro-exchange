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
apt-get install openjdk-18-jdk -y \

#Install Python
RUN apt-get install python3 -y
RUN apt-get install python3-pip -y

#Install ner.py dependecnies
RUN pip3 install spacy
RUN python3 -m spacy download ru_core_news_md

#Setting up WORKDIR
WORKDIR /tgcrawler

RUN mkdir -p log/
RUN mkdir -p tdlib/
RUN mkdir -p tdlib/log/

RUN chown tguser -R tdlib/
RUN chown tguser tdlib/log/
RUN chown tguser log/
USER tguser

#Add jar file generated via mvn 'cleaninstall spring-boot:repackage'
#ADD ./target/tg-1.0-SNAPSHOT-spring-boot.jar tg-spring.jar
#Add TDLib
ADD /src/main/resources/libtdjni.so /TDLib/libtdjni.so
#Add ner.py
#ADD src/main/python/ner.py ner.py

#Expose ports outside of our docker image
EXPOSE 8080
EXPOSE 5432
#EXPOSE 9243

#Start application
#CMD java -cp tg-spring.jar -Djava.library.path="/TDLib" -Xmx1200M -Dloader.main=net.broscorp.Application org.springframework.boot.loader.PropertiesLauncher
