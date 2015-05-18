//
//  ScannerViewController.h
//  PGNewProj
//
//  Created by Thomas Forth on 02/04/2015.
//
//

#import <UIKit/UIKit.h>
#import <Moodstocks/Moodstocks.h>
#import "MS4Plugin.h"

@interface ScannerViewController : UIViewController

@property MSScanner *scanner;
@property (nonatomic,strong) MS4Plugin *msfourplugin;
- (void)postNotificationOfScanResult:(NSString *)scanResult;
- (void)useNotificationToChangeState:(NSNotification *)scanAction;
- (id)initWithScanType:(NSString *)desiredScanType andFormats:(int) scanFormats noPartials:(bool) partialsFlag smallTarget:(bool) smallTargetFlag;
@end
