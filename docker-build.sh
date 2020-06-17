#!/bin/bash

docker pull adoptopenjdk/openjdk11:alpine-slim
sbt assembly

docker image rm oac-ui
docker image rm oac-data
docker image rm oac-winchecker

docker build ./OacUi -t oac-ui
docker build ./OacData -t oac-data
docker build ./OacWinChecker -t oac-winchecker