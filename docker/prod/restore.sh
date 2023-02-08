#!/bin/bash
SMART_HOME_USER=$(id -u smart-home):$(id -g smart-home) docker compose --file ./docker-compose.yml --project-name smart-home down
#TODO: Update for smart-home-observer
#sudo rm -rf ./data
#sudo tar -xf ./backup/db.tar data/db
#sudo tar -xf ./backup/wp.tar data/wp
SMART_HOME_USER=$(id -u smart-home):$(id -g smart-home) docker compose --file ./docker-compose.yml --project-name smart-home up --detach