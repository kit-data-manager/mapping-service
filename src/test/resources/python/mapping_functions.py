    # -*- coding: utf-8 -*-

import json
import xmltodict
import os
import pprint


def expand_path(path):
    """
    Function to expand a path string of the type:
    'TEI.teiHeader.fileDesc.titleStmt.title'
    and to return a path list of the type:
    ['TEI', 'teiHeader', 'fileDesc', 'titleStmt', 'title']
    :param path: string of nested dictionaries separated by '.'
    :return: list of strings, where each element is a dictionary
    """
    return path.split(".")


def find_path(path, diz):
    """
    Function iteratively going deeper into a nested dictionary,
    up to a value which is not a dictionary (but could be a list).
    If no element is found, because the key is missing, the function
    exit from the loop (further nested keys will be missing too)
    and returns NONE
    :param path: path string of the type:
                'TEI.teiHeader.fileDesc.titleStmt.title
    :param diz: the dictionary to access
    :return: the value of the most nested dictionary, or None
    """
    expanded_path = expand_path(path)
    print("\nFinding element in path {}".format(expanded_path))
    for pos in expanded_path:
        if (diz is None) or (diz is ""):
            print("No or empty dictionary returned from last iteration. Skipping from position '{}'.".format(pos))
            diz = None
            break
        if isinstance(diz, dict):
            print("Accessing element '{}' in dictionary {}".format(pos, diz))
            diz = diz.get(pos)
        else:
            print(" Current element {} is no dictionary. Trying to obtain a value for type {} and position '{}'.".format(diz, type(diz).__name__, pos))
            index = 0
            if isinstance(diz, list):
                try:
                    index = int(pos)
                except ValueError:
                  print('No index found, using index 0.') 
                
                try:
                    diz = diz[index]
                except IndexError:
                    print(" Addressed index {} not found in list {}. Trying index 0.".format(index, diz))
                    if(len(diz) != 0):
                        diz = diz[0]
                    else:
                        print(" No element found in list {}. Setting value to None".format(diz))
                        diz = None
            else:
                print(" {} is not a dictionary".format(diz))
                try:
                    diz = diz.get(pos)
                except AttributeError:
                    print(" No attribute {} found.".format(pos))
    
    if diz is None:
        print("Nothing found at position {}. This entry will not be reported in the JSON.".format(pos))
    else:
        print("Element at position '{}' is {}.".format(pos, diz))
    
    # if isinstance(diz, list):
    #    print("Result {} is a list. Building string representation.".format(diz))
    #    return str(diz)
         
    # return plain diz in all other cases  
    return diz


def read_response(file):
    if os.path.splitext(file)[1].startswith(".json"):
        resp = json2dict(file)
    elif os.path.splitext(file)[1].startswith(".xml"):
        resp = xml2dict(file)
    return resp


def json2dict(file):
    """
    Function to read a json file and parse it do a dictionary
    :param file: json file
    :return: the dictionary
    """
    with open(file, "r", encoding='utf-8') as read_file:
        # encoding='utf-8' to take into account Greek letters
        d = json.load(read_file, encoding='utf-8')
    return d


def xml2dict(file):
    """
    Function to read a xml file and parse it do a dictionary
    :param file: xml file
    :return: the dictionary
    """
    with open(file, "r", encoding='utf-8') as read_file:
        # encoding='utf-8' to take into account Greek letters
        d = xmltodict.parse(read_file.read(), encoding='utf-8')
        # the output is an OrderedDict
        # to convert OrderedDict to a regular Dict:
        d = json.loads(json.dumps(d))
    return d


def dict2json(d, file):
    """
    Function to write a json file parsing the dictionary,
    ready to be indexed in Elasticsearch
    :param d: dictionary to be converted to json
    :param file: file json to be created and written
    """
    with open(file, 'w', encoding='utf-8') as j:
        # ensure_ascii=False to take into account Greek letters
        json.dump(d, j, indent=2, ensure_ascii=False)


def list2dict(flat_list):
    """
    Function taking a list of (key, value) tuples,
    where the value can be a nested path like:
    ('title.path', 'Titel')
    and transform it into a dictionary like:
    {"title":{"path":"Titel"}}
    :param flat_list: list of (key, value) tuples
    :return: the dictionary
    """
    output_dict = {}
    for key, value in flat_list:
        key_levels = key.split('.')
        current_dict = output_dict
        if len(key_levels) == 1:
            if key_levels[0] not in current_dict:
                current_dict[key_levels[0]] = value
        else:
            for level_name in key_levels[:-1]:
                if level_name not in current_dict:
                    current_dict[level_name] = {}
                current_dict = current_dict[level_name]

            current_dict[key_levels[-1]] = value

    return output_dict


