# scriptella-http
An HTTP connection driver for Scriptella. 

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/nz.ac.auckland.scriptella.driver/scriptella-http/badge.svg)](https://maven-badges.herokuapp.com/maven-central/nz.ac.auckland.scriptella.driver.scriptella-http/scriptella-http)

This driver was reverse-engineered from the source code of other Scriptella drivers, notably the CSV driver. 
As such, the structure may be a little odd or not as expected from an official driver. 

# Usage

The driver can be used by adding the .jar to the lib folder in your scriptella install directory (link coming soon).
Example usage:

```
  <!DOCTYPE etl SYSTEM "http://scriptella.javaforge.com/dtd/etl.dtd">
<etl>
    <connection id="http" driver="nz.ac.auckland.scriptella.driver.http.Driver" url="http://127.0.0.1:8080">
        type=post
        format=string
        timeout=600
    </connection>
    <connection id="csv" driver="csv" url="IntegrationTest.csv"/>

    <query connection-id="csv">
        <!-- Empty query means select all columns -->
        <script connection-id="http">
            abc=$One
            def=$Two
            ghi=$Three

        </script>
    </query>
</etl>
```

Values for type: GET(default), POST, PUT.

Values for format: String(default), JSON.

Timeout is in ms. default value is 0(infinite).

The driver can only be used in a script tag, queries are not implemented. Variables are generally specified using the dollar sign ($),
but can be used without it. The variables here represent the names of the headers in the CSV file.
