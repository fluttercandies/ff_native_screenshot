import {
  FlutterPlugin,
  FlutterPluginBinding
} from '@ohos/flutter_ohos/src/main/ets/embedding/engine/plugins/FlutterPlugin';
import { Result, ScreenshotFlutterApi, ScreenshotHostApi } from './ScreenshotApi';
import window from '@ohos.window';
import { BusinessError } from '@ohos.base';
import image from '@ohos.multimedia.image';
import AbilityAware from '@ohos/flutter_ohos/src/main/ets/embedding/engine/plugins/ability/AbilityAware';
import {
  AbilityPluginBinding
} from '@ohos/flutter_ohos/src/main/ets/embedding/engine/plugins/ability/AbilityPluginBinding';
import FlutterManager from '@ohos/flutter_ohos/src/main/ets/embedding/ohos/FlutterManager';


/** FfNativeScreenshotPlugin **/
export default class FfNativeScreenshotPlugin extends ScreenshotHostApi implements FlutterPlugin, AbilityAware {
  private static screenshotFlutterApi: ScreenshotFlutterApi | null = null;
  private static  mainWindow: window.Window | null = null;

  takeScreenshot(result: Result<number[]>): void {
    FfNativeScreenshotPlugin.TakeScreenshot(result);
  }

  startListeningScreenshot(): void {
    if (FfNativeScreenshotPlugin.mainWindow != null) {
      FfNativeScreenshotPlugin.mainWindow.on('screenshot', FfNativeScreenshotPlugin.onTakeScreenshot);
    }
  }

  stopListeningScreenshot(): void {
    if (FfNativeScreenshotPlugin.mainWindow != null) {
      FfNativeScreenshotPlugin.mainWindow.off('screenshot', FfNativeScreenshotPlugin.onTakeScreenshot);
    }
  }

  private static onTakeScreenshot(): void {
    class ResultImp implements Result<number[]> {
      success(result: number[]): void {
        if (FfNativeScreenshotPlugin.screenshotFlutterApi != null) {
          FfNativeScreenshotPlugin.screenshotFlutterApi.onTakeScreenshot(result, {
            reply: (reply) => {
              // console.log(reply.message);
            }
          });
        }
      }

      error(error: Error): void {

      }
    }
    FfNativeScreenshotPlugin.TakeScreenshot(new ResultImp());
  }

  private static TakeScreenshot(result: Result<number[]>): void {
    if (FfNativeScreenshotPlugin.mainWindow != null) {
      FfNativeScreenshotPlugin.mainWindow.snapshot(async (err: BusinessError, pixelMap: image.PixelMap) => {
        const errCode: number = err.code;
        if (errCode) {
          console.error('Failed to snapshot window. Cause:' + JSON.stringify(err));
          result.error(err);
          return;
        }
        const imagePackerApi = image.createImagePacker();
        const packOptions: image.PackingOption = {
          format: 'image/jpeg',
          quality: 100
        }
        const imageData = await imagePackerApi.packing(pixelMap, packOptions);
        let uint8Array = new Uint8Array(imageData);
        result.success(Array.from(uint8Array));
      })
    }
  }

  getUniqueClassName(): string {
    return "FfNativeScreenshotPlugin"
  }

  onAttachedToEngine(binding: FlutterPluginBinding): void {
    ScreenshotHostApi.setup(binding.getBinaryMessenger(), this);
    FfNativeScreenshotPlugin.screenshotFlutterApi = new ScreenshotFlutterApi(binding.getBinaryMessenger());
  }

  onDetachedFromEngine(binding: FlutterPluginBinding): void {
    ScreenshotHostApi.setup(binding.getBinaryMessenger(), null);
    FfNativeScreenshotPlugin.screenshotFlutterApi = null;
  }

  onAttachedToAbility(binding: AbilityPluginBinding): void {
    FfNativeScreenshotPlugin.mainWindow = FlutterManager.getInstance().getWindowStage(binding.getAbility()).getMainWindowSync();
  }

  onDetachedFromAbility(): void {

  }
}

