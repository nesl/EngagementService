//
//  ViewController.h
//  dummy
//
//  Created by Siyou Pei on 2018/9/25.
//  Copyright © 2018 Siyou Pei. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "RBDMuteSwitch.h"

@interface ViewController : UIViewController <RBDMuteSwitchDelegate> {
    NSTimer* updateTimer;
}

@end

