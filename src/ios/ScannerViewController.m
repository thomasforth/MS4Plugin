//
//  ScannerViewController.m
//  PGNewProj
//
//  Created by Thomas Forth on 02/04/2015.
//
//

#import "ScannerViewController.h"

// these are just default scan types and settings that can be changed later on initialisation
static int kMSResultTypes = MSResultTypeImage  |
                            MSResultTypeQRCode |
                            MSResultTypeEAN13;
static bool noPartials = NO;
static bool smallTarget = NO;

@interface ScannerViewController () <MSAutoScannerSessionDelegate, MSManualScannerSessionDelegate>
@property (weak) IBOutlet UIView *videoPreview;
@end
@implementation ScannerViewController {
    MSAutoScannerSession *_autoScannerSession;
    MSManualScannerSession *_manualScannerSession;
}

// scanType is stored as a global
NSString *scanType;

-(id)initWithScanType:(NSString *)desiredScanType andFormats:(int) scanFormats noPartials:(bool) partialsFlag smallTarget:(bool) smallTargetFlag
{
    self = [super init];
    if (self) {
        scanType = desiredScanType;
        kMSResultTypes = scanFormats;
        noPartials = partialsFlag;
        smallTarget = smallTargetFlag;
    }
    return self;
}

-  (void)viewDidLoad {
    [super viewDidLoad];
    
    // Set camera view to fill screen on devices with larger screens
    [[self view] setFrame:[[UIScreen mainScreen] bounds]];
    
    // subscribe to state change messages
    [[NSNotificationCenter defaultCenter]
     addObserver:self
     selector:@selector(useNotificationToChangeState:)
     name:@"changeStateNotification"
     object:nil];
    
    NSLog(@"ScannerView Loaded");
    
    if ([scanType  isEqual: @"auto"]) {
        _autoScannerSession = [[MSAutoScannerSession alloc] initWithScanner:_scanner];
        _autoScannerSession.delegate = self;
        _autoScannerSession.resultTypes = kMSResultTypes;
        if (noPartials) {
            NSLog(@"No partial flag enabled");
            _autoScannerSession.searchOptions = MSSearchNoPartial;
        }
        if (smallTarget) {
            NSLog(@"Smalltarget support enabled");
            _autoScannerSession.searchOptions = MSSearchSmallTarget;
        }
        
        CALayer *videoPreviewLayer = [self.videoPreview layer];
        [videoPreviewLayer setMasksToBounds:YES];
    
        CALayer *captureLayer = [_autoScannerSession captureLayer];
        [captureLayer setFrame:[self.videoPreview bounds]];
    
        [videoPreviewLayer insertSublayer:captureLayer
                                below:[[videoPreviewLayer sublayers] objectAtIndex:0]];
    
        [_autoScannerSession startRunning];
    }
    else if ([scanType  isEqual: @"manual"]) {
        _manualScannerSession = [[MSManualScannerSession alloc] initWithScanner:_scanner];
        _manualScannerSession.delegate = self;
        _manualScannerSession.resultTypes = kMSResultTypes;
        _manualScannerSession.wantsQuery = NO;
        CALayer *videoPreviewLayer = [self.videoPreview layer];
        [videoPreviewLayer setMasksToBounds:YES];
        
        CALayer *captureLayer = [_manualScannerSession captureLayer];
        [captureLayer setFrame:[self.videoPreview bounds]];
        
        [videoPreviewLayer insertSublayer:captureLayer
                                    below:[[videoPreviewLayer sublayers] objectAtIndex:0]];
        
        [_manualScannerSession startRunning];
    }
}

// Handle orientation changes
- (void)viewWillLayoutSubviews
{
    [self updateInterfaceOrientation:self.interfaceOrientation];
}

- (void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)orientation duration:(NSTimeInterval)duration
{
    [super willAnimateRotationToInterfaceOrientation:orientation duration:duration];
    [self updateInterfaceOrientation:orientation];
}

- (void)updateInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    AVCaptureVideoPreviewLayer *captureLayer;
    if ([scanType  isEqual: @"auto"]) {
        [_autoScannerSession setInterfaceOrientation:interfaceOrientation];
        captureLayer = (AVCaptureVideoPreviewLayer *)[_autoScannerSession captureLayer];
        captureLayer.frame = self.view.bounds;
        // AVCapture orientation is the same as UIInterfaceOrientation
        switch (interfaceOrientation) {
            case UIInterfaceOrientationPortrait:
                [[captureLayer connection] setVideoOrientation:AVCaptureVideoOrientationPortrait];
                break;
            case UIInterfaceOrientationPortraitUpsideDown:
                [[captureLayer connection] setVideoOrientation:AVCaptureVideoOrientationPortraitUpsideDown];
                break;
            case UIInterfaceOrientationLandscapeLeft:
                [[captureLayer connection] setVideoOrientation:AVCaptureVideoOrientationLandscapeLeft];
                break;
            case UIInterfaceOrientationLandscapeRight:
                [[captureLayer connection] setVideoOrientation:AVCaptureVideoOrientationLandscapeRight];
                break;
            default:
                break;
        }
    }
    else if ([scanType  isEqual: @"manual"]) {
        [_manualScannerSession setInterfaceOrientation:interfaceOrientation];
        captureLayer = (AVCaptureVideoPreviewLayer *)[_manualScannerSession captureLayer];
        captureLayer.frame = self.view.bounds;
        // AVCapture orientation is the same as UIInterfaceOrientation
        switch (interfaceOrientation) {
            case UIInterfaceOrientationPortrait:
                [[captureLayer connection] setVideoOrientation:AVCaptureVideoOrientationPortrait];
                break;
            case UIInterfaceOrientationPortraitUpsideDown:
                [[captureLayer connection] setVideoOrientation:AVCaptureVideoOrientationPortraitUpsideDown];
                break;
            case UIInterfaceOrientationLandscapeLeft:
                [[captureLayer connection] setVideoOrientation:AVCaptureVideoOrientationLandscapeLeft];
                break;
            case UIInterfaceOrientationLandscapeRight:
                [[captureLayer connection] setVideoOrientation:AVCaptureVideoOrientationLandscapeRight];
                break;
            default:
                break;
        }
    }
}


