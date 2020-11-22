#!/bin/bash

DEPLOY_CONFIG=$1

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

