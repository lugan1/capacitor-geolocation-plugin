import {WebPlugin} from '@capacitor/core';

import type { SampleGeolocationPlugin } from './definitions';

export class SampleGeolocationWeb extends WebPlugin implements SampleGeolocationPlugin {
  async requestPermission(): Promise<{ result: String }> {
    return new Promise(
        resolve => {
          return resolve;
        }
    );
  }

    gpsStart(): Promise<{ result: String }> {
        return new Promise(
            resolve => {
                return resolve;
            }
        );
    }

    gpsStop(): Promise<{ result: String }> {
        return new Promise(
            resolve => {
                return resolve;
            }
        );
    }

    requestPsermissionMap(): Promise<{ result: String }> {
        return new Promise(
            resolve => {
                return resolve;
            }
        );
    }

    openGoogleMap(): Promise<{ result: String }> {
        return new Promise(
            resolve => {
                return resolve;
            }
        );
    }

}
