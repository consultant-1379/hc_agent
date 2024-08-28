#!/usr/bin/env python3

import yaml
import sys
import os
import datetime
from subprocess import call
from argparse import ArgumentParser
from argparse import RawTextHelpFormatter
from shutil import copyfile
import textwrap
from dpath import util as dpath_util
from createXlsx import generateXlsx
from createCSV import generateCSV


#THE TAGS IN THE FOSS FILES CAN NOT BE EMPTY
#IF ManualSpecificFOSSEvaluation VALUE IS YES THEN DE DEFAULT VALUES FROM THE defaultTemplate.yaml ARE NOT INSERTED IN
#THE FOSS REPORT FOR THIS DEPENDENCY

#DEFINE GLOBAL VARIABLES
HOME = os.path.abspath(os.path.join(os.path.dirname(__file__), '..')) +"/"
print("HOME: " + HOME)
REPORT_FILE_TEMPLATE= HOME+"templates/defaultTemplate.yaml"
FILE_LIST_TEMPLATE= HOME+"templates/fileListTemplate.yaml"
SC_REPORT_NAME="Maven_Report/"
MVN_FILE="maven-sc.yaml"				#MAVEN DEPENDENCY FILE

fossFilesDir="" 		#SC FOSS FILES DIRECTORY
fossFilesDirSC=""    #FOSS FILES DIRECTORY

createXlsx=False
createCSV=False

LicenseObligationText="Check Free and Open Source Software license agreement for license obligations."
licensesDict = {"MIT License": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"Apache License v 2.0": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"Simplified BSD License": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"New BSD, BSD License": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"Eclipse Public License (EPL)": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"Eclipse Public License version 2.0": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"Lesser General Public License v 3.0 (LGPL)": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"LGPL3, GNU Lesser General Public License 3": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"MPL 2.0 / Mozilla Public License v 1.1 (MPL)": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"LGPL2.1, GNU Lesser General Public License 2.1": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"CDDL 1.0": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"CDDL 1.1": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"GNU General Public License with classpath except": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"GPL2 with classpath exception": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"GPL3, GNU General Public License 3.0": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"GPL2, GNU General Public License 2.0": "{{ChoiceOfLicense}}. "+LicenseObligationText}


def checkArgs():
#CHECKING THE ARGUMENTS
    global fossFilesDirSC, createXlsx, createCSV

    parser = ArgumentParser(
        # description='This script generates the FOSS report from a set of FOSS files and from the Maven dependency lists. '
        #             'Foss files directory, Maven dependency files must be provided as input to the script. '
        #             'Creation of the maven dependency list can be initiated via -dev for the '
        #             'project hc_agent. '
        #             'The Specific FOSS Evaluation .xlsx file can be created via the -xlsx option.',
        #             'The Specific FOSS Evaluation .csv file can be created via the -csv option.',

        description='This script generates the FOSS report from a set of FOSS files for hc_agent. '
                    'The script stored the report and the generated '
                    'xlsx files (if -xlsx is present) into a directory.'
                    'csv files (if -csv is present) into a directory.',
        epilog=textwrap.dedent('''\
                Usage examples: 
                Report.py -f fossFiles/ -dev /local/username/hc_agent -m dev_maven.yaml -xlsx '''),
        formatter_class=RawTextHelpFormatter)

    parser.add_argument("-fsc", dest="foss_Files_Dir_SC", help="Directory name of the SC FOSS files.", required=False)

    parser.add_argument("-xlsx", help="Create Specific FOSS Evaluation .xlsx files from the Report", required=False,action='store_true')

    parser.add_argument("-csv", help="Create Specific FOSS Evaluation .csv files from the Report for Mimer", required=False,action='store_true')
    
    if parser.parse_args().foss_Files_Dir_SC == None:
        parser.print_help()
        sys.exit(1)

    if parser.parse_args().foss_Files_Dir_SC != None:
        fossFilesDirSC = parser.parse_args().foss_Files_Dir_SC

    if parser.parse_args().xlsx:
        createXlsx=True
        
    if parser.parse_args().csv:
        createCSV=True 

