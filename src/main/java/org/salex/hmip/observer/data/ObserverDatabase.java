package org.salex.hmip.observer.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Transactional
@Repository
public class ObserverDatabase {
    private static final Logger LOG = LoggerFactory.getLogger(ObserverDatabase.class);

    private final JdbcTemplate jdbcTemplate;

    private List<Sensor> sensors;

    public ObserverDatabase(JdbcTemplate jdbcTemplate) throws SQLException {
        this.jdbcTemplate = jdbcTemplate;

        if(tableExists("sensors")) {
            LOG.info("Table sensors already exists");
        } else {
            LOG.info("Creating table sensors");
            this.jdbcTemplate.execute("create table sensors(id int not null, name varchar(32) not null, kind varchar(32) not null, sgtin varchar(32), primary key (id))");
            LOG.info("Initializing sensor data");
            this.jdbcTemplate.execute("insert into sensors(id, name, kind, sgtin) values (1, 'Maschinenraum', 'HmIP_STHO', '3014-F711-A000-0EDD-89B3-A015')");
            this.jdbcTemplate.execute("insert into sensors(id, name, kind, sgtin) values (2, 'Bankraum', 'HmIP_STHO', '3014-F711-A000-0EDD-89B3-A112')");
            this.jdbcTemplate.execute("insert into sensors(id, name, kind, sgtin) values (3, 'Carport', 'HmIP_STHO', '3014-F711-A000-10DD-899E-53A0')");
        }

        if(tableExists("readings")) {
            LOG.info("Table readings already exists");
        } else {
            LOG.info("Creating table readings");
            this.jdbcTemplate.execute("create table readings(id int not null primary key generated always as identity (start with 1, increment by 1), reading_time timestamp not null)");
        }

        if(tableExists("climate")) {
            LOG.info("Table climate already exists");
        } else {
            LOG.info("Creating table climate");
            this.jdbcTemplate.execute("create table climate(reading int not null, sensor int not null, measuring_time timestamp not null, temperature double, humidity double, vapor_amount double, wind_speed double, wind_direction double, brightness double, rainfall double, primary key(reading, sensor), foreign key (reading) references readings(id), foreign key (sensor) references sensors(id) )");
        }

        if(tableExists("operating")) {
            LOG.info("Table operating already exists");
        } else {
            LOG.info("Creating table operating");
            this.jdbcTemplate.execute("create table operating(reading int not null, cpu_temperature double, core_voltage double, disk_usage double, memory_usage double, primary key(reading), foreign key (reading) references readings(id) )");
        }

        if(tableExists("consumption")) {
            LOG.info("Table consumption already exists");
        } else {
            LOG.info("Creating table consumption");
            this.jdbcTemplate.execute("create table consumption(reading int not null, sensor int not null, measuring_time timestamp not null, power double, primary key(reading, sensor), foreign key (reading) references readings(id), foreign key (sensor) references sensors(id) )");
        }

        LOG.info("Loading sensor data");
        this.sensors = loadSensors();

        LOG.info("Database ready to rumble!");
    }

    private boolean tableExists(String name) throws SQLException {
        DatabaseMetaData meta = this.jdbcTemplate.getDataSource().getConnection().getMetaData();
        return meta.getTables(null, null, name.toUpperCase(), null).next();
    }

    private List<Sensor> loadSensors() {
        return this.jdbcTemplate.query("select * from sensors order by id", (row, rowNum) ->
            new Sensor(row.getInt("id"), row.getString("name"), row.getString("kind"), row.getString("sgtin"))
        );
    }

    private Sensor getSensor(List<Sensor> sensors, int sid) {
        for(Sensor sensor : sensors) {
            if(sensor.getId() == sid) {
                return sensor;
            }
        }
        return null;
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    public Reading addReading(Reading reading) {
        // TODO implement
        return reading;
    }

    public List<Reading> getReadings(int hours) {
        // TODO implement
        return List.of();
    }

    public List<Reading> getReadings(int hours, Sensor sensor) {
        // TODO implement
        return List.of();
    }

    public Map<Sensor, List<ClimateMeasurementBoundaries>> getClimateMeasurementBoundaries(int days) {
        // TODO implement
        return Map.of();
    }

    public List<ClimateMeasurementBoundaries> getClimateMeasurementBoundaries(int days, Sensor sensor) {
        // TODO implement
        return List.of();
    }
}
