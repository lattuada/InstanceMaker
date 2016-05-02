# Instance Maker

The code in this repository allows for creating instance files compatible
with [SPACE4Cloud webGUI](https://github.com/deib-polimi/diceH2020-space4cloud-webGUI).

All the code in the repository is licensed under
[Apache License, version 2.0](https://www.apache.org/licenses/LICENSE-2.0).

## Getting Started

In order to create instances it is necessary to parse Hive logs via
[LogParser](https://github.com/deib-polimi/LogParser) and to obtain job profiles
and task durations via [Profiler](https://github.com/deib-polimi/Profiler).
The scripts in `sh` expect `data` directories containing job profiles in the
file `profile.txt` and task durations in `tasks.txt`.
You should make sure that these scripts can be found in `PATH`.
After applying:
```sh
prepare_folders.sh directory
```
`directory` will contain the necessary files for scripts adapted from the
versions already present in `octave`.
`InstanceMaker` should be applied to the output of these latter scripts.
