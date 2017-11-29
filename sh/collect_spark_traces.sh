#!/bin/sh

## Copyright 2017 Eugenio Gianniti
##
## Licensed under the Apache License, Version 2.0 (the "License");
## you may not use this file except in compliance with the License.
## You may obtain a copy of the License at
##
##     http://www.apache.org/licenses/LICENSE-2.0
##
## Unless required by applicable law or agreed to in writing, software
## distributed under the License is distributed on an "AS IS" BASIS,
## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
## See the License for the specific language governing permissions and
## limitations under the License.

root="${1?error: missing input directory}"
vm="${2?error: missing VM type}"

if ls "$root" > /dev/null 2>&1; then
  find "$root" -name summary.csv | while IFS= read -r filename; do
    dir="$(dirname "$filename")"
    outdir="${dir#$root}"
    outdir="${outdir#/}/$vm"
    mkdir -p "$outdir"
    cp "$filename" "$outdir"
  done

  find "$root" -name '*.txt' -or -name '*.lua.template' | grep /empirical/ | while IFS= read -r filename; do
    dir="$(dirname "$filename")"
    upperdir="$(dirname "$dir")"
    outdir="${upperdir#$root}"
    outdir="${outdir#/}/$vm"
    base="$(basename "$filename")"
    base="${base%.template}"
    cp "$filename" "$outdir/$base"
  done
else
  echo error: the input directory "'$root'" does not exist >&2
  exit 1
fi
