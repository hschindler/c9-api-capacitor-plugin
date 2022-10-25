import { WebPlugin } from '@capacitor/core';
import type { C9ApiCapacitorPlugin } from './definitions';
export declare class C9ApiCapacitorPluginWeb extends WebPlugin implements C9ApiCapacitorPlugin {
    echo(options: {
        value: string;
    }): Promise<{
        value: string;
    }>;
    getFirmware(): Promise<{
        firmware: number[];
    }>;
    startInventory(): Promise<{
        uhfData: string[];
    }>;
    stopInventory(): Promise<boolean>;
    setOutputPower(): Promise<number>;
    scanBarcode(): Promise<{
        barcodeData: string;
    }>;
    private throwUnimplementedError;
}
