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

When the software gets installed on a system for the first time, create the users:

```shell
# Create the main user with docker permissions
sudo useradd --create-home --user-group --groups docker smart-home

# Create additional users for the services
sudo useradd --shell /usr/sbin/nologin --no-create-home --home-dir /home/smart-home/observer --no-user-group --gid smart-home observer
sudo useradd --shell /usr/sbin/nologin --no-create-home --home-dir /home/smart-home/mosquitto --no-user-group --gid smart-home mosquitto
sudo useradd --shell /usr/sbin/nologin --no-create-home --home-dir /home/smart-home/influx --no-user-group --gid smart-home influx
sudo useradd --shell /usr/sbin/nologin --no-create-home --home-dir /home/smart-home/grafana --no-user-group --gid smart-home grafana
```

Checkout the scripts, login to the container registry and pull the docker images

```shell
# Checkout the scripts (example for v2.0.0):
curl -LO https://github.com/salex-org/smart-home-observer/releases/download/v2.0.0/scripts.tar.gz
mkdir observer
tar xfvz scripts.tar.gz -C observer
rm scripts.tar.gz

# Login to the ghcr.io private repository with access token:
echo $GHCR_TOKEN | docker login ghcr.io -u sagaert --password-stdin

# Pull the docker images from the repository:
./init.sh
```

## Starting and stopping
Set the following variables for the users the container are run with.
You may add this to your `.bachrc` or `.zshrc` file.

```shell
# Set Smart-Home user for docker compose
export SMART_HOME_USER_OBSERVER=$(id -u observer):$(id -g smart-home)
export SMART_HOME_USER_MOSQUITTO=$(id -u mosquitto):$(id -g smart-home)
export SMART_HOME_USER_INFLUX=$(id -u influx):$(id -g smart-home)
export SMART_HOME_USER_GRAFANA=$(id -u grafana):$(id -g smart-home)
```


For starting and stopping the container, run the following commands:

```shell
# Start the container:
./start.sh

# Stop the container:
./stop.sh
```

Enter the encryption key for the configuration after starting the observer container.
After entering the key disconnect with `CTRL-p CTRL-q`. **Do not use `CTRL-c` because this stops the continer!**

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

The Log is managed by docker. Use the following command to see the Log:

```shell
docker logs smart-home-observer-1
```

## Profiles and configuration

The configuration can be found in `observer/config/observer-config.yml` and
is encrypted. To temporarily encrypt the configuration and (re-)encrypt it
again user the following commands:

```shell
# Decrypt the configuration
./decrypt-config.sh

# Encrypt the configuration
./encrypt-config.sh
```

**Ensure to remove the decrypted configuration after encryption and
do not copy oder commit it to another location!**