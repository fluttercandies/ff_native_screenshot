#import "FfNativeScreenshotPlugin.h"
static FLTNativeScreenshotApi *nativeScreenshotApi;
@implementation FfNativeScreenshotPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    FfNativeScreenshotPlugin* instance = [[FfNativeScreenshotPlugin alloc] init];
    FLTFlutterScreenshotApiSetup(registrar.messenger,instance);
    nativeScreenshotApi = [[FLTNativeScreenshotApi alloc] initWithBinaryMessenger: registrar.messenger ];
}


- (void)takeScreenshotWithCompletion:(nonnull void (^)(FlutterStandardTypedData * _Nullable, FlutterError * _Nullable))completion {
    FlutterStandardTypedData *data = [self takeScreenshot];
    completion(data,nil);
}

- (void)startListeningScreenshotWithError:(FlutterError * _Nullable __autoreleasing * _Nonnull)error {
    [[NSNotificationCenter defaultCenter] addObserver: self
                                             selector:@selector(onTakeScreenShoot:)
                                                 name:UIApplicationUserDidTakeScreenshotNotification object:nil];
}


- (void)stopListeningScreenshotWithError:(FlutterError * _Nullable __autoreleasing * _Nonnull)error{
    [[NSNotificationCenter defaultCenter] removeObserver: self name:UIApplicationUserDidTakeScreenshotNotification object:nil];
}

- (void)onTakeScreenShoot:(NSNotification *)notification{
    
    FlutterStandardTypedData *data = [self takeScreenshot];
    [nativeScreenshotApi onTakeScreenshotData:data completion:^(NSError * _Nullable error) {
        
    }];
}

- (FlutterStandardTypedData *)takeScreenshot {
    @try {
        UIApplication *app=  [UIApplication sharedApplication];
        UIView *view= [[[[app delegate] window] rootViewController] view];
        
        UIGraphicsBeginImageContextWithOptions([view bounds].size,[view isOpaque],[UIScreen mainScreen].scale);
        
        [view drawViewHierarchyInRect:view.bounds afterScreenUpdates:TRUE];
        
        UIImage *image= UIGraphicsGetImageFromCurrentImageContext();
        
        UIGraphicsEndImageContext();
        
        
        NSData *imageData= UIImageJPEGRepresentation(image, 1.0f);
        FlutterStandardTypedData *data  = [FlutterStandardTypedData typedDataWithBytes:imageData];
        return  data;
        
        
    } @catch (NSException *exception) {
        NSLog(@"%@",exception);
        return nil;
        
    } @finally {
        return nil;
    }

}

@end
