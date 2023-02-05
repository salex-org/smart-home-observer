#!/bin/bash
docker exec -it smart-home-observer-1 /smart-home-observer -o docker/local/observer/config/observer-config.yml -i docker/local/observer/config/observer-config.decrypted.yml encrypt-config