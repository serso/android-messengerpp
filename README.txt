DESCRIPTION

Messenger++ is a multi-protocol instant messaging client for Android OS. It's free and open-source.
M++ is available on Google Play: https://play.google.com/store/apps/details?id=org.solovyev.android.messenger

INSTALLATION

The project is based on Maven 3. In order to build it you need to install Apache Maven and run 'mvn install' command.
NOTE: You need Maven 3.1.x or higher in order to build the project.

Some modules of project contain dependencies not included in Maven Central - to build such modules you need first install
these dependencies in your local repository. To do this, please, download Android SDK Deployer:
git clone git://github.com/serso/maven-android-sdk-deployer.git
And then:
cd ./maven-android-sdk-deployer/
git checkout tags/api-19-mvn3.1
mvn install -P4.2

LICENSE

Apache 2.0, see LICENSE.txt.

---------------------------------------------------------------------

Contact details

Email: se.solovyev@gmail.com
Site:  http://se.solovyev.org