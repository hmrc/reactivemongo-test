#!/bin/bash -x

printf "\n\n\n\n"
export PLAY_VERSION="2.5"

sbt version
sbt "show libraryDependencies"
sbt clean test

printf "\n\n\n\n"
export PLAY_VERSION="2.6"

sbt version
sbt "show libraryDependencies"
sbt clean test

printf "\n\n\n\n"
unset PLAY_VERSION

sbt version
sbt "show libraryDependencies"
sbt clean test