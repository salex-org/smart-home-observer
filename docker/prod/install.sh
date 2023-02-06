#!/bin/bash
version=$1
docker compose --file ./docker-compose.yml --project-name smart-home down
sudo curl -LO https://github.com/salex-org/smart-home-observer/releases/download/v$version/scripts.tar.gz
sudo tar xfvz scripts.tar.gz
sudo rm scripts.tar.gz
docker compose --file ./docker-compose.yml --project-name smart-home up --detach
docker attach smart-home-observer-1