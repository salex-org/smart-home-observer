#!/bin/bash
docker compose --file ./docker-compose.yml --project-name smart-home down
#TODO: Update for smart-home-observer
#sudo rm -rf ./data
#sudo tar -xf ./backup/db.tar data/db
#sudo tar -xf ./backup/wp.tar data/wp
docker compose --file ./docker-compose.yml --project-name smart-home up --detach