import { registerPlugin } from '@capacitor/core';

import type { C9ApiCapacitorPlugin } from './definitions';

const C9ApiCapacitor = registerPlugin<C9ApiCapacitorPlugin>('C9ApiCapacitorPlugin', {
  web: () => import('./web').then(m => new m.C9ApiCapacitorPluginWeb()),
});

export * from './definitions';
export { C9ApiCapacitor };
