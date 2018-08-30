#!/bin/bash -xe

export PLAY_VERSION="2.5"
sbt "; version; show libraryDependencies; clean; test"

export PLAY_VERSION="2.6"
sbt "; version; show libraryDependencies; clean; test"