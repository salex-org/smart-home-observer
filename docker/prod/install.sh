#!/bin/bash
version=$1
curl -LO https://github.com/salex-org/smart-home-observer/releases/download/v$version/scripts.tar.gz
tar xfvz scripts.tar.gz
rm scripts.tar.gz
