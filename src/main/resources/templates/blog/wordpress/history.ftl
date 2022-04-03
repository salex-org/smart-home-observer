<h3>Zeitraum von ${periodStart?date} bis ${periodEnd?date}</h3>
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
        <td rowspan="2"><span style="font-size: 18px; color: ${measurement.sensor.color};">${measurement.sensor.name}</span></td>
        <td colspan="2" style="padding: 20px 0px">
            <a href="${measurement.tempDiagram.full}">
                <img class="aligncenter wp-image-${measurement.tempDiagram.id} size-medium" src="${measurement.tempDiagram.thumbnail}" alt="" width="${measurement.tempDiagram.thumbnailWidth}" height="${measurement.tempDiagram.thumbnailHeight}" />
            </a>
        </td>
        <td colspan="2" style="padding: 20px 0px">
            <a href="${measurement.humDiagram.full}">
                <img class="aligncenter wp-image-${measurement.humDiagram.id} size-medium" src="${measurement.humDiagram.thumbnail}" alt="" width="${measurement.humDiagram.thumbnailWidth}" height="${measurement.humDiagram.thumbnailHeight}" />
            </a>
        </td>
    </tr>
    <tr>
        <td align="center">
            <span style="font-size: 18px;">${measurement.minTemp.minimumTemperature?string.@temp}</span><br />
            <span style="font-size: 10px; color: gray;">${measurement.minTemp.day?date}</span>
        </td>
        <td align="center">
            <span style="font-size: 18px;">${measurement.maxTemp.maximumTemperature?string.@temp}</span><br />
            <span style="font-size: 10px; color: gray;">${measurement.maxTemp.day?date}</span>
        </td>
        <td align="center">
            <span style="font-size: 18px;">${measurement.minHum.minimumHumidity?string.@hum}</span><br />
            <span style="font-size: 10px; color: gray;">${measurement.minHum.day?date}</span>
        </td>
        <td align="center">
            <span style="font-size: 18px;">${measurement.maxHum.maximumHumidity?string.@hum}</span><br />
            <span style="font-size: 10px; color: gray;">${measurement.maxHum.day?date}</span>
        </td>
   </tr>
   </#list>
</table>