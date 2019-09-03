#!/bin/bash

JAR_PATH=lib
BIN_PATH=bin
SRC_PATH=src
TESTCASE_PATH=test_case
IDEALRESULTSET_PATH=ideal_result_set
DBINSTANCE_PATH=database_instance
CONFIG_PATH=config
MIDDLERESULT_PATH=middle_result


echo "build classpath"  
# java�ļ��б�Ŀ¼  
SRC_FILE_LIST_PATH=src/sources.list  
  
#�������е�java�ļ��б�  
rm -f $SRC_PATH/sources.list
find $SRC_PATH/ -name *.java > $SRC_FILE_LIST_PATH
  
#ɾ���ɵı����ļ� ����binĿ¼  
rm -rf $BIN_PATH/
mkdir $BIN_PATH/

#��������jar���б�  
for file in  ${JAR_PATH}/*.jar;  
do
jarfile=${jarfile}:${file}
done
#echo "jarfile = "$jarfile


#����
echo "compile Woodpecker"
javac -d $BIN_PATH/ -cp $jarfile @$SRC_FILE_LIST_PATH
#echo $jarfile$casefile$irsfile$dbifile$configfile
  
#����
echo "run Dispatcher"
java -cp $BIN_PATH$jarfile edu.ecnu.woodpecker.stresstest.Dispatcher
#echo $BIN_PATH$jarfile$casefile$irsfile$dbifile$configfile
