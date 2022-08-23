# Dynco Application
##### _(c) 2020-2021 Dynamic Element A G_
#
#
This repository contains the code for using the MDDI service with Android Client. This android app is basically a ImageResult feature matching application. It is helpful to find the feature comparison between images. The dependencies and the following configuration should be met for the proper working of the application.



### Dependencies:
- Android Studio 4.1.2 and above
- Minimum SDK version 24
- Target SDK version 29
- Git 2.3.0 and above
- OpenCV 3.4.9 

### Clone the project:
##### For Linux and Windows:-
#
- Install Android Studio and open it.
- Install Git(if it is not already installed)
- On the taskbar, select VCS -> Git -> Clone..
- "Get from version control" dialog box will be displayed.
- Select "Git" from version control.

```sh
https://gitlab.com/dynamic-element/Android_Dynco.git
```
- Copy the above link and paste it in the "URL:" box.
- Click "Clone". The project should be successfully cloned.


### Configure the project:
##### For Linux and Windows:-
#
- Download the required SDK and NDK files.
- Make sure you have the android SDK platforms(APIs 29,30,31)  
- Sync the project with gradle files.
- Install proto plugins(If not already installed)
- Sync the project again.
- The project should be successfully configured and we can start working on this project.

### Build and Install the APK file:
##### 1. Build APK:-
#
- On the taskbar, select Build -> Build Bundle(s)/APK(s) -> Build APK(s).
- After successfully building the APK files, bottom pop up message will be displayed.
- Click "locate" to check the different versions of APK files.
- Choose the correct version of APK file for the android phone and install it.

##### 2. USB debugging Method to install APK:-
#
- If the connected phone supports USB debugging, the apk can be directly installed.
- Enable 'USB debugging' under Developer options on the Android.
- Connect the android to the PC through USB. Now, the phone is connected to android studio.
- Now, click 'Run' option to install the APK.

### Description about the Dynco User application:
#
- Dynco application is a MDDI service based application for feature comparison. Currently, the application works on the IVF and DB-SNO instance. 
- This enables us to take the images through smartphone's camera.
- It can be be used to add and search the images from the MDDI database.
- If any matching ImageResult is found, we will get a positive response from the server. 
- We will usually get a posiitve "UID" value and a "Score" value (0.4 to 1) for a matching ImageResult.
- For a non-matched ImageResult, the "UID" and "Score" will be "-1" and "0" respectively.


