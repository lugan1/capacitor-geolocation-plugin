export interface SampleGeolocationPlugin {
  requestPermission():Promise<{result:String}>;
  requestPsermissionMap():Promise<{result:String}>;
  gpsStart():Promise<{result:String}>;
  gpsStop():Promise<{result:String}>;
  openGoogleMap():Promise<{result:String}>;
}
