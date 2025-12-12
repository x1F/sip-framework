#!/bin/bash

set -euo pipefail

RELEASE_VERSION=$1
CLEAN_FRAGMENTS=${CLEAN_FRAGMENTS:-false}
HEADER_LINES=8

section_title() {
  # Map fragment folder name to section header used in release notes.
  case "$1" in
    major) echo "### 🚀 Major Changes" ;;
    feature) echo "### ⭐ Features" ;;
    bugfix) echo "### 🐞 Bugfixes" ;;
    documentation) echo "### 📔 Documentation" ;;
    *) echo "### Other" ;;
  esac
}

render_line() {
  # Render one fragment JSON into a markdown bullet line.
  local file=$1
  local msg pr issue author
  msg=$(jq -r '.message' "${file}")
  pr=$(jq -r '"[#\(.pullrequestId)](https://github.com/x1F/sip-framework/pull/\(.pullrequestId))"' "${file}")
  issue=$(jq -r '.issue' "${file}")
  if echo "${issue}" | grep -q null; then
    issue=""
  else
    issue="/[#$issue](https://github.com/x1F/sip-framework/issues/${issue})"
  fi
  author=$(jq -r '"[\(.author)](https://github.com/\(.author))"' "${file}")
  echo "- ${msg} ${pr}${issue} by ${author}"
}

ensure_release_block() {
  # If the release block for this version does not exist in the given file, create it at the top.
  local file=$1
  if ! grep -q "^## ${RELEASE_VERSION} " "${file}" 2>/dev/null; then
    # prepend new block to file
    tmp=$(mktemp)
    echo "## ${RELEASE_VERSION} - $(date +%Y-%m-%d)" > "${tmp}"
    echo "" >> "${tmp}"
    if [ -f "${file}" ]; then
      cat "${file}" >> "${tmp}"
    fi
    mv "${tmp}" "${file}"
  fi
}

dedupe_line() {
  # Insert a line into a section, avoiding duplicates (by PR id or exact line).
  local line=$1 pr=""
  if [[ "${line}" =~ \[#([0-9]+)\] ]]; then
    pr="${BASH_REMATCH[1]}"
  fi
  awk -v section="$2" -v line="$line" -v pr="$pr" '
    BEGIN {in_section=0; inserted=0}
    /^### / {
      if(in_section==1 && inserted==0){print line; inserted=1}
      if($0==section){in_section=1} else {in_section=0}
      print; next
    }
    {
      if(in_section==1){
        pr_line="";
        match($0, /\[#([0-9]+)\]/, m);
        if(m[1]!=""){pr_line=m[1]}
        if(pr!="" && pr_line==pr){next}
        if($0==line){next}
      }
      print
    }
    END{
      if(in_section==1 && inserted==0){print line; inserted=1}
      if(inserted==0){print section; print ""; print line; inserted=1}
    }
  '
}

add_line_to_file() {
  # Add the given line into the desired section of the target file.
  local file=$1 section=$2 line=$3
  local tmp
  tmp=$(mktemp)
  dedupe_line "${line}" "${section}" < "${file}" > "${tmp}"
  mv "${tmp}" "${file}"
}

merge_fragments_into_changelog() {
  # Merge all fragment files into the target changelog file, creating sections as needed.
  local target_file=$1
  for dir in major feature bugfix documentation other; do
    if test -n "$(find "${dir}" -name '*.json' -print -quit 2>/dev/null)"; then
      local section
      section=$(section_title "${dir}")
      ensure_release_block "${target_file}"
      # ensure section exists (will be added by dedupe if missing)
      for f in "${dir}"/*.json; do
        [ -e "${f}" ] || continue
        line=$(render_line "${f}")
        add_line_to_file "${target_file}" "${section}" "${line}"
        if [ "${CLEAN_FRAGMENTS}" = "true" ] && [ -f "${f}" ]; then
          git rm -f "${f}"
        fi
      done
    fi
  done
}

# ensure current release file exists
if [ ! -f current-release-changelog.md ]; then
  echo "## ${RELEASE_VERSION} - $(date +%Y-%m-%d)" > current-release-changelog.md
  echo "" >> current-release-changelog.md
fi

merge_fragments_into_changelog current-release-changelog.md

# rebuild main changelog by replacing this release block with the current one
HEADER_CONTENT=$(head -n ${HEADER_LINES} ../CHANGELOG.md)
REST_CONTENT=$(tail -n +$((HEADER_LINES+1)) ../CHANGELOG.md)

FILTERED_TAIL=$(printf "%s\n" "${REST_CONTENT}" | awk -v ver="${RELEASE_VERSION}" '
  BEGIN {skip=0}
  /^## / {
    if ($0 ~ "^## "ver" ") {skip=1; next}
    if (skip==1) {skip=0}
  }
  skip==0 {print}
')

{
  printf "%s\n\n" "${HEADER_CONTENT}"
  cat current-release-changelog.md
  echo ""
  printf "%s\n" "${FILTERED_TAIL}"
} > ../CHANGELOG.md
