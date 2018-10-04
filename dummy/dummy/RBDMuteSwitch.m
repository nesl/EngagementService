//
//  RBDMuteSwitch.m
//  dummy
//
//  Created by Siyou Pei on 2018/9/25.
//  Copyright © 2018 Siyou Pei. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "RBDMuteSwitch.h"

static RBDMuteSwitch *_sharedInstance;

@implementation RBDMuteSwitch

@synthesize delegate;

+ (RBDMuteSwitch *)sharedInstance
{
    if (!_sharedInstance) {
        _sharedInstance = [[[self class] alloc] init];
    }
    return _sharedInstance;
}

- (id)init
{
    self = [super init];
    if (self) {
    }
    
    return self;
}

/*
- (void)dealloc {
    [delegate release];
    [super dealloc];
}
 */

- (void)playbackComplete {
    if ([(id)self.delegate respondsToSelector:@selector(isMuted:)]) {
        // If playback is far less than 100ms then we know the device is muted
        if (soundDuration < 0.010) {
            [delegate isMuted:YES];
        }
        else {
            [delegate isMuted:NO];
        }
    }
    [playbackTimer invalidate];
    playbackTimer = nil;
    
    
}

static void soundCompletionCallback (SystemSoundID mySSID, void* myself) {
    AudioServicesRemoveSystemSoundCompletion (mySSID);
    [[RBDMuteSwitch sharedInstance] playbackComplete];
}

- (void)incrementTimer {
    soundDuration = soundDuration + 0.001;
}

- (void)detectMuteSwitch {
/*#if TARGET_IPHONE_SIMULATOR
    // The simulator doesn't support detection and can cause a crash so always return muted
    if ([(id)self.delegate respondsToSelector:@selector(isMuted:)]) {
        [self.delegate isMuted:YES];
    }
    return;
#endif
 */
    
    // iOS 5+ doesn't allow mute switch detection using state length detection
    // So we need to play a blank 100ms file and detect the playback length
    soundDuration = 0.0;
    CFURLRef        soundFileURLRef;
    SystemSoundID    soundFileObject;
    
    // Get the main bundle for the app
    CFBundleRef mainBundle;
    mainBundle = CFBundleGetMainBundle();
    
    // Get the URL to the sound file to play
    soundFileURLRef  =    CFBundleCopyResourceURL(
                                                  mainBundle,
                                                  CFSTR ("detection"),
                                                  CFSTR ("aiff"),
                                                  NULL
                                                  );
    
    // Create a system sound object representing the sound file
    AudioServicesCreateSystemSoundID (
                                      soundFileURLRef,
                                      &soundFileObject
                                      );
    
    AudioServicesAddSystemSoundCompletion (soundFileObject,NULL,NULL,
                                           soundCompletionCallback,
                                           (__bridge void*) self);
    
    // Start the playback timer
    playbackTimer = [NSTimer scheduledTimerWithTimeInterval:0.001 target:self selector:@selector(incrementTimer) userInfo:nil repeats:YES];
    // Play the sound
    AudioServicesPlaySystemSound(soundFileObject);
    return;
}

@end
