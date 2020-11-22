#!/bin/bash

DEPLOY_CONFIG=$1

if [[ $# < 1 ]]; then
	echo "not enough args"
	echo "usage: $0 <config>"
	exit 1
fi

if [[ $DEPLOY_CONFIG != "config-osx" || $DEPLOY_CONFIG != "config-linux" ]]; then
	echo "invalid config: \"$DEPLOY_CONFIG\""
	echo -ne "possible configs:\n"
	echo -ne "\t- config-osx\n"
	echo -ne "\t- config-linux\n"
	exit 2
fi

cd desktopapp
mvn assembly:single
cd ../..
mv whork/desktopapp/target/desktopapp-0.0.0-jar-with-dependencies.jar .

if [[ $DEPLOY_CONFIG == "config-osx" ]]; then
	mv desktopapp-0.0.0-jar-with-dependencies.jar whork-desktop-$TRAVIS_TAG-osx.jar
elif [[ $DEPLOY_CONFIG == "config-linux" ]]; then
	mv desktopapp-0.0.0-jar-with-dependencies.jar whork-desktop-$TRAVIS_TAG-linux.jar
	cd whork/webapp
	mvn assembly:single
	cd ../..
	mv whork/webapp/target/webapp-0.0.0-jar-with-dependencies.jar .
	mv webapp-0.0.0-jar-with-dependencies whork-webapp-$TRAVIS_TAG-any.jar
	cd docs
	pdflatex srs.tex
	rm -rfv srs.log srs.aux srs.tex *.log
	cd ..
	zip -r whork-docs-$TRAVIS_TAG.zip docs
fi

