#!/bin/bash
PRODUCT_PATH=""

while [ "$1" != "" ]; do
  case $1 in
    -p|--path)
      if [ -n "$2" ]; then
        PRODUCT_PATH="$2"
        shift 2
        continue
      else
        echo "ERROR: '--path' requires a non-empty option argument."
        exit 1
      fi
        ;;
    -h|-\?|--help)
      echo "Usage: $(basename $0) --path <APPLICATION_HOME>"
      exit
      ;;
    --)              # End of all options.
      shift
      break
      ;;
    -?*)
      echo "WARN: Unknown option (ignored): $1"
      ;;
    *)               # Default case: If no more options then break out of the loop.
      break
  esac
  shift
done

java -jar target/SecurityConfigChecker-1.0-SNAPSHOT-jar-with-dependencies.jar $PRODUCT_PATH
