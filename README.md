# org.openhab.binding.abbegon
ABB Ego-n binding for OpenHAB v1.x
Currently supports controlling of rollershutters & switches and reading sensors & device states

# build
copy __org.openhab.binding.abbegon directory to __binding__ directory of OpenHAB source code (https://github.com/openhab/openhab)

build using maven (mvn clean install)

# install
copy target file __org.openhab.binding.abbegon.jar__ to __addons__ directory of OpenHAB distribution

#usage
Add these lines to openhab.cfg and update ip, port, login and password with your real values
```
############################## ABB Ego-n Binding ###############################
#
# ABB Ego-n ip adddress
abbegon:ip=192.168.1.111

# ABB Ego-n http port (default 80)
abbegon:port=80

# ABB Ego-n local account user
abbegon:user=abb

# ABB Ego-n local account password
abbegon:password=password

# refresh interval in milliseconds (optional, default to 60000)
abbegon:refresh=10000
```

Defining items:
```
{abbegon="X"} where X is the corresponding Ego-n device id
```

#example
items file:
```
Rollershutter Roleta1 "Roleta [%d %%]" (LivingRoom) {abbegon="1", autoupdate="false"}
Switch ZarovkyPracovna "Zarovky pracovna" (FirstFloor) {abbegon="2"}
Number TempPracovna "Temperature [%.1f °C]" (FirstFloor) {abbegon="3"}
```

