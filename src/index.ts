import { registerPlugin } from '@capacitor/core';

import type { SampleGeolocationPlugin } from './definitions';

const SampleGeolocation = registerPlugin<SampleGeolocationPlugin>('SampleGeolocation', {
  web: () => import('./web').then(m => new m.SampleGeolocationWeb()),
});

export * from './definitions';
export { SampleGeolocation };
