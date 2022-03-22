#!/bin/bash
read -sp 'Encryption key: ' ek
echo
ENCRYPT_KEY=$ek java -jar observer.jar 2>&1 | multilog s1024 n10 ~/observer/logs &
echo 'Observer started'
