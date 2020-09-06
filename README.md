## Goal
My goal is to essentially run the Android Vosk Demo as a foreground service, but without the file reading methods; that is, I want to have the phone's microphone always be listening to the user and recognizing speech, even when the app has been minimized or closed. The user will be always aware of this, as a notification be visible until the service stops.

## Issues

I'm getting a crash whenever I start the service (Fatal signal 11, code 1). I use the TestService class as the Service and the DemoActivity as the Activity that calls the service. The TestService is almost a carbon copy of the KaldiActivity class, albeit a VERY crude one.
I removed any methods I deemed "unnecessary", since I'll only be using the recognizeMicrophone() method and no file recognition or UI elements will be necessary.
I suspect the bug lays on the SetupTask class. As mentioned in a comment on the KaldiActivity class, "Recognizer initialization is a time-consuming and it involves IO, so we execute it in async task".

Since I don't know if I can initialize the Recognizer in the main activity (DemoActivity) and pass the Model object to this service by an Intent, I set to execute the SetupTask in this service. However, when the service is called by the activity, the model is most likely not initialized yet, and then I get a segmentation fault error.

I hope I'm correct in my assessment, but I really wouldn't be surprised if I'm not. As I stated, I'm fairly new to Android development, and services have been a tricky subject for me to learn.

In any case, I wanted to thank you, Nickolay, for the help. Besides the VOSK demo, most Speech Recognition related questions on StackOverflow have been answered by you, and they've helped me in great lengths for developing my project :)
