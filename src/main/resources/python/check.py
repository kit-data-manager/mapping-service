import platform
import json
import socket
import re 
import uuid
import json
import psutil
import logging

#schema = sys.argv[1]
#input_folder = sys.argv[2]
output_folder = sys.argv[3]

print("\n \n \n MAPPING DOCUMENT: {} \n \n \n".format(manuscript))

outpath = os.path.join(output_folder, 'info.json')

def getSystemInfo():
    try:
        info={}
        info['platform']=platform.system()
        info['platform-release']=platform.release()
        info['platform-version']=platform.version()
        info['architecture']=platform.machine()
        info['hostname']=socket.gethostname()
        info['ip-address']=socket.gethostbyname(socket.gethostname())
        info['mac-address']=':'.join(re.findall('..', '%012x' % uuid.getnode()))
        info['processor']=platform.processor()
        info['ram']=str(round(psutil.virtual_memory().total / (1024.0 **3)))+" GB"
        
        with open(outpath, 'w', encoding='utf-8') as j:
            json.dump(d, j, indent=2, ensure_ascii=False)
    except Exception as e:
        logging.exception(e)


getSystemInfo()