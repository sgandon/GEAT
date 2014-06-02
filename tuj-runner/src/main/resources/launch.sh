#! /bin/bash
cd `dirname $0`
java -cp . org.talend.colibri.EntryPoint $1 $2
