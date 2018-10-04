//
//  DeviceLockStatus.m
//  SoundSwitch
//
//  Created by Siyou Pei on 2018/9/27.
//  Copyright © 2018 Moshe Gottlieb. All rights reserved.
//

//#import <CoreFoundation/CoreFoundation.h>

#import "notify.h"
#import "DeviceLockStatus.h"
#import "SoundSwitch-Swift.h"


@implementation DeviceLockStatus

-(void)registerAppforDetectLockState {
    int notify_token;
    notify_register_dispatch("com.apple.springboard.lockstate", &notify_token,dispatch_get_main_queue(), ^(int token) {
        uint64_t state = UINT64_MAX;
        notify_get_state(token, &state);
        
        DeviceStatus *myOb = [DeviceStatus new];  //DeviceStatus is .swift file
        //DeviceStatus *myOb = [[DeviceStatus alloc] init];
        
        if(state == 0) {
            [myOb unlocked];
            NSLog(@"It is unlocked")
        } else {
            [myOb locked];
            NSLog(@"It is locked")
        }
        
    });
}
@end
