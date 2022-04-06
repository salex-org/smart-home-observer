#!/bin/bash
read -sp 'Encryption key: ' ek
echo
ENCRYPT_KEY=$ek java -jar observer.jar --spring.profiles.active=prod 2> ~/logs/stderr.log 1> ~/logs/stdout.log &
echo 'Observer started'
