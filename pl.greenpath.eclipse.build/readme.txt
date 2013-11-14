These instructions are for versions of Eclipse starting with 3.6 (Helios). For earlier versions, see the post http://www.modumind.com/2007/06/06/getting-started-with-pde-build/.

To set up the build, do the following:

1. Import and configure the build project.

* Import the three projects into your Eclipse workspace.

* In the build.properties file in this project, set the "eclipseLocation" property to your Eclipse installation (directory containing eclipse.exe). 

* Modify the "configs" property to specify the platform you wish to build for. It is currently set to build for win32.

2. Create the build repo and target target.

* Create a directory called c:\helloworld-base.

* Under c:\helloworld-base, create the nested directories "repos\rcp". Extract the RCP Runtime Binary here.

* Under c:\helloworld-base, create the directory "target". Extract the RCP Delta Pack here.


3. Run the build.

* In your Eclipse workspace, right click on the build.xml file in this project and choose Run As -> Ant Build from the context menu.


4. Verify the build.

* When the build completes, navigate to {USER.HOME}/eclipse-build/I.HelloWorld and unzip the HelloWorld-win32.win32.x86.zip file to some place on your hard drive.

* Locate and execute the helloworld.exe file to verify that the application runs.


If you have any problems, feel free to contact me at patrick@modumind.com.

Regards,

Patrick Paulin
Eclipse RCP/OSGi Trainer and Consultant
Modular Mind

patrick@modumind.com
www.modumind.com

twitter.com/pjpaulin
linkedin.com/in/pjpaulin
