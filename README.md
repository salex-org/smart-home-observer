# Smart Home Observer
A GoLang application reading data from the HomematicIP Cloud and writing it to a bucket in an InfluxDB.

## Local Testing

For local testing it is possible to start  an instanc of InfluxDB together with the
current code of the observer by using Docker.
The following commands can be used to start and stop the container:

```shell
# Start the container:
make docker-run

# Stop the container:
make docker-stop
```
