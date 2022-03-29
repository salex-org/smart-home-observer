<span class="salex_no-series-meta-information">
    <#list measurements as measurement>
    <p style="text-align: left;">${measurement.sensor.name}: <strong>${measurement.temperature?string.@temp}</strong> bei ${measurement.humidity?string.@hum}</p>
    </#list>
    <p style="text-align: left;"><span style="color: #808080;">Gemessen am ${readingTime?date} um ${readingTime?time}</span></p>
    <p style="text-align: left;"><a href="https://holzwerken.salex.org/werkstattklima">Details ansehen</a></p>
    <p style="text-align: left;"><a href="https://holzwerken.salex.org/werkstattklima-entwicklung">Jahresverlauf ansehen</a></p>
</span>