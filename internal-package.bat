cd webapp
mvn assembly:single
cd ../..
mv whork/webapp/target/desktopapp-0.0.0-jar-with-dependencies.jar .
ren desktopapp-0.0.0-client-jar-with-dependencies.jar whork-desktop-%APPVEYOR_REPO_TAG_NAME%-win32.jar
