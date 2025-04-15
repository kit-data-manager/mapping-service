import sys
import platform
import os
import json
import logging

# input folder and mapping file are ignored
output_folder = sys.argv[3]
# output path obtained from mapping service
outpath = os.path.join(output_folder, 'info.json')


def getSystemInfo():
    try:
        # Python code execution
        info = {'version': sys.version, 'platform-release': platform.release(), 'platform-version': platform.version(),
                'architecture': platform.machine(), 'processor': platform.processor()}
        # output writing
        with open(outpath, 'w', encoding='utf-8') as j:
            json.dump(info, j, indent=2, ensure_ascii=False)
    except Exception as e:
        # exception logging for later checks and exit code != 0 to propagate error and cause
        logging.exception(e)
        exit(1)


getSystemInfo()
