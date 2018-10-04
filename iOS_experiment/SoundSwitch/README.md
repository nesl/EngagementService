# SoundSwitch

### Original Repo:
[https://github.com/moshegottlieb/SoundSwitch](https://github.com/moshegottlieb/SoundSwitch)

Our implementation is adapted based on this SoundSwitch repo. We extend this repo and add logics to detect screen on/off status.

### What we have tested
* Vibration switch status
* Screen status

### Assumptions
iOS doesn't have any public API for silence detection or screen on/off detection. This repo provides similar alternatives for those.
* This solution will not give you real time detection, the check is ran every second.
* It will only work on audio categories that respect the silent switch (the default ambient category does, but play & record does not).
* It's a dirty hack, but 100% public API based.
* At present, it doesn't support running in the background because iOS restriction. Adding other features like location/motion status detection may help (not sure).

#### Test 1: Vibration switch status
The trick is quite simple, all we need to do is to play a system sound, measure the amount of time it took for the sound to complete playing, and determine if we're in silent mode or not.
We'll need to install a sound completion routine for that.
Something like:
```objectivec
void SharkfoodSoundMuteNotificationCompletionProc(SystemSoundID  ssID,void* clientData); // sound completion proc
/** ... **/
if (AudioServicesCreateSystemSoundID((__bridge CFURLRef)url, &_soundId) == kAudioServicesNoError){
  AudioServicesAddSystemSoundCompletion(self.soundId, CFRunLoopGetMain(), kCFRunLoopDefaultMode, SharkfoodSoundMuteNotificationCompletionProc,(__bridge void *)(self));
}
```
All we need to do now is to periodically play a system sound (silent sound, probably) and check how long it took to play it - near zero value will mean the silent switch is on.


#### Test 2: Screen on/off detection
Actually screen on/off detection is not exactly implemented here. What I check is whether the device is locked or not. This method works as long as we enable data protection like password or touchID to unlock a device.
Something like:
```objectivec
- (void)applicationProtectedDataDidBecomeAvailable:(UIApplication *)application{
    NSLog(@"unlocked")
}

-(void)applicationProtectedDataWillBecomeUnavailable:(UIApplication *)application{
    NSLog(@"locked")
}
```
All we need to do is to detect the status of protected data. If it is available, UNLOCKED, indicating SCREEN ON; If unavailable, LOCKED, indicating SCREEN OFF. Although sometimes users turn on their screen but doesn't unlock their devices, the approximation is pretty close.

#### Testing Setup
Both these two features cannot be tested in an Xcode simulator (The modules in the app interface can show up, but they doesn't respond to any hardware status switch. So I have to test them using a real iPhone.

##### Device info:
iPhone 7 32G
iOS 12.0
(for iOS >= 5.0, the hardware detection like "AudioSessionGetProperty" is blocked. Apple also stated in a developer forum that there is no valid API for the exact screen on/off detection)

##### Test result
Ringer switches off ---- "silent mode is on"  in the app interface
Ringer switches on  ---- "silent mode is off" in the app interface
Lock the phone      ---- "applicationProtectedDataWillBecomeUnavailable [the 53th line] + ... (I don't care)" in the Xcode console
Unlock the phone using password or touchID ---- "applicationProtectedDataDidBecomeAvailable [the 48th line] + ... (I don't care)" in the Xcode console

#### Things to pay attention to
In Xcode 10 (what I use), NSLog only works for a simulator rather than a real device. My solution is:
```
//In the corresponding .h files
#ifdef DEBUG
#define NSLog(format, ...) printf("%s [the %dth line] %s\n", __FUNCTION__, __LINE__, [[NSString stringWithFormat:format, ## __VA_ARGS__] UTF8String]);
#else
#define NSLog(format, ...)
#endif
```

When playing with a real phone, we need to do the following:
1. Add your Apple ID in Xcode Preferences > Accounts > Add Apple ID. (the apple account should be the same as the real iPhone's account)
2. Select the project (the root of the directory tree on the left) -- go to General section -- Click "Team" -- Switch it from "None" to your existing team  -- define a unused identifier -- sign -- connect the iPhone to Mac
3. Beside the "run" triangular button, change the carrier from a simulator to the XXX's iPhone
4. Run and play
