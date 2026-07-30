#!/bin/bash

set -e

mvn --batch-mode --no-transfer-progress clean package license:aggregate-add-third-party -P license-check -DskipTests

IGNORED_HEADER_LINES=2
COUNTER=0
ERROR_COUNT=0

while read line; do
   if (( $COUNTER < $IGNORED_HEADER_LINES )); then
     COUNTER=$(($COUNTER+1))
     continue
   fi

   if [[ $line == *"(Eclipse Public License - v 1.0)"* ]] ||
      [[ $line == *"(Eclipse Public License 1.0)"* ]] ||
      [[ $line == *"(Eclipse Public License, Version 1.0)"* ]] ||
      [[ $line == *"(Eclipse Distribution License - v 1.0)"* ]] ||
      [[ $line == *"(Eclipse Public License v2.0)"* ]] ||
      [[ $line == *"(Eclipse Public License, Version 2.0)"* ]] ||
      [[ $line == *"(The Apache Software License, Version 2.0)"* ]] ||
      [[ $line == *"(Apache License, Version 2.0)"* ]] ||
      [[ $line == *"(Apache License, version 2.0)"* ]] ||
      [[ $line == *"(Apache-2.0)"* ]] ||
      [[ $line == *"(Apache License 2.0)"* ]] ||
      [[ $line == *"(The Apache License, Version 2.0)"* ]] ||
      [[ $line == *"(Apache Software License, version 2.0)"* ]] ||
      [[ $line == *"(Apache 2)"* ]] ||
      [[ $line == *"(Apache 2.0)"* ]] ||
      [[ $line == *"(BSD 2-Clause License)"* ]] ||
      [[ $line == *"(The BSD 2-Clause License)"* ]] ||
      [[ $line == *"(BSD-2-Clause)"* ]] ||
      [[ $line == *"(BSD License 3)"* ]] ||
      [[ $line == *"(BSD-3-Clause)"* ]] ||
      [[ $line == *"(Revised BSD)"* ]] ||
      [[ $line == *"(BSD)"* ]] ||
      [[ $line == *"(The BSD License)"* ]] ||
      [[ $line == *"(EDL 1.0)"* ]] ||
      [[ $line == *"(EPL 2.0)"* ]] ||
      [[ $line == *"(EPL-2.0)"* ]] ||
      [[ $line == *"(Eclipse Public License v. 2.0)"* ]] ||
      [[ $line == *"(Eclipse Public License 2.0)"* ]] ||
      [[ $line == *"(CDDL/GPLv2+CE)"* ]] ||
      [[ $line == *"(CDDL+GPL License)"* ]] ||
      [[ $line == *"(CDDL + GPLv2 with classpath exception)"* ]] ||
      [[ $line == *"(CDDL 1.1) (GPL2 w/ CPE)"* ]] ||
      [[ $line == *"(GNU Lesser General Public License)"* ]] ||
      [[ $line == *"(The MIT License (MIT))"* ]] ||
      [[ $line == *"(The MIT License)"* ]] ||
      [[ $line == *"(MIT License)"* ]] ||
      [[ $line == *"(MIT license)"* ]] ||
      [[ $line == *"(MIT)"* ]] ||
      [[ $line == *"(Mozilla Public License, Version 2.0)"* ]] ||
      [[ $line == *"(Unicode/ICU License)"* ]] ||
      [[ $line == *"(Unicode-3.0)"* ]] ||
      [[ $line == *"(Public Domain)"* ]] ||
      [[ $line == *"(Public Domain, per Creative Commons CC0)"* ]]; then
    continue
   fi;
   ERROR_COUNT=$(($ERROR_COUNT+1))
   echo "$line"
done <target/generated-sources/license/THIRD-PARTY.txt


if (( $ERROR_COUNT > 0 )); then
  echo "$ERROR_COUNT errors found, please consult the project management to see if the flagged libraries/licenses above can be whitelisted, too"
  exit 1
else
  echo "Everything fine"
fi
