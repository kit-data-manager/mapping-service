import http.client
import os
import json
import wget
import mapping_functions
import pprint
import sys

schema = sys.argv[1] 
#schema = "simple-schema.json" 
#schema = "./sample/schema-for-json-response.json" 

input_file = sys.argv[2]
#input_file = "simple.json" 
#input_file = "./episteme/93e6f248-7636-4f7e-9e65-ed4b8a6d474b.json" 

output_filename = sys.argv[3] 
#output_filename = "output.json" 


print("\n \n \n MAPPING DOCUMENT: {} \n \n \n".format(input_file))
mapping_functions.map_response(schema, input_file, output_filename)


