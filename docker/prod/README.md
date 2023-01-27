# Smart-Home-Observer installation
Configuration for running a Smart-Home-Observer installation with dependencies.

## Installation
After checkout this repo, run the following command once:

```shell
# Log into the ghcr.io private repository:
echo $GHCR_TOKEN | docker login ghcr.io -u sagaert --password-stdin

# Pull the docker images from the repository:
./init.sh
```

## Starting and stopping
For starting and stopping the container, run the following commands:

```shell
# Start the container:
./start.sh

# Stop the container:
./stop.sh
```

## Backup and restore
Backup and restore of the data can be done by the following commands:

```shell
# Backup database content
./backup.sh

# Delete data and replace it by the content auf the backup tars:
./restore.sh
```

The script `backup.sh` first stops the containers. Then the data of the database is tared into
the folder `backup`. The tar files will then be copied to the NAS system mounted on `/mnt/aragorn/backup`. After
that, the containers will be started again.

The script `restore.sh` first stops the containers and then deletes all content in the folder `data` and un-tars
the backup files located in the folder `backup`. After that, the containers will be started again.