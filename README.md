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
