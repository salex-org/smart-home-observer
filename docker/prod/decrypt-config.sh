#!/bin/bash
docker exec -it smart-home-observer-1 /smart-home-observer -i docker/local/observer/config/observer-config.yml -o docker/local/observer/config/observer-config.decrypted.yml decrypt-config