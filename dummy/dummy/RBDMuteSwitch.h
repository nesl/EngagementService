//
//  RBDMuteSwitch.h
//  dummy
//
//  Created by Siyou Pei on 2018/9/25.
//  Copyright © 2018 Siyou Pei. All rights reserved.
//

#ifndef RBDMuteSwitch_h
#define RBDMuteSwitch_h


#endif /* RBDMuteSwitch_h */

#import <Foundation/Foundation.h>
#include <AudioToolbox/AudioToolbox.h>

@class RBDMuteSwitch;

@protocol RBDMuteSwitchDelegate
@required
- (void)isMuted:(BOOL)muted;
@end

@interface RBDMuteSwitch : NSObject {
@private
    NSObject<RBDMuteSwitchDelegate> *delegate;
    float soundDuration;
    NSTimer *playbackTimer;
}

/**
 Your delegate
 */
@property (readwrite, retain) NSObject<RBDMuteSwitchDelegate> *delegate;

/** Creates a shared instance
 */
+ (RBDMuteSwitch *)sharedInstance;

/** Determines if the device is muted, wait for delegate callback using isMuted: on your delegate.
 */
- (void)detectMuteSwitch;

@end
