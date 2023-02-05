#!/bin/bash
docker compose --file ./docker-compose.yml --project-name smart-home up --detach
docker attach smart-home-observer-1