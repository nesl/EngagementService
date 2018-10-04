//
//  DeviceStatus.swift
//  SoundSwitch
//
//  Created by Siyou Pei on 2018/9/27.
//  Copyright © 2018 Moshe Gottlieb. All rights reserved.
//


import Foundation

@objc class DeviceStatus : NSObject {
    
    @objc func locked(){
        print("device locked") // Handle Device Locked events here.
        NSLog("locked")
    }
    
    @objc func unlocked(){
        print("device unlocked") //Handle Device Unlocked events here.
        NSLog("unlocked")
    }
}
