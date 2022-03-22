#!/bin/bash
read -sp 'Encryption key: ' ek
echo
ENCRYPT_KEY=$ek java -jar observer.jar --spring.profiles.active=prod 2>&1 | multilog s10485760 n10 ~/observer/logs &
echo 'Observer started'
