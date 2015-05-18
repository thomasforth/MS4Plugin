//  MS4Plugin.m
//  PGNewProj
//
//  Created by Thomas Forth on 02/04/2015.
//
//

#import "MS4Plugin.h"
#import <Cordova/CDV.h>
#import "ScannerViewController.h"
#import <Moodstocks/Moodstocks.h>

@implementation MS4Plugin{
    MSScanner *_scanner;
}

// global objects are stored here so they can be responded to later
CDVInvokedUrlCommand *messageCommand;
CDVInvokedUrlCommand *scanCommand;
CDVPluginResult* messagePluginResult;
CDVPluginResult* syncPluginResult;
CDVPluginResult* scanPluginResult;

ScannerViewController *scannerVC;

#define MS_API_KEY    @"ektwqaxtd9aqlr5livjl"
#define MS_API_SECRET @"cXVVtyXufAXv7uLn"

- (void)testMessage:(CDVInvokedUrlCommand*)command
{
    // save the command object (containing the callbackId) for the later
    messageCommand = command;
    
    NSString* message = [[command.arguments objectAtIndex:0] valueForKey:@"message"];
    
    if (message != nil && [message length] > 0) {
        messagePluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:message];
    } else {
        messagePluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }
    
    //[self.commandDelegate sendPluginResult:messagePluginResult callbackId:messageCommand.callbackId];
}

- (void)openScanner:(CDVInvokedUrlCommand*)command
{
    [[UIApplication sharedApplication] setStatusBarHidden:YES animated:NO];
    
    CDVPluginResult* pluginResult = nil;
    
    // open Moodstocks scanner
    NSString *path = [MSScanner cachesPathFor:@"scanner.db"];
    _scanner = [[MSScanner alloc] init];
    [_scanner openWithPath:path key:MS_API_KEY secret:MS_API_SECRET error:nil];
    
    if (_scanner != nil) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    }
    else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)syncScanner:(CDVInvokedUrlCommand*)command
{
    
    // Create the progression and completion blocks:
    void (^completionBlock)(MSSync *, NSError *) = ^(MSSync *op, NSError *error) {
        if (error)
        {
            NSLog(@"Sync failed with error: %@", [error ms_message]);
            syncPluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        }
        else
        {
            NSLog(@"Sync succeeded (%li images(s))", (long)[_scanner count:nil]);
            syncPluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:(int)[_scanner count:nil]];
        }
        [self.commandDelegate sendPluginResult:syncPluginResult callbackId:command.callbackId];
    };
    
    void (^progressionBlock)(NSInteger) = ^(NSInteger percent) {
        NSLog(@"Sync progressing: %li%%", (long)percent);
    };
    
    // Launch the synchronization
    [_scanner syncInBackgroundWithBlock:completionBlock progressBlock:progressionBlock];
}

- (void)startScan:(CDVInvokedUrlCommand*)command
{
    // register to receive notifications (which will deliver the result)
    [[NSNotificationCenter defaultCenter]
     addObserver:self
     selector:@selector(useNotificationOfScanResult:)
     name:@"scanResultNotification"
     object:nil];
    
    scanCommand = command; // save scanCommand to the result can be returned.
    
    NSString* scanType = [[command.arguments objectAtIndex:0] valueForKey:@"scanType"];
    int scanFormats = [[[command.arguments objectAtIndex:0] valueForKey:@"scanFormats"] integerValue];
    BOOL useDeviceOrientation = [[[command.arguments objectAtIndex:0] valueForKey:@"useDeviceOrientation"] integerValue];
    BOOL noPartialMatching = [[[command.arguments objectAtIndex:0] valueForKey:@"noPartialMatching"] integerValue];
    BOOL smallTargetSupport = [[[command.arguments objectAtIndex:0] valueForKey:@"smallTargetSupport"] integerValue];
    
    scannerVC = [[ScannerViewController alloc] initWithScanType:scanType
                                                     andFormats:scanFormats
                                                     noPartials:noPartialMatching
                                                    smallTarget:smallTargetSupport];

    scannerVC.scanner = _scanner;
    [self.viewController addChildViewController:scannerVC];
    [self.viewController.view addSubview:scannerVC.view];
    [self.viewController.view sendSubviewToBack:scannerVC.view];
    NSLog(@"ScannerView Added as Child View Controller");    
}

- (void)useNotificationOfScanResult:(NSNotification *)scanResult
{
    NSDictionary *dictionary = [scanResult userInfo];
    //NSString *value = [dictionary valueForKey:@"value"];
    NSString *format = [dictionary valueForKey:@"format"];
    //NSString *recognisedFrame = [dictionary valueForKey:@"imageAsString"];
    
    if([format  isEqual: @"false"]) {
        // special case where no result was found
        scanPluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"No Image Found."];
    }
    else {
        scanPluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dictionary];
    }
    [scanPluginResult setKeepCallback:[NSNumber numberWithBool:YES]];
    [self.commandDelegate sendPluginResult:scanPluginResult callbackId:scanCommand.callbackId];        
}

- (void)postNotificationToChangeState:(NSNotification *)scanAction
{
    NSDictionary *dictionary = [NSDictionary dictionaryWithObject:scanAction forKey:@"action"];
    [[NSNotificationCenter defaultCenter] postNotificationName:@"changeStateNotification" object:nil userInfo:dictionary];
}

- (void)stopScan:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    [scannerVC removeFromParentViewController];
    [scannerVC.view removeFromSuperview];
    NSLog(@"ScannerView removed as Child View Controller");
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)pauseScan:(CDVInvokedUrlCommand*)command
{
    [self postNotificationToChangeState:@"pause"];    
}
- (void)resumeScan:(CDVInvokedUrlCommand*)command
{
    [self postNotificationToChangeState:@"resume"];
}
- (void)tapToScan:(CDVInvokedUrlCommand *)command
{
    [self postNotificationToChangeState:@"tapToScan"];
}
- (void)recognisePhotoString:(CDVInvokedUrlCommand*)command
{
    NSString* photoAsBase64 = [[command.arguments objectAtIndex:0] valueForKey:@"photoString"];
    NSString* pngPrefix = @"data:image/png;base64,";
    NSString* appendedString = [pngPrefix stringByAppendingString:photoAsBase64];
    NSURL *photoURL = [NSURL URLWithString:appendedString];
    NSData *imageData = [NSData dataWithContentsOfURL:photoURL];
    UIImage *image = [UIImage imageWithData:imageData];
    MSImage *img = [MSImage imageWithUIImage:image error:nil];
    MSResult *result = [_scanner searchWithQuery:img options:MSSearchDefault extras:MSResultExtraNone error:nil];
    if (result)
    {
        NSString *resultType = [result type] == MSResultTypeImage ? @"Image" : @"Barcode";
        NSDictionary *resultDictionary = [NSDictionary
                                    dictionaryWithObjects:@[[result string], resultType, @""]
                                    forKeys:@[@"value", @"format", @"recognisedFrame"]];
        scanPluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:resultDictionary];
        [self.commandDelegate sendPluginResult:scanPluginResult callbackId:command.callbackId];
    }
    else
    {
        scanPluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"No Image Found."];
        [self.commandDelegate sendPluginResult:scanPluginResult callbackId:command.callbackId];
    }
}

@end

