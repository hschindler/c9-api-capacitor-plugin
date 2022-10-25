import { WebPlugin } from '@capacitor/core';

import type { C9ApiCapacitorPlugin } from './definitions';

export class C9ApiCapacitorPluginWeb extends WebPlugin implements C9ApiCapacitorPlugin {
    async echo(options: { value: string }): Promise<{ value: string }> {
        console.log('ECHO', options);
        return options;
    }

    async getFirmware(): Promise<{ firmware: number[] }> {
        // logic here
        this.throwUnimplementedError();
    }

    async startInventory(): Promise<{ uhfData: string[] }> {
        // logic here
        this.throwUnimplementedError();
    }

    async stopInventory(): Promise<boolean> {
        // logic here
        this.throwUnimplementedError();
    }

    async setOutputPower(): Promise<number> {
        // logic here
        this.throwUnimplementedError();
    }

    async scanBarcode(): Promise<{ barcodeData: string }> {
        // logic here
        this.throwUnimplementedError();
    }

    private throwUnimplementedError(): never {
        throw this.unimplemented('Not implemented on web.');
    }


}
