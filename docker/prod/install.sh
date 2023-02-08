#!/bin/bash
version=$1
SMART_HOME_USER=$(id -u smart-home):$(id -g smart-home) docker compose --file ./docker-compose.yml --project-name smart-home down
sudo curl -LO https://github.com/salex-org/smart-home-observer/releases/download/v$version/scripts.tar.gz
sudo tar xfvz scripts.tar.gz
sudo rm scripts.tar.gz
SMART_HOME_USER=$(id -u smart-home):$(id -g smart-home) docker compose --file ./docker-compose.yml --project-name smart-home up --detach
docker attach smart-home-observer