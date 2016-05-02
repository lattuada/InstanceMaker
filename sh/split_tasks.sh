#!/bin/sh

## Copyright 2016 Eugenio Gianniti
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

input=${1:?"error: input file missing"}

if ls "$input" > /dev/null 2>&1; then
  program=/tmp/split_tasks.tmp
  cat > "$program" << 'EOF'
    index($0, task) { doPrint = 1; next }
    index($0, ":") { doPrint = 0; next }
    doPrint { print }
EOF
  dir=$(dirname "$input")
  cat "$input" | grep : | grep -v Application | tr -d : | while read taskname; do
    awk -v task="$taskname" -f "$program" < "$input" | \
      grep -v ^$ > "${dir}/${taskname}.txt"
  done
  rm "$program"
  find "$dir" -name 'Reduce*' | while read filename; do
    number="${filename##* }"
    shuffle="${dir}/Shuffle $number"
    pasted="${dir}/rs $number"
    paste "${filename}" "${shuffle}" | awk '{ print $1 + $2 }' > "${pasted}"
  done
else
  echo "error: the input file does not exist" >&2
  exit 1
fi
