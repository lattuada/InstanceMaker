#!/usr/bin/python

## Copyright 2017 Marco Lattuada
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

import json
import logging
import os.path
import sys

logging.basicConfig(level=logging.INFO,format='%(levelname)s: %(message)s')

if len(sys.argv) < 3:
   logging.error("Missing arguments. fill_in_ml_per_query.sh <instance_dir> <ML_dir>")
   sys.exit(1)

instance_directory = sys.argv[1]
ml_directory = sys.argv[2]

#Check that instance directory exists
if not os.path.exists(instance_directory):
   logging.error("Instance directory " + instance_directory + " does not exist")
   sys.exit(1)

#Check that ML directory exists
if not os.path.exists(ml_directory):
   logging.error("ML directory " + ml_directory + " does not exist")
   sys.exit(1)

#Load all ML models
logging.info("Looking for ML models")
ML_models = {}
for sub in os.listdir(ml_directory):
   logging.info("Looking into " + str(os.path.join(ml_directory, sub)))
   if os.path.isdir(os.path.join(ml_directory, sub)):
      #Look for .json file
      ML_json_file_name = os.path.join(ml_directory, sub, "model.json")
      logging.info("Looking for " + ML_json_file_name)
      if os.path.exists(ML_json_file_name):
         ML_json_file = open(ML_json_file_name)
         ML_json = json.load(ML_json_file)
         if not sub in ML_json:
            logging.error("ML json " + str(ML_json_file) + " does not application " + str(sub))
            sys.exit(1)
         ML_models[sub] = ML_json[sub]

#For each directory (instance)   
for sub in os.listdir(instance_directory):
   if os.path.isdir(os.path.join(instance_directory, sub)):
      #Look for .json file
      for in_file in os.listdir(os.path.join(instance_directory, sub)):
         if os.path.splitext(in_file)[1] == ".json":
            instance_json_file_name = os.path.join(instance_directory, sub, in_file)
            instance_json_file = open(instance_json_file_name)
            logging.info("Opening " + str(instance_json_file_name)) 
            instance_json = json.load(instance_json_file)
            if not "mapClassParameters" in instance_json:
               logging.error("Instance json " + str(instance_json_file_name) + " does not contain mapClassParameters")
               sys.exit(1)
            if not "mapJobMLProfile" in instance_json:
               logging.error("Instance json " + str(instance_json_file_name) + " does not contain mapJobMLProfile")
               sys.exit(1)
            #Look for applications
            applications = instance_json["mapClassParameters"]
            for application, classParameters in applications.items():
               if not application in ML_models:
                  logging.error("ML model of " + application + " not found")
                  sys.exit(1)
               instance_json["mapJobMLProfile"][application] = ML_models[application]
               #Close file
               instance_json_file.close()

               #Reopen file for write it
               instance_json_file = open(instance_json_file_name, "w")
               
               #Write back modified json
               json.dump(instance_json, instance_json_file, indent=3)
               instance_json_file.close()
               