def replaceTags(value,fossObj):

    for tag in fossObj:
        if (isinstance(value, (str))):

            if "{{"+tag+"}}.strip()" in value:
                value = value.replace("{{" + tag + "}}.strip()", str(fossObj[tag]).split("/")[1])
                value = value.replace("{{" + tag + "}}", str(fossObj[tag]))

            else:
                value = value.replace("{{" + tag + "}}", str(fossObj[tag]))
                # print(value, tag,fossObj[tag])

    return value

def updateBrackets(fossObj):
    #UPDATE THE BRACKETS WITH THE CORRESPONDING VALUES
    for tag in fossObj:
        
        try:

            if (isinstance(fossObj[tag], (list))):  # EXCLUDING WHEN THE VALUE IS YES/NO
                for i in range(0, len(fossObj[tag])):  # MAVEN DEPENDENCY FIELD HAS IS A LIST
                    fossObj[tag][i]=replaceTags(fossObj[tag][i],fossObj)            #REPLACE THE BRACKET FIELD WITH VALUE

            else: #ALL TAGS EXCEPT MavenDependency
                fossObj[tag] = replaceTags(fossObj[tag], fossObj)

        except IOError as e:
            print("Failed updating the double brackets for " +tag + " in "+fossObj+". Exiting...")
            print(fossObj['FOSSName'], fossObj['FOSSVersion'],fossObj['PRIMNumber(CAX/CTX)'],fossObj['ChoiceOfLicense'])
            print("" % e)
            sys.exit()

    return fossObj

def checkFiles():

    if not os.path.isfile(REPORT_FILE_TEMPLATE):
        print("The file report template" + REPORT_FILE_TEMPLATE + " is not found. Exiting...")
        sys.exit()

    if not os.path.isfile(MVN_FILE) and MVN_FILE:
        print("The file mvn file" + MVN_FILE + " is not found. Exiting...")
        sys.exit()

    if not os.path.isfile(FILE_LIST_TEMPLATE):
        print("The file file list template" + FILE_LIST_TEMPLATE + " is not found. Exiting...")
        sys.exit()

def createReport(obj,fileListObj, reportPath):
    
    reportFileName = "ReportFoss.yaml"
    reportFileList = "ReportFileList.yaml"

# COPY FILES USED AS INPUT TO REPORT FOLDER    
    if MVN_FILE:
        copyfile(MVN_FILE, reportPath+os.path.basename(MVN_FILE))

    with open(reportPath+reportFileName, 'w') as ymlfile:             #WRITE TO FOSS REPORT
        ymlfile.write(yaml.dump(obj, default_flow_style=False))
    ymlfile.close()

    with open(reportPath+reportFileList, 'w') as ymlfile:             #WRITE TO FOSS FILE LIST
        ymlfile.write(yaml.dump(fileListObj, default_flow_style=False))
    ymlfile.close()

#MATCH THE ChoiceOfLicense AND RETURN THE CORRESPONDING FulfillmentOfLicenseObligations
def getLicenseObligation(ChoiceOfLicense):

    for license in licensesDict:

        if ChoiceOfLicense==license:
            return licensesDict.get(license)
    return "None"

#CREATING INDEX FOR LIST WITH DEPENDENCY NAME AND FOSS FILE NAME FOR THE FOSSES WITH MAVENDEPENDENCY TAG CONTAINING A LIST
#FOR THE FOSSES WITH MAVENDEPENDENCY SET TO NO THE FOSSURL AND FOSS FILE NAME ARE INSERTED INTO THE INDEX
def createFossIndex(fossFilesDir):
    try:
        index={}
        for fossFile in os.listdir(fossFilesDir):
            with open(fossFilesDir + "/" + fossFile, 'r') as fossFileID:
                fossFileObj = yaml.safe_load(fossFileID)  # READ EACH FOSS FILE INTO A YAML OBJECT
            fossFileID.close()

            fossFileObj['FOSS']=updateBrackets(fossFileObj['FOSS'])

            if (isinstance(fossFileObj['FOSS']['MavenDependency'], (list))):  # EXCLUDING WHEN THE VALUE IS YES/NO
                for fossDependency in fossFileObj['FOSS']['MavenDependency']:
                    index.update({fossDependency: fossFile})
            elif str(fossFileObj['FOSS']['MavenDependency']).lower()== "no" or (not fossFileObj['FOSS']['MavenDependency']):
                index.update({fossFileObj['FOSS']['FOSSName']+":"+str(fossFileObj['FOSS']['FOSSVersion']) : fossFile})
                index.update({fossFileObj['FOSS']['FOSSURL']: fossFile})
            else:
                raise Exception('Failed to create Foss file index. check '+ fossFile+'. Exiting...')

    except IOError as e:
        print("Failed to create Foss file index. Exiting..." % e);
        sys.exit()

    return index

