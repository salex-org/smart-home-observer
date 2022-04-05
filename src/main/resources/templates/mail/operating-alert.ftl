<#if exceedances?has_content >
<h1>Betriebsparameter im Grenzbereich</h1>
<p>Die nachfolgenden Betriebsparameter liegen im Grenzbereich</p>
<table>
    <tr>
        <th><span style="font-size: 16px; color: #666666;">Zeitpunkt</span></th>
        <th><span style="font-size: 16px; color: #666666;">CPU-Temperatur</span></th>
        <th><span style="font-size: 16px; color: #666666;">Speicherauslastung</span></th>
        <th><span style="font-size: 16px; color: #666666;">Festplattenauslastung</span></th>
    </tr>
    <#list exceedances as exceedance>
    <tr>
        <td><span style="font-size: 16px; color: gray;">${exceedance.timestamp?datetime}</span></td>
        <td><span style="font-size: 16px;">${exceedance.measurement.cpuTemperature?string.@cpuTemp}</span></td>
        <td><span style="font-size: 16px;">${exceedance.measurement.memoryUsage?string.@memUsage}</span></td>
        <td><span style="font-size: 16px;">${exceedance.measurement.diskUsage?string.@memUsage}</span></td>
    </tr>
    </#list>
</table>
</#if>
<#if errors?has_content >
<h1>Aufgetretene Fehler</h1>
<p>Details zu den Fehlern sind im Fehler-Log zu finden</p>
<table>
    <tr>
        <th><span style="font-size: 16px; color: #666666;">Zeitpunkt</span></th>
        <th><span style="font-size: 16px; color: #666666;">Meldung</span></th>
        <th><span style="font-size: 16px; color: #666666;">Ursache</span></th>
    </tr>
    <#list errors as error>
        <tr>
            <td><span style="font-size: 16px; color: gray;">${error.timestamp?datetime}</span></td>
            <td><span style="font-size: 16px; color: gray;">${error.error.message}</span></td>
            <td><span style="font-size: 16px; color: gray;">${error.rootCause.message}</span></td>
        </tr>
    </#list>
</table>
</#if>