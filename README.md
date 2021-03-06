# IceJar
Port of some [ACR](https://github.com/Rammelkast/AntiCheatReloaded) and [NCP](https://github.com/Updated-NoCheatPlus/NoCheatPlus) anticheat checks to Fabric,
with additional [GolfIV](https://github.com/samolego/GolfIV) & other stuff.

## Download

IceJar has a long way to go and release as stable.
You can however play with it and test it at your own risk.
Download the latest version from [CircleCI](https://app.circleci.com/pipelines/github/samolego/IceJar)
(navigate to latest build and click the "artifacts" tab).

**Please report false positives for checks that cannot be configured.**

Also, currently there's no special mod compatibility system, so please avoid
reporting false positives that are a result of mod incompatibility.

Thanks :)

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
