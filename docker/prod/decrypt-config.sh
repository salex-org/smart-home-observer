#!/bin/bash
docker exec -it smart-home-observer-1 /smart-home-observer -i config/observer-config.yml -o config/observer-config.decrypted.yml decrypt-config