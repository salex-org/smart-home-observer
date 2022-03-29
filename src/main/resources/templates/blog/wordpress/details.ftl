<h3>Zeitraum von ${periodStart?datetime} bis ${periodEnd?datetime}</h3>
<img class="aligncenter size-full wp-image-${diagramId}" src="${diagramFull}" alt="" width="600" height="300" />
<table border="0">
    <tr>
        <th rowspan="2" width="80px">&nbsp;</th>
        <th colspan="2" style="border-bottom-width: 2px; border-bottom-color: #999999; border-bottom-style: solid;"><span style="font-size: 18px; color: #666666;">Temperatur</span></th>
        <th colspan="2" style="border-bottom-width: 2px; border-bottom-color: #999999; border-bottom-style: dashed;"><span style="font-size: 18px; color: #666666;">Luftfeuchtigkeit</span></th>
    </tr>
    <tr>
        <th width="120px"><span style="font-size: 18px; color: #666666;">Minimum</span></th>
        <th width="120px"><span style="font-size: 18px; color: #666666;">Maximum</span></th>
        <th width="120px"><span style="font-size: 18px; color: #666666;">Minimum</span></th>
        <th width="120px"><span style="font-size: 18px; color: #666666;">Maximum</span></th>
    </tr>
    <#list measurements as measurement>
    <tr>
        <td><span style="font-size: 18px; color: ${measurement.color};">${measurement.sensor.name}<span></td>
        <td align="center">
            <span style="font-size: 18px;">${measurement.minTemp.temperature?string.@temp}</span><br />
            <span style="font-size: 10px; color: gray;">${measurement.minTemp.measuringTime?datetime}</span>
        </td>
        <td align="center">
            <span style="font-size: 18px;">${measurement.maxTemp.temperature?string.@temp}</span><br />
            <span style="font-size: 10px; color: gray;">${measurement.maxTemp.measuringTime?datetime}</span>
        </td>
        <td align="center">
            <span style="font-size: 18px;">${measurement.minHum.humidity?string.@hum}</span><br />
            <span style="font-size: 10px; color: gray;">${measurement.minHum.measuringTime?datetime}</span>
        </td>
        <td align="center">
            <span style="font-size: 18px;">${measurement.maxHum.humidity?string.@hum}</span><br />
            <span style="font-size: 10px; color: gray;">${measurement.maxHum.measuringTime?datetime}</span>
        </td>
    </tr>
    </#list>
</table>
