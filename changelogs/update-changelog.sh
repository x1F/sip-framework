#!/bin/bash

RELEASE_VERSION=$1

function writeChangelogLine() {
    echo "handling file $1"
    MSG=$(jq -r '.message' $1)
    PR=$(jq -r '"[#\(.pullrequestId)](https://github.com/x1F/sip-framework/pull/\(.pullrequestId))"' $1)
    ISSUE=$(jq -r '.issue' $1)
    if test $(echo $ISSUE | grep null); then
      ISSUE=""
    else
      ISSUE="/[#$ISSUE](https://github.com/x1F/sip-framework/issues/$ISSUE)"
    fi
    AUTHOR=$(jq -r '"[\(.author)](https://github.com/\(.author))"' $1)
    echo "- $MSG $PR$ISSUE by $AUTHOR"  >> current-release-changelog.md
    git rm $1
}

# store footer so we can append it later again
tail -n +8 ../CHANGELOG.md > tmp_footer.md

# replace file with only the header
echo "$(head -n  8 ../CHANGELOG.md)" > ../CHANGELOG.md

# create current changelog
echo "## ${RELEASE_VERSION} - $(date +%Y-%m-%d)" > current-release-changelog.md
echo "" >> current-release-changelog.md

if test -n "$(find major -name '*.json' -print -quit)"; then
  echo -e "### 🚀 Major Changes" >> current-release-changelog.md
  for f in $(ls major/*.json); do
    writeChangelogLine $f
  done;
  echo "" >> current-release-changelog.md
fi

if test -n "$(find feature -name '*.json' -print -quit)"; then
  echo -e "### ⭐ Features" >> current-release-changelog.md
  for f in $(ls feature/*.json); do
    writeChangelogLine $f
  done;
  echo "" >> current-release-changelog.md
fi

if test -n "$(find bugfix -name '*.json' -print -quit)"; then
  echo -e "### 🐞 Bugfixes" >> current-release-changelog.md
  for f in $(ls bugfix/*.json); do
    writeChangelogLine $f
  done;
  echo "" >> current-release-changelog.md
fi

if test -n "$(find documentation -name '*.json' -print -quit)"; then
  echo -e "### 📔 Documentation" >> current-release-changelog.md
  for f in $(ls documentation/*.json); do
    writeChangelogLine $f
  done;
  echo "" >> current-release-changelog.md
fi


# add current changelog
echo "" >> ../CHANGELOG.md
cat current-release-changelog.md >> ../CHANGELOG.md

# readd old versions footer
cat tmp_footer.md >> ../CHANGELOG.md
rm tmp_footer.md
