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

### Create smart-home-user

When the software gets installed on a system for the first time, create the user:

```shell
# Create the smart-home-user with docker permissions
sudo useradd --create-home --user-group --groups docker smart-home
```

Set the environment variable `SMART_HOME_USER` in the `.bashrc` or `.zshrc` file:

```shell
# Set Smart-Home user for docker compose
export SMART_HOME_USER=$UID:$GID
```

### Checkout the scripts

Login as User `smart-home` and change to the home folder.
If installing for the first time, use `curl` and `tar` to download and extract the scripts
(replace `v2.0.0` in the example by the version you want to checkout):

```shell
curl -LO https://github.com/salex-org/smart-home-observer/releases/download/v2.0.0/scripts.tar.gz
tar xfvz scripts.tar.gz
rm scripts.tar.gz
```

If installing an update you can use a script:

```shell
./install.sh 2.0.0
```

### Login to the ghcr.io private repository with access token

This must only be once on the system.

```shell
export GHCR_TOKEN=<the token> 
echo $GHCR_TOKEN | docker login ghcr.io -u sagaert --password-stdin
```

### Pull the docker images from the repository

```shell
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