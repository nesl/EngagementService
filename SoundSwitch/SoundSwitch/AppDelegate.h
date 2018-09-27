//
//  AppDelegate.h
//  SoundSwitch
//
//  Created by Moshe Gottlieb on 6/2/13.
//  Copyright (c) 2013 Moshe Gottlieb. All rights reserved.
//
#ifdef DEBUG
#define NSLog(format, ...) printf("%s [第%d行] %s\n", __FUNCTION__, __LINE__, [[NSString stringWithFormat:format, ## __VA_ARGS__] UTF8String]);
#else
#define NSLog(format, ...)
#endif

#import <UIKit/UIKit.h>

@interface AppDelegate : UIResponder <UIApplicationDelegate>

@property (strong, nonatomic) UIWindow *window;

@end
