#!/bin/bash
version=$1
mvn dependency:get -DremoteRepositories=github::::https://maven.pkg.github.com/salex-org/smart-home-observer -Dartifact=org.salex.hmip:observer:$version:jar -Dtransitive=false
ln -sf ~/.m2/repository/org/salex/hmip/observer/$version/observer-$version.jar observer.jar
echo 'Version' $version 'of observer installed'
