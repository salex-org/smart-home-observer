<h1>Klimawerte im kritischen Bereich</h1>
<p>Im Zeitraum von ${periodStart?datetime} bis ${periodEnd?datetime} liegen Klimawerte im kritischen Bereich!<p>
<table>
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
    <#list boundaries as boundary>
        <tr>
            <td><span style="font-size: 18px; color: ${boundary.sensor.color};">${boundary.sensor.name}</span></td>
            <td align="center">
                <span style="font-size: 18px;">${boundary.minTemp.temperature?string.@temp}</span><br />
                <span style="font-size: 10px; color: gray;">${boundary.minTemp.measuringTime?datetime}</span>
            </td>
            <td align="center">
                <span style="font-size: 18px;">${boundary.maxTemp.temperature?string.@temp}</span><br />
                <span style="font-size: 10px; color: gray;">${boundary.maxTemp.measuringTime?datetime}</span>
            </td>
            <td align="center">
                <span style="font-size: 18px;">${boundary.minHum.humidity?string.@hum}</span><br />
                <span style="font-size: 10px; color: gray;">${boundary.minHum.measuringTime?datetime}</span>
            </td>
            <td align="center">
                <span style="font-size: 18px;">${boundary.maxHum.humidity?string.@hum}</span><br />
                <span style="font-size: 10px; color: gray;">${boundary.maxHum.measuringTime?datetime}</span>
            </td>
        </tr>
    </#list>
</table>