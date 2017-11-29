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

root="${1?error: missing instances directory}"
modelfile="${2?error:model JSON file missing}"

model="$(cat "$modelfile")"

if ls "$root" > /dev/null 2>&1; then
    find "$root" -name '*.json' -exec \
         sed -e "s#\"mapJobMLProfile\" : { },#\"mapJobMLProfile\" : $model,#" \
         -i.bak '{}' \;
else
    echo error: the instances directory "'$root'" does not exist >&2
    exit 1
fi
