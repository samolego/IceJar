# IceJar
Port of  [ACR](https://github.com/Rammelkast/AntiCheatReloaded) and [NCP](https://github.com/Updated-NoCheatPlus/NoCheatPlus) anticheat checks to Fabric,
with additional [GolfIV](https://github.com/samolego/GolfIV) stuff.

## Usage

Here be dragons!

## Features

* When blocking some sorts of hacks, fake info is sent to player
to make it seem like cheat worked.
* Each check can be configured in config file.

## Permissions

Bypass a check:
`icejar.checks.bypass.<check_type>`

Get reports:
`icejar.checks.get_report.<check_type>`

*Check types can be found in [CheckType file](./src/main/java/org/samo_lego/icejar/check/CheckType.java).
Make sure to use them in lower-case style.*

## License

This mod inherits the license of NoCheatPlus & AntiCheatReloaded, therefore
it's licensed under the terms of the [GPLv3](https://www.gnu.org/licenses/gpl-3.0.en.html).
