# Smart Home Observer
A Golang Application managing consumption data in an InfluxDB for visualization
in a Grafan Dashboard. The data will be sent by [Smart Home Agents](https://github.com/salex-org/smart-home-agent)
via MQTT.

## Local Testing

For local testing it is possible to start instances of Mosquitto, InfluxDB and Grafana together with the
current code of the observer by using Docker.
The following commands can be used to start and stop the container:

```shell
# Start the container:
make docker-run

# Stop the container:
make docker-stop
```

All services will initially be created with a user named `local` using `localsecret` as the password.

After the start the observer needs the key for the configuration. To enter the key attach to the container
and enter it. After entering the key disconnect with `CTRL-p CTRL-q`. **Do not use `CTRL-c` because this stops the continer!**

```shell
docker attach local-sho-observer-1
```

To open a CLI for the InfluxDB, you can open a shell in the container

```shell
docker exec -it 
```

## Installation
Checkout the scripts, login to the container registry and pull the docker images

```shell
# Checkout the scripts (example for v2.0.0):
curl https://github.com/salex-org/smart-home-observer/releases/download/v2.0.0/scripts.tar.gz
tar xfvz scripts.tar.gz

# Login to the ghcr.io private repository with access token:
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

## Logging

TODO: Write documentation

## Profiles and configuration

TODO: Write documentation
