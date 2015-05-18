//
//  MS4Plugin.h
//  PGNewProj
//
//  Created by Thomas Forth on 02/04/2015.
//
//

#import <Cordova/CDV.h>
#import <Moodstocks/Moodstocks.h>

@interface MS4Plugin: CDVPlugin

@property MSScanner *_scanner;

- (void)useNotificationOfScanResult:(NSNotification*)scanResult;
- (void)postNotificationToChangeState:(NSString *)scanAction;

- (void)testMessage:(CDVInvokedUrlCommand*)command;
- (void)startScan:(CDVInvokedUrlCommand*)command;
- (void)stopScan:(CDVInvokedUrlCommand*)command;
- (void)pauseScan:(CDVInvokedUrlCommand*)command;
- (void)resumeScan:(CDVInvokedUrlCommand*)command;
- (void)tapToScan:(CDVInvokedUrlCommand*)command;
@end
