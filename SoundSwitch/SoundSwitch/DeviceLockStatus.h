//
//  DeviceLockStatus.h
//  SoundSwitch
//
//  Created by Siyou Pei on 2018/9/27.
//  Copyright © 2018 Moshe Gottlieb. All rights reserved.
//
#ifdef DEBUG
#define NSLog(format, ...) printf("[%s] %s [第%d行] %s\n", __TIME__, __FUNCTION__, __LINE__, [[NSString stringWithFormat:format, ## __VA_ARGS__] UTF8String]);
#else
#define NSLog(format, ...)
#endif

#ifndef DeviceLockStatus_h
#define DeviceLockStatus_h

//#import <CoreFoundation/CoreFoundation.h>
#import <Foundation/Foundation.h>

@interface DeviceLockStatus : NSObject

@property (strong, nonatomic) id someProperty;

-(void)registerAppforDetectLockState;

@end
#endif /* DeviceLockStatus_h */
