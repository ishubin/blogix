#!/bin/bash

if [ "$(id -u)" != "0" ]; then
    echo "You should run this script as root: sudo $0" 
    exit 1
fi

mkdir -p /opt/blogix
cp blogix.jar /opt/blogix/blogix.jar

cp -r templates /opt/blogix/.

if [[ -e /usr/bin ]]; then
    cp blogix /usr/bin/blogix
else
    echo "Could not copy blogix executable to /usr/bin"
    exit 1
fi


echo "Done. Blogix is now installed"
