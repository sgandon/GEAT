#! /bin/bash
cd `dirname $0`
REV=$(svn log --limit 1 $1 | grep ^r | head -1 | perl -ne 'm/^r(\d+)/; print $1')
./launch.sh trunk $REV
