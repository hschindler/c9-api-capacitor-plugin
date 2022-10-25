import { registerPlugin } from '@capacitor/core';
const C9ApiCapacitor = registerPlugin('C9ApiCapacitorPlugin', {
    web: () => import('./web').then(m => new m.C9ApiCapacitorPluginWeb()),
});
export * from './definitions';
export { C9ApiCapacitor };
//# sourceMappingURL=index.js.map