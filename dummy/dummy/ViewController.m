//
//  ViewController.m
//  dummy
//
//  Created by Siyou Pei on 2018/9/25.
//  Copyright © 2018 Siyou Pei. All rights reserved.
//

#import "ViewController.h"

@interface ViewController ()

@end

@implementation ViewController

#pragma mark View Lifecycle methods
- (void)viewDidLoad {
    [super viewDidLoad];
    [self setupUIbutton];
    [self setupUIlabel];
    [self beginDetection];
    //[self isMuted];
    // Do any additional setup after loading the view, typically from a nib.
}

#pragma mark - Setup
- (void)setupUIbutton {
    // Hello button.
    UIButton *helloButton = [UIButton buttonWithType:UIButtonTypeSystem];
    [helloButton setTitle:@"Hello" forState:UIControlStateNormal];
    [helloButton addTarget:self action:@selector(onHelloButtonClicked:) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:helloButton];
    //// Layout with frame.
    //[helloButton setFrame:CGRectMake(0, 0, 60, 40)];
    //helloButton.center = self.view.center;
    //// Layout with constraint.
    helloButton.translatesAutoresizingMaskIntoConstraints = NO; // If you want to use Auto Layout to dynamically calculate the size and position of your view, you must set this property to NO.
    [self.view addConstraints:@[
                                [NSLayoutConstraint constraintWithItem:helloButton attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:60.0],
                                [NSLayoutConstraint constraintWithItem:helloButton attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:40.0],
                                [NSLayoutConstraint constraintWithItem:helloButton attribute:NSLayoutAttributeCenterX relatedBy:NSLayoutRelationEqual toItem:self.view attribute:NSLayoutAttributeCenterX multiplier:1.0 constant:0],
                                [NSLayoutConstraint constraintWithItem:helloButton attribute:NSLayoutAttributeCenterY relatedBy:NSLayoutRelationEqual toItem:self.view attribute:NSLayoutAttributeCenterY multiplier:1.0 constant:0]
                                ]];
}

#pragma mark - Action
- (void)onHelloButtonClicked:(id)sender {
    NSLog(@"Hello, world!");
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"Hello" message:@"Hello, world!" preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"Cancel" style:UIAlertActionStyleCancel handler:^(UIAlertAction *action) {
        NSLog(@"Cancle Action");
    }];
    UIAlertAction *okAction = [UIAlertAction actionWithTitle:@"OK" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        NSLog(@"OK Action");
    }];
    [alertController addAction:cancelAction];
    [alertController addAction:okAction];
    [self presentViewController:alertController animated:YES completion:nil];
}


- (void)setupUIlabel{
    NSDate *date=[NSDate date];//获取当前时间
    NSDateFormatter *format1=[[NSDateFormatter alloc]init];
    [format1 setDateFormat:@"yyyy/MM/dd HH:mm:ss"];
    NSString *str1=[format1 stringFromDate:date];
    UILabel *label = [[UILabel alloc]init];
    label.frame = CGRectMake(20,40,280,40);
    label.text = str1;
    NSLog(@"The code runs through here!");
    [self.view addSubview:label];
    
}

#pragma mark Timer methods
- (void)beginDetection {
    [[RBDMuteSwitch sharedInstance] setDelegate:self];
    [[RBDMuteSwitch sharedInstance] detectMuteSwitch];
}




#pragma mark RBDMuteSwitchDelegate methods
- (void)isMuted:(BOOL)muted {
    UILabel *muteLabel = [[UILabel alloc]init];
    muteLabel.frame = CGRectMake(50,100,280,40);
    //(origin.x，origin.y，size.width. Size.height)
    if (muted) {
        NSLog(@"Muted");
        muteLabel.text = @"Muted";
    }
    else {
        NSLog(@"Not Muted");
        muteLabel.text = @"Not Muted";
    }
    [self.view addSubview:muteLabel];
}


@end
