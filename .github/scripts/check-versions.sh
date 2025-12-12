#!/bin/bash

set -euo pipefail

EXPECTED_VERSION=$1

echo "Checking that all modules have version ${EXPECTED_VERSION}"

# Check root version
ROOT_VERSION=$(mvn -q -DforceStdout help:evaluate -Dexpression=project.version)
if [ "${ROOT_VERSION}" != "${EXPECTED_VERSION}" ]; then
  echo "ERROR: Root version ${ROOT_VERSION} does not match expected ${EXPECTED_VERSION}"
  exit 1
fi
echo "✓ Root version: ${ROOT_VERSION}"

# Check module versions
MODULES_RAW=$(mvn -q -DforceStdout help:evaluate -Dexpression=project.modules | tail -n1)
MODULES=$(echo "${MODULES_RAW}" | tr -d '[]' | tr ',' ' ' | xargs)

if [ -n "${MODULES}" ]; then
  for MODULE in ${MODULES}; do
    VERSION=$(mvn -q -pl "${MODULE}" -N -DforceStdout help:evaluate -Dexpression=project.version | tail -n1)
    if [ "${VERSION}" != "${EXPECTED_VERSION}" ]; then
      echo "ERROR: Module ${MODULE} is at version ${VERSION}, expected ${EXPECTED_VERSION}"
      exit 1
    fi
    echo "✓ Module ${MODULE}: ${VERSION}"
  done
fi

echo "All versions match ${EXPECTED_VERSION}"