#CHECK FOR EMPTY TAGS WITHING A DICT, THE MavenDependency TAG IS EXCLUDED
def checkFossTags(fossFileObj):
    for tag in fossFileObj:

        if (not str(fossFileObj[tag])) or (str(fossFileObj[tag]) == "None"):
            print("")
            print("The tag ",tag," is empty... Exiting")
            return 1
    return 0

def readFile(fileName):
    try:
        with open(fileName, 'r') as fileToOpen:
            fileObject = yaml.safe_load(fileToOpen)
        fileToOpen.close()
    except IOError as e:
        print("Couldn't open and read file (%s)." % e)
        exit(1)

    return fileObject

def generateReportFileObj(dependencyList, fossFilesIndex, matchedFosslist, fossFilesDir):
    
    dependencyCounter=0
    numberOfMatchedDependencies=0
    undefinedDependencyList=[]		#KEEPING HTE UNDEFINED DEPENDENCIES

    defaultFossFile = readFile(REPORT_FILE_TEMPLATE)

    reportFileObj = readFile(REPORT_FILE_TEMPLATE)
    reportFileObj['FOSS'] = {}				#SETTING AS DICT
    reportFileObj['Undefined'] = []			#SETTING AS LIST

    print("Searching the Dependencies in FOSS files... \n")
    for dependency in dependencyList:  # FOR EVERY MVN DEPENDENCY IN THE DEPENDENCY LIST SEARCH FOR MATCHING
                                        #DEPENDENCIES IN THE FOSS FILES INDEX. IF MATCHED FOUND THEN ADD TO THE FOSS
                                        # REPORT FILE UNDER FOSS TAG ELSE ADD UNDER UNDEFINED TAG.

        dependencyCounter+=1
        sys.stdout.write("\r")
        sys.stdout.write("Dependency %d of %d: %s \r" % (dependencyCounter,len(dependencyList),dependency))
        sys.stdout.flush()

        matched=False				# THE CURRENT DEPENDECY IS NOT YET MATCHED IN DEPENDENCY LIST

        for fossFilesDependency in fossFilesIndex:					#SEARCH THE FOSS FILE INDEX FOR THE CURRENT DEPENDENCY
            fossFileName=fossFilesIndex.get(fossFilesDependency)

            if str(dependency).lower() == str(fossFilesDependency).lower():  # IS THE CURRENT DEPENDENCY IN THE FOSS FILE INDEX
                matched = True
                matchedFossFile = fossFileName
                break

        if matched: 												#THE CURRENT DEPENDENCY WAS FOUND IN THE FOSS FILE INDEX

            numberOfMatchedDependencies+=1			#INCREASE THE COUNTER OF MATCHED DEPENDENCIES

            fossFileExists=False
            for i in matchedFosslist:									#CHECKING IF THIS MATCHED FOSS HAS BEEN ALREADY FOUND
                if i == matchedFossFile:
                    fossFileExists = True

            if 	not fossFileExists:										#FOSS FILENAME IS NOT IN THE MATCHED FOSS LIST HENCE WILL BE INSERTED IN THE REPORT

                matchedFosslist.append(matchedFossFile)					# PUTTING FOSS FILENAME IN THE MATCHED FOSS LIST

                fossFileObj=readFile(fossFilesDir + "/" + matchedFossFile)

                matchedFoss = updateBrackets(fossFileObj['FOSS'])

                #UPDATE THE FOSS REPORT OBJECT WITH THE CURRENT DEPENDECY
                reportFileObj['FOSS'][matchedFoss['PRIMNumber(CAX/CTX)']]=matchedFoss

                #MANUAL EVALUATION FOR THIS FOSS, THE DEFAULT FOSS VALUES WILL NOT BE INSERTED INTO THE REPORT FOR THIS DEPENDENCY
                #ONLY VALUES FROM THE FOSS FILE WILL BE ADDED INTO THE FOSS REPORT FOR THIS DEPENDENCY
                if not (((str(matchedFoss['ManualSpecificFOSSEvaluation']).lower())== "yes") or (matchedFoss['ManualSpecificFOSSEvaluation'])):    # IF IS YES OR TRUE
                    reportFileObj['FOSS'][matchedFoss['PRIMNumber(CAX/CTX)']].update(defaultFossFile['FOSS'])
                    reportFileObj['FOSS'][matchedFoss['PRIMNumber(CAX/CTX)']]['FulfillmentOfLicenseObligations']=getLicenseObligation(reportFileObj['FOSS'][matchedFoss['PRIMNumber(CAX/CTX)']]['ChoiceOfLicense'])

                #UPDATE THE BRACKETS FOR THE CURRENT FOSS
                reportFileObj['FOSS'][matchedFoss['PRIMNumber(CAX/CTX)']]=updateBrackets(reportFileObj['FOSS'][matchedFoss['PRIMNumber(CAX/CTX)']])

        else:														#THE DEPENDENCY WAS NOT FOUND IN FOSS FILES HENCE ADDING AS UNDEFINED IN THE FOSS REPORT
            undefinedExists=False
            for i in undefinedDependencyList:
                if i == dependency:
                    undefinedExists=True
            if not undefinedExists:
                undefinedDependencyList.append(dependency)

                reportFileObj['Undefined'].append(dependency)

    reportFileObj['Description'] = "Foss Report"
    reportFileObj['CreationDate']=datetime.datetime.now().strftime("%Y-%m-%d")+datetime.datetime.now().strftime("_%H:%M")
    
    return reportFileObj, numberOfMatchedDependencies, undefinedDependencyList;

