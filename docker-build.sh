#!/bin/bash

docker pull adoptopenjdk/openjdk11:alpine-slim
sbt assembly

docker build ./OacUi -t oac-ui
docker build ./OacData -t oac-data
docker build ./OacWinChecker -t oac-winchecker