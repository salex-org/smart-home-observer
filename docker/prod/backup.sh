#!/bin/bash
echo "Stopping container..."
docker compose --file ./docker-compose.yml --project-name smart-home down
if [ $? == 0 ]; then
    echo "Stopping container successful"
else
    echo "ERROR stopping container"
fi

echo "Removing old local backups..."
rm -f ./backup/*
if [ $? == 0 ]; then
    echo "Removing old local backups successful"
else
    echo "ERROR removing old local backups"
fi

#TODO: Update for smart-home-observer
echo "Creating backup..."
#sudo tar cf ./backup/db.tar data/db
#if [ $? == 0 ]; then
#    echo "Database backup successful"
#else
#    echo "ERROR creating database backup"
#fi
#sudo tar cf ./backup/wp.tar data/wp
#if [ $? == 0 ]; then
#    echo "Webserver backup successful"
#else
#    echo "ERROR creating webserver backup"
#fi

echo "Removing old remote backups..."
rm -f /mnt/aragorn/backup/*
if [ $? == 0 ]; then
    echo "Removing old remote backups successful"
else
    echo "ERROR removing old remote backups"
fi

echo "Transfering backups from local to remote..."
cp ./backup/* /mnt/aragorn/backup
if [ $? == 0 ]; then
    echo "Transfering backups from local to remote successful"
else
    echo "ERROR transfering backups from local to remote"
fi

echo "Starting container..."
docker compose --file ./docker-compose.yml --project-name smart-home up --detach
if [ $? == 0 ]; then
    echo "Starting container successful"
else
    echo "ERROR starting container"
fi
