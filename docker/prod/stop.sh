#!/bin/bash
SMART_HOME_USER=$(id -u smart-home):$(id -g smart-home) docker compose --file ./docker-compose.yml --project-name smart-home down