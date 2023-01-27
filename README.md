# Smart Home Observer
A Golang Application managing consumption data in an InfluxDB for visualization
in a Grafan Dashboard. The data will be sent by [Smart Home Agents](https://github.com/salex-org/smart-home-agent)
via MQTT.

## Local Testing

For local testing it is possible to start instances of Mosquitto, InfluxDB and Grafana by using Docker.
The following commands can be used to start and stop the container:

```shell
# Start the container:
make local-bootstrap

# Stop the container:
make local-bootstrap
```

All services will initially be created with a user named `local` using `localsecret` as the password.

To open a CLI for the InfluxDB, you can open a shell in the container

```shell
docker exec -it 
```

## Installation

TODO: Write documentation

## Starting and stopping

TODO: Write documentation

## Logging

TODO: Write documentation

## Profiles and configuration

TODO: Write documentation