def generateReportLstObj(matchedFosslist, unusedFossFiles):
    
    fileListObj=readFile(FILE_LIST_TEMPLATE)
    fileListObj['UsedFiles'] = []				#SETTING AS DICT
    fileListObj['UnusedFiles'] = []			#SETTING AS LIST

    #PREPARE FILES LIST REPORT TAGS
    fileListObj['Description']="Used and Unused FOSS files for generating the report"
    fileListObj['CreationDate']=datetime.datetime.now().strftime("%Y-%m-%d")+datetime.datetime.now().strftime("_%H:%M")

    fileListObj['UsedFiles']=sorted(matchedFosslist)
    fileListObj['UnusedFiles'] = sorted(unusedFossFiles)

    return fileListObj, matchedFosslist, unusedFossFiles

def main():

    combainedReportObj={}
    repeat=1
    reportOutputMsg=""

    checkArgs()

    reportDirName = "FossReport" + datetime.datetime.now().strftime("_%Y-%m-%d") + datetime.datetime.now().strftime("_%H:%M")
    reportDirNameX = "FossReport"
    path = HOME + reportDirName + "/"
    pathX = HOME + reportDirNameX + "/"

    if(os.path.exists(path)):
        print("Report directory already exists: ",path,". Exiting...")
        sys.exit()

    if(os.path.exists(pathX)):
        print("Clean default directory: ",pathX,".")
        os.remove(pathX)

    os.mkdir(path)
    os.mkdir(pathX)
    reportFolderPath=path
    reportFolderPathX=pathX
    
    print("")
    innerPath = path+SC_REPORT_NAME
    innerPathX = pathX+SC_REPORT_NAME
    print("Creating report for SC to path: "+innerPath+" and default path: "+innerPathX)
    os.mkdir(innerPath)
    os.mkdir(innerPathX)
    print("")

    for turn in range(0,repeat):
        matchedFosslist=[]				#KEEPING THE CAX NUMBERS
        fossFilesIndex={}
        unusedFossFiles=[]
        reportFileObj={}
        fileListObj={}

        reportPath=reportFolderPath+SC_REPORT_NAME
        reportPathX=reportFolderPathX+SC_REPORT_NAME
        fossFilesDir=HOME + fossFilesDirSC
        targetNameReport="Maven"
        print("Running for SC")

        print("")
        print("Starting the FOSS report script...")
        print("")
        print("Checking Files..... ", end=" ")

        #OPEN AND READ THE FILES INTO YAML OBJECTS
        checkFiles()

        mvnFileObj=""
        if MVN_FILE:
            mvnFileObj=readFile(HOME + MVN_FILE)

        for fossFile in os.listdir(fossFilesDir):
            fossFileObj=readFile(fossFilesDir + "/" + fossFile)

            if checkFossTags(fossFileObj['FOSS']) == 1 :
                print("")
                print("The FOSS file",fossFile," contains empty tags. Exiting...")
                sys.exit()    

        print("OK")

        #CREATE FOSS FILE INDEX WITH {DEPENDENCY : FILENAME }
        fossFilesIndex=createFossIndex(fossFilesDir)

        # CREATE DEPENDENCY LIST
        dependencyList=[]
        try:
            if mvnFileObj:
                for i in mvnFileObj['Dependency']:
                    dependencyList.append(i)

        except IOError as e:
            print("Failed reading the Dependency tag from dependency files. Exiting... " % e)
            sys.exit()

        reportFileObj, numberOfMatchedDependencies, undefinedDependencyList=generateReportFileObj(dependencyList, fossFilesIndex, matchedFosslist, fossFilesDir)

        for fossFile in os.listdir(fossFilesDir):      #GATHER THE FOSS FILES THAT WERE NOT MATCHED TO NONE OF THE DEPENDENCIES
            matched=False
            for matchedFoss in matchedFosslist:
                if matchedFoss == fossFile:
                    matched=True

            if not matched:
                unusedFossFiles.append(fossFile)

        fileListObj, matchedFosslist, unusedFossFiles=generateReportLstObj(matchedFosslist, unusedFossFiles)

        #CREATE THE REPORT
        createReport(reportFileObj,fileListObj,reportPath)
        createReport(reportFileObj,fileListObj,reportPathX)     

        dpath_util.merge(combainedReportObj,reportFileObj, flags=dpath_util.MERGE_ADDITIVE)

        reportOutputMsg=reportOutputMsg+("-" * 100)
        reportOutputMsg=reportOutputMsg+("\nNumber of " + targetNameReport + " Dependencies matched: " + str(numberOfMatchedDependencies))
        reportOutputMsg=reportOutputMsg+("\nNumber of " + targetNameReport + " Undefined dependencies: " + str(len(undefinedDependencyList)))
        reportOutputMsg=reportOutputMsg+("\nNumber of " + targetNameReport + " FOSS products included: " + str(len(matchedFosslist)))
        reportOutputMsg=reportOutputMsg+("\nNumber of " + targetNameReport + " Unused FOSS products: " + str(len(unusedFossFiles)))
        reportOutputMsg=reportOutputMsg+("\n\nFOSS files directory used: " + fossFilesDir)

        if MVN_FILE:
            reportOutputMsg=reportOutputMsg+("\nMaven dependency file used: " + MVN_FILE)

        reportOutputMsg=reportOutputMsg+"\n"
        reportOutputMsg=reportOutputMsg+("-" * 100)
        reportOutputMsg=reportOutputMsg+("\n " + targetNameReport + " FOSS Report \"" + reportFolderPath + "\" directory and \"" + reportFolderPathX + "\"\n")
        reportOutputMsg=reportOutputMsg+("-" * 100)
        reportOutputMsg=reportOutputMsg+"\n"

    print(reportOutputMsg)
    if createXlsx:                              # PROCEED WITH CREATING THE XLSX FILES
        generateXlsx(reportFolderPath, combainedReportObj)
        generateXlsx(reportFolderPathX, combainedReportObj)
        
    if createCSV:                              # PROCEED WITH CREATING THE CSV FILES
       generateCSV(reportFolderPath, combainedReportObj)
       generateCSV(reportFolderPathX, combainedReportObj)  

    if len(undefinedDependencyList) > 0:
    	print("Undefined dependencies exist.. Please resolve and rerun FOSS analysis!")
    	sys.exit(1)    

main()
