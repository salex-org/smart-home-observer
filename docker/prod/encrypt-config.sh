#!/bin/bash
docker exec -it smart-home-observer /smart-home-observer -o config/observer-config.yml -i config/observer-config.decrypted.yml encrypt-config
docker restart smart-home-observer
docker attach smart-home-observer