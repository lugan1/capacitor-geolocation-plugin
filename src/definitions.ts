import {PluginListenerHandle} from "@capacitor/core";

export interface SampleGeolocationPlugin {
  requestPermission():Promise<{result:String}>;
  requestPsermissionMap():Promise<{result:String}>;
  gpsStart():Promise<{result:String}>;
  gpsStop():Promise<{result:String}>;
  openGoogleMap():Promise<{result:String}>;
  addListener(eventName: 'abnormal_vitalSign', listenerFunc: (value : any) => void): Promise<PluginListenerHandle> & PluginListenerHandle;
}
