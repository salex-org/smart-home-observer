# Smart Home Observer for the Homematic IP Cloud
A Spring Boot Application observing measuring values in the Homematic IP Cloud.
Records the measuring values in a database for statistical evaluation. Sends alarm
emails when the measuring values are outside the optimal range. Posts the
current measuring values on a Word Press based blog.

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
To install the observer on a Raspberry Pi first ensure, that mvn and java are available
on the target system. After that create a folder named `observer` in the home directory
of the user running the observer. Copy the scripts from `scripts` folder of this repo
into the `observer` folder.

To install a specific version of the observer software run the script `install.sh`
within the `observer` folder and add the version number as a parameter:
```shell
cd ~/oberver
./install.sh 1.0.0
```
This will install the version 1.0.0 from the maven repository in GitHub and creates
or updates the softlink `agent.jar` in the folder `observer` to reference the new version.
*Tip: You can run the script also to switch to an already installed version.*

## Starting and stopping
To start or stop the observer just do to the `observer` folder and run the `start.sh`
or the `stop.sh` script:
```shell
cd ~/observer
./start.sh
```
```shell
cd ~/observer
./stop.sh
```
The observer will create a file named `observer.pid` during startup containing the
pid of the running observer. When the observer stops, the file will be deleted.

## Logging
When the observer starts, the logs will be written into the folder `~/observer/logs`.
Every time the observer ist startet, a new log file names `current` will be created.
Old log files will be kept with the extension `.s` and a timestamp. The timestamp can
be viewed in human-readable form with the tool `tai64nlocal`:
```shell
cd ~/observer/logs
ls *.s | tai64nlocal
```

## Profiles and configuration
In production the profile `prod` will be activated by the `start.sh` script.
For local tests the profile `dev` should be used. With this the configuration
is done in different `application.yml` files:

| File                                      | Usage                                               |
|-------------------------------------------|-----------------------------------------------------|
| `src/main/resources/application.yml`      | Contains all stage unspecific configuration values. |
| `src/main/resources/application-dev.yml`  | Contains configuration values for stage `dev`.      |
| `src/main/resources/application-prod.yml` | Contains configuration values for stage `prod`.     |
| `src/test/resources/application.yml`      | Contains configuration values for stage `test`.     |
