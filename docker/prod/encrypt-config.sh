#!/bin/bash
docker exec -it smart-home-observer-1 /smart-home-observer -o config/observer-config.yml -i config/observer-config.decrypted.yml encrypt-config