def __dict2list(v, resp, prefix, append_to):
    """
    Hidden function to flatten a nested dictionary into a list of tuples.
    :param v: the dictionary with the schema
    :param resp: the dictionary with the response
    :param prefix: the prefix of the path. usually empty at the beginning
    :param append_to: the list where to append (defined in the external function)
    """
    
    # Check v for being path-type-default dictionary
    # If this is the case, use it for transformation, otherwise recurse
    if ('path' in v) and ('type' in v):
        # remove trailing dot from last recursion in order to avoid empty key
        prefix = prefix.strip(".")
        print("Element dictionary {} with prefix {}".format(v, prefix))
        value = find_path(v.get('path'), resp)
        
        if (value is None) and ('default' in v):
            print("Setting default value '{}' for path {}.".format(v.get('default'), v.get('path')))
            value = v.get('default')
       
        # skip the mapping of missing key:value pairs in the response
        if value is not None:
            casted_value = value
            if v.get('type') == 'string': 
                print("Casting value '{}' to string.".format(value))   
                casted_value = cast_to_string(value)
            elif v.get('type') == 'integer':
                print("Checking value {} for integer.".format(value))
                if isinstance(value, int) == False:
                    if 'default' in v:
                        print("Value '{}' is no integer. Applying default value '{}'.".format(value, v.get('default')))
                        casted_value = v.get('default')
                    else:
                        print("Value '{}' is no integer, no default defined. Applying value 0.".format(value))
                        casted_value = 0 
            elif v.get('type') == 'array':
                print("Transforming value {} to type array.".format(value))
                if isinstance(value, list) == True:
                    print("Value '{}' is a list, applying optional transformation.".format(value))
                    casted_value = transformList(value, v.get('include'))
                else:
                    print("Value '{}' is no list, using empty value.".format(value))
                    casted_value = "[]"
                        
            output = (prefix, casted_value)
            append_to.append(output)
    else:
        if isinstance(v, dict):
            for k, v2 in v.items():
                p2 = "{}{}.".format(prefix, k)
                __dict2list(v2, resp, p2, append_to)

        elif isinstance(v, list):
            for i, v2 in enumerate(v):
                p2 = "{}{}.".format(prefix, i)
                __dict2list(v2, resp, p2, append_to)


def transformList(list, includeList=None):
    """
    Transform the provided list by removing items whose keys
    are not listed in argument includeList. It the provided list
    does not consist of dictionary elements or if attributeList is 
    None, the argument list is returned unmodified.
    :param list: A list which should contain dictionaries.
    :param includeList: A list of strings containing item keys which should be 
    used from the provided list.
    """
    
    if(includeList == None): return list

    print("Extracting items {} from list {}.".format(includeList, list))

    result = []
    for listItem in list:
        modList = {} #the temporary dictionary holding all acceptable items
        if isinstance(listItem, dict):
            for key, value in listItem.items(): #for dictionaries iterate through all items
                if key in includeList:
                    modList[key] = value #add key and value to temporary dictionary
        else:
            modList = listItem #listItem is no dictionary, e.g. a string, just reuse it       
        result.append(modList) #append temporary dictionary (or unmodified list) to result
        
    return result
    
    
def dict2list(v, v2, prefix=''):
    """
    Function to flatten a nested dictionary into a list of tuples.
    :param v: the dictionary with the schema
    :param v2: the dictionary with the response
    :param prefix: the prefix of the path. usually empty at the beginning
    :return: the flattened list of the dictionary
    """
    dictList = []
    __dict2list(v, v2, prefix, dictList)
    return dictList


def cast_to_string(elem):
    if isinstance(elem, dict):
        # print("WARNING: Unexpected dictionary. Please check. Casting {} to string".format(elem))
        elem = str(elem)

    elif isinstance(elem, list):
        for i in range(len(elem)):
            if not isinstance(elem[i], str):
                # print("WARNING: Casting element {} of list {} to string because it was {}".format(elem[i],
                #                                                                                  elem,
                #                                                                                  type(elem[i])))
                elem[i] = str(elem[i])
    else:
        # assuming that if not a list, can only be a single element
        if not isinstance(elem, str):
            # print("WARNING: Casting {} to string because it was {}".format(elem, type(elem)))
            elem = str(elem)

    return elem


def map_response(schema_file, response_file, elastic_response_file):
    schema = json2dict(schema_file)
    response = read_response(response_file)
    flat_response = dict2list(schema["properties"], response)
    dictionary = list2dict(flat_response)
    dict2json(dictionary, elastic_response_file)