- (void)useNotificationToChangeState:(NSNotification *)scanAction
{
    NSDictionary *dictionary = [scanAction userInfo];
    NSString *action = [dictionary valueForKey:@"action"];
    if ([action isEqual: @"pause"]){
        [self pause];
    }
    if ([action isEqual: @"resume"]){
        [self resume];
    }
    if ([action isEqual: @"tapToScan"]){
        [self tapToScan];
    }
    //CDVPluginResult* scanPluginResult = nil;
    //scanPluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:value];
    //[self.commandDelegate sendPluginResult:scanPluginResult callbackId:scanCommand.callbackId];
}


- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

// - (void)postNotificationOfScanResult:(NSString *)value foundResult:(BOOL)gotResult imageAsString:(NSString*)imageAsString

- (void)postNotificationOfScanResult:(NSString *)value format:(NSString *)format recognisedFrame:(NSString*)imageAsString
{
    // format will be "false" if no result was found
    NSDictionary *dictionary = [NSDictionary
                                dictionaryWithObjects:@[value, format, imageAsString]
                                forKeys:@[@"value", @"format", @"recognisedFrame"]];
    [[NSNotificationCenter defaultCenter] postNotificationName:@"scanResultNotification" object:nil userInfo:dictionary];
}



- (void)session:(id)scannerSession didFindResult:(MSResult *)result
{
    NSLog(@"Found result");
    NSString *resultType = [result type] == MSResultTypeImage ? @"Image" : @"Barcode";
    [self postNotificationOfScanResult:[result string] format:resultType recognisedFrame:@""];
}

/*
- (void)session:(id)scannerSession didFindResult:(MSResult *)result forVideoFrame:(UIImage *)videoFrame
{
    NSLog(@"Found result with queryFrame");
    NSString *imageAsString = [UIImagePNGRepresentation(videoFrame) base64EncodedStringWithOptions:NSDataBase64Encoding64CharacterLineLength];
    [self postNotificationOfScanResult:[result string] format:@"true" recognisedFrame:imageAsString];
}
*/
- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
    [_autoScannerSession resumeProcessing];
}

- (void)pause {
    if ([scanType  isEqual: @"manual"]) {
        [_manualScannerSession pauseProcessing];
    }
    else if ([scanType  isEqual: @"auto"]) {
        [_autoScannerSession pauseProcessing];
    }
}

- (void)resume {
    if ([scanType  isEqual: @"manual"]) {
        [_manualScannerSession resumeProcessing];
    }
    else if ([scanType  isEqual: @"auto"]) {
        [_autoScannerSession resumeProcessing];
    }
}

- (void)tapToScan
{
    if ([scanType  isEqual: @"manual"]) {
        [_manualScannerSession snap];
    }
    else {
        NSLog(@"Can't tapToScan except with a manual scanner!");
    }
}

// needed by tapToScan
- (void)sessionWillStartServerRequest:(id)scannerSession
{
    // show that scanning is happening
}

 // manual Scan result - optionalQuery is empty unless requested
- (void)session:(id)scannerSession didFindResult:(MSResult *)result optionalQuery:(UIImage *)query
{
    if (result && query){
        NSString *imageAsString = [UIImagePNGRepresentation(query) base64EncodedStringWithOptions:NSDataBase64Encoding64CharacterLineLength];
        NSString *resultType = [result type] == MSResultTypeImage ? @"Image" : @"Barcode";
        [self postNotificationOfScanResult:[result string] format:resultType recognisedFrame:imageAsString];
    }
    else if (result) {
        NSString *resultType = [result type] == MSResultTypeImage ? @"Image" : @"Barcode";
        [self postNotificationOfScanResult:[result string] format:resultType recognisedFrame:@""];
    }
    else {
        [self postNotificationOfScanResult:@"" format:@"false" recognisedFrame:@""];
    }
    
    // the manual scanner stops scanning when a result is found. That makes no sense, so I restart it.
    [_manualScannerSession resumeProcessing];
}


- (void)dealloc
{
    NSLog(@"ScannerViewController dealloc'ed");
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [_autoScannerSession stopRunning];
    [_manualScannerSession stopRunning];
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
