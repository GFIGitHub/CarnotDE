#!/bin/bash
INSTALLATION_ROOT=$(echo $INSTALLATION_ROOT)
# If env var is not set, prompt user for path to WDB dir 
if [ -z $INSTALLATION_ROOT ]; then
    echo "Note to user: To avoid always being prompted for this env var, put 'source' before the path to this script."
    echo "Ex: source ~/Workspace/F15D1/WDB"

    echo ""
    echo ""

    echo "Environment variable INSTALLATION_ROOT not found."
    echo "Enter the absolute or relative path to the WDB directory or QUIT to exit setup."
    echo "For reference, here is your current working directory: $(pwd)"
    read INPUT
    if [ $INPUT = "quit" -o $INPUT = "QUIT" ]; then
      echo "Exiting without setting environment variable."
      exit -1
    else
      INPUT=$(realpath -e $INPUT)
      if [ -d $INPUT ]; then
        OLD_DIR=$(pwd)
        cd $INPUT
        export INSTALLATION_ROOT=$(realpath -e $INPUT)
        cd $OLD_DIR
      else
        echo "Input is path to valid directory but something went wrong. Exiting"
        exit -1
      fi
    fi
fi

# Double check that the env var actually got set before proceeding
if [ -z $INSTALLATION_ROOT ]; then
  echo "Setting INSTALLATION_ROOT unsuccessful."
  exit -1
fi
OLD_DIR=$(pwd)
cd $INSTALLATION_ROOT
# ./build.sh clean all
java -classpath classes/production/WDB:lib/je.jar:lib/antlr-complete.jar:lib/avro.jar:lib/gwt-servlet.jar:lib/jackson-core-asl.jar:lib/jackson-mapper-asl.jar:lib/jetty-continuation.jar:lib/jetty-http.jar:lib/jetty-io.jar:lib/jetty-security.jar:lib/jetty-server.jar:lib/jetty-servlet.jar:lib/jetty-util.jar:lib/jetty-webapp.jar:lib/jetty-xml.jar:lib/jline.jar:lib/jsch.jar:lib/kvclient.jar:lib/kvstore.jar:lib/servlet-api.jar:lib/commons-lang3-3.4.jar wdb.WDB;
cd $OLD_DIR
