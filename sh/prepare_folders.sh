#!/bin/sh

## Copyright 2016-2017 Eugenio Gianniti
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

root=${1:?"error: input directory missing"}

if ls "$root" > /dev/null 2>&1; then
    find "$root" -name tasks.txt | while IFS= read -r filename; do
        split_tasks.sh "$filename"
    done
    find "$root" -name profile.txt | while IFS= read -r filename; do
        dir="$(dirname "$filename")"
        cat "$filename" | grep tasks | awk -F : '{ print $2 }' > "$dir"/numTasks.txt
    done
else
    echo "error: the input directory does not exist" >&2
    exit 1
fi
