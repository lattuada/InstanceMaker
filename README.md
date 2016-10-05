# Instance Maker

The code in this repository allows for creating instance files compatible
with [SPACE4Cloud webGUI](https://github.com/deib-polimi/diceH2020-space4cloud-webGUI).

All the code in the repository is licensed under
[Apache License, version 2.0](https://www.apache.org/licenses/LICENSE-2.0).

## Getting Started

In order to create instances it is necessary to parse Hive logs via
[LogParser](https://github.com/deib-polimi/LogParser) and to obtain job profiles
and task durations via [Profiler](https://github.com/deib-polimi/Profiler).
The scripts in `sh` expect a directory tree containing job profiles 
(`Profiler -p ...`) in the files `profile.txt` and task durations
(`Profiler -t ...`) in `tasks.txt`.
You should make sure that these scripts can be found in `PATH`.

The first step in obtaining instances is to further process the
`profile.txt` and `tasks.txt` files with:
```sh
prepare_folders.sh directory
```
where `directory` is the root of the above mentioned directory tree.
After running this script, the directory tree will contain files ready
for creating traces.

The `octave` folder contains several specific scripts and a general
purpose function, `prepare_traces.m`.
At an Octave prompt, after adding the `octave` folder to Octave's path,
you should type:
```octave
prepare_traces ("directory", "vm", true)
```
Such an invocation will take the previously processed files from the
directory tree and create traces for use with `InstanceMaker`,
also adding `vm` as virtual machine label.
The third argument is a boolean value: if it is `true`, the function
will randomly shuffle task durations in the output task lists.
`InstanceMaker` should be applied to the obtained traces.
