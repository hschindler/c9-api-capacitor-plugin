package de.schindlergmbh.plugins.capacitor.c9api;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;

// import com.handheld.UHF.UhfManager;
import com.android.hdhe.uhf.reader.UhfReader;
import com.android.hdhe.uhf.readerInterface.TagModel;

import android.util.Log;

@CapacitorPlugin(name = "C9ApiCapacitorPlugin")
public class C9ApiCapacitorPlugin extends Plugin {

    private static final String TAG = C9ApiCapacitorPlugin.class.getName();

    private UhfReader _uhfManager;

    private boolean _barcodeInitFlag = false;
    private boolean _initRuns = false;

    private ArrayList<String> _listepc = new ArrayList<String>();
    private ArrayList<String> _listTID = new ArrayList<String>();
    // private ArrayList<EPC> _listEPCObject;

    private boolean startFlag = false;

    private String _errorLog;

    private int _uhfPort = 13;

    // private int _barcodePort = 0;
    // private int _barcodePower = SerialPort.Power_Scaner;
    // private int _barcodeBaudrate = 9600;

    private String _readMode = "tid"; // tid / epc
    private int _outputPower = 0;

    private Thread _scanThread;

    // TODO: how can i use onDestroy with capacitor?
    // @Override
    // public void onDestroy() {
    // super.onDestroy();

    // Log.d(TAG, "onDestroy C9 plugin");

    // this.StopInventoryThread();

    // this.disposeUHFManager();

    // this.closeBarcodeManager();
    // }

    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", value);
        call.resolve(ret);
    }

    @PluginMethod()
    public void getFirmware(PluginCall call) {

        Log.d(TAG, "getFirmware");

        this.initializeUHFManager();

        if (_uhfManager == null) {
            call.reject("UHF API not installed");
            return;
        }

        // final byte[] firmwareVersion = _uhfManager.getFirmware();
        byte[] firmwareVersion = _uhfManager.getFirmware();

        if (firmwareVersion != null) {
            Log.d(TAG, "firmwareVersion");
            Log.d(TAG, String.valueOf(firmwareVersion.length));
            Log.d(TAG, String.valueOf(firmwareVersion[0]));
            Log.d(TAG, String.valueOf(firmwareVersion[1]));
        } else {
            firmwareVersion = "test".getBytes();
        }

        this.disposeUHFManager();

        JSObject ret = new JSObject();
        ret.put("firmware", firmwareVersion);
        call.resolve(ret);
    }

    @PluginMethod()
    public void startInventory(PluginCall call) {

        Log.d(TAG, "startInventory");

        String value = call.getString("value", "tid");

        Log.d(TAG, "startInventory value=" + value);

        if (value.equals("tid") || value.equals("epc")) {
            this._readMode = value;
        }

        saveCall(call);

        this.StartInventoryThread();

    }

    @PluginMethod()
    public void stopInventory(PluginCall call) {

        Boolean result = true;

        this.StopInventoryThread();

        JSObject ret = new JSObject();
        ret.put("value", result);
        call.resolve(ret);
    }

    @PluginMethod()
    public void setOutputPower(PluginCall call) {
        // 0-30
        Integer value = call.getInt("value", 30);

        if (value != null) {
            Log.d(TAG, "Power value = " + value);
            this._outputPower = value;
            Log.d(TAG, "outputPower value = " + Integer.valueOf(this._outputPower).toString());
        } else {
            Log.d(TAG, "Power value = null");
        }

        JSObject ret = new JSObject();
        ret.put("value", this._outputPower);
        call.resolve(ret);
    }

    private void initializeUHFManager() {

        Log.d(TAG, "initializeUHFManager C9ApiCapacitorPlugin");

        if (this._uhfManager == null) {
            /*
             * UhfReader.Port = _uhfPort;
             * UhfReader.BaudRate = 115200;
             * UhfReader.Power = SerialPort.Power_Rfid;
             */

            try {
                this._uhfManager = UhfReader.getInstance();
                // Thread.sleep ist hier wichtig!!! Sonst scannt der Scanner deutlich schlechter.
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (this._uhfManager != null) {
                    Log.d(TAG, "initializeUHFManager C9ApiCapacitorPlugin successful");
                } else {
                    Log.d(TAG, "initializeUHFManager C9ApiCapacitorPlugin failed");
                }

                if (_uhfManager != null && this._outputPower > 0) {
                    boolean result = _uhfManager.setOutputPower(this._outputPower);
                }

                if (_uhfManager != null) {
                    int result = _uhfManager.setWorkArea(3);
                    _uhfManager.setSensitivity(3);
                    if (result == 0) {
                        Log.d(TAG, "initializeUHFManager - set working area successful");
                        Log.d(TAG, "initializeUHFManager - frequency = " + _uhfManager.getFrequency());
                    }

                }

            } catch (Exception e) {
                _errorLog = e.getMessage();
                e.printStackTrace();
                // Log.d(TAG, "Error: " + e.getMessage());
            }
        } else {
            _uhfManager.setOutputPower(this._outputPower);
            _uhfManager.setWorkArea(3);
        }
    }

    private void disposeUHFManager() {

        if (this._uhfManager != null) {
            Log.d(TAG, "disposeUHFManager");

            try {
                this._uhfManager.close();
            } catch (Exception e) {
                _errorLog = e.getMessage();
            }

            this._uhfManager = null;
        }
    }

    private void StartInventoryThread() {

        Log.d(TAG, "StartInventoryThread");

        // start inventory thread
        startFlag = true;

        if (this._scanThread == null || this._scanThread.getState() == Thread.State.TERMINATED) {
            Log.d(TAG, "StartInventoryThread - create new thread");
            this._scanThread = new InventoryThread();
        }

        Log.d(TAG, "StartInventoryThread - start thread");

        if (this._scanThread.getState() == Thread.State.NEW) {
            this._scanThread.start();
        }

    }

    private void StopInventoryThread() {
        // runFlag = false;
        startFlag = false;
    }

    private void PauseInventoryThread() {
        startFlag = false;
    }

    private JSONArray ConvertArrayList(ArrayList<String> list) {
        org.json.JSONArray jsonArray = new org.json.JSONArray();
        for (String value : list) {
            jsonArray.put(value);
        }

        return jsonArray;
    }

    // add TIDs to view
    private void returnCurrentTIDs(final ArrayList<String> tidList, PluginCall call) {
        if (call != null) {
            if (tidList != null || tidList.isEmpty() == false) {
                Log.d(TAG, "returnCurrentTIDs No. " + tidList.size());
                JSObject ret = new JSObject();
                ret.put("uhfData", ConvertArrayList(tidList));
                call.resolve(ret);

                // PluginResult pluginResult = new PluginResult(PluginResult.Status.OK,
                // ConvertArrayList(tidList));
                // pluginResult.setKeepCallback(true);
                // _uhfCallBackContext.sendPluginResult(pluginResult);
            }

        }
    }

    /**
     * Inventory Thread
     */
    class InventoryThread extends Thread {
        private List<TagModel> epcList;
        private ArrayList<String> dataList;

        @Override
        public void run() {
            super.run();

            Log.d(TAG, "InventoryThread starting...");

            PluginCall savedCall = getSavedCall();
            if (savedCall == null) {
                Log.d("Test", "No stored plugin call for startInventory request result");
                return;
            }

            initializeUHFManager();

            if (_uhfManager == null) {
                Log.d(TAG, "InventoryThread failed creating uhfManager");
                savedCall.reject("InventoryThread failed creating uhfManager");
                return;
            }

            Log.d(TAG, "InventoryThread startflag = " + String.valueOf(startFlag));

            while (startFlag) {

                Log.d(TAG, "Waiting for timeout..");

                if (_uhfManager != null) {

                    epcList = _uhfManager.inventoryRealTime(); // inventory real time

                    if ("tid".equals(_readMode)) {

                        try {

                            if (epcList != null && !epcList.isEmpty()) {
                                // play sound
                                // Util.play(1, 0);
                                dataList = new ArrayList<>();

                                for(TagModel tag:epcList){
                                    if(tag != null){{
                                        if (SelectEPC(tag.getmEpcBytes(), savedCall)) {
                                            byte[] tid = GetTID(savedCall);

                                            if (tid != null) {
                                                String tidStr = Tools.Bytes2HexString(tid, tid.length);
                                                dataList.add(tidStr);
                                            }
                                        }
                                    }
                                        String epcStr = Tools.Bytes2HexString(tag.getmEpcBytes(), tag.getmEpcBytes().length);

                                        dataList.add(epcStr);

                                    }
                                }

                                /*
                                for (byte[] epc : epcList) {

                                    if (SelectEPC(epc, savedCall)) {
                                        byte[] tid = GetTID(savedCall);

                                        if (tid != null) {
                                            String tidStr = Tools.Bytes2HexString(tid, tid.length);
                                            dataList.add(tidStr);
                                        }
                                    }
                                }
                                */
                                if (!dataList.isEmpty()) {
                                    returnCurrentTIDs(dataList, savedCall);
                                    startFlag = false;
                                }

                            }

                        } catch (Exception ex) {
                            Log.e(TAG, "GetTID Exception: " + ex.getMessage());
                            savedCall.reject("Fehler-GetTID: " + ex.getMessage());
                        }

                    } else if ("epc".equals(_readMode)) {

                        try {

                            dataList = new ArrayList<String>();

                            if (epcList != null && !epcList.isEmpty()) {

                                for(TagModel tag:epcList){
                                    if(tag == null){
                                        String epcStr = "";
                                        dataList.add(epcStr);
                                        //addToList(listEPC, epcStr, (byte)-1);
                                    }else{
                                        String epcStr = Tools.Bytes2HexString(tag.getmEpcBytes(), tag.getmEpcBytes().length);

                                        dataList.add(epcStr);

                                    }

                                }
                                /*
                                for (byte[] epc : epcList) {
                                    if (epc != null) {
                                        String epcStr = Tools.Bytes2HexString(epc,
                                                epc.length);

                                        dataList.add(epcStr);
                                    }
                                }
                                */
                                if (!dataList.isEmpty()) {
                                    returnCurrentTIDs(dataList, savedCall);
                                    startFlag = false;
                                }
                            }

                        } catch (Exception ex) {
                            Log.e(TAG, "GetEPC Exception: " + ex.getMessage());
                            savedCall.reject("Fehler-GetEPC: " + ex.getMessage());
                        }

                    }

                } else {
                    // returnCurrentTIDs(null);
                    savedCall.reject("UHFManager is not initialized!");
                }

                epcList = null;

                try {
                    Thread.sleep(40);
                } catch (InterruptedException e) {
                    // Thread.currentThread().interrupt();
                    e.printStackTrace();
                    // return;
                }

                // }
            } // while

            Log.d(TAG, "InventoryThread is closing...");

            disposeUHFManager();

        } // run

        private boolean SelectEPC(byte[] epc, PluginCall call) {
            try {
                if (_uhfManager != null) {
                    _uhfManager.selectEPC(epc);
                }
            } catch (Exception ex) {
                if (call != null) {
                    call.reject("Fehler-SelectEPC: " + ex.getMessage());
                }

                return false;
            }

            return true;
        }

        // first select tag by epc
        private byte[] GetTID(PluginCall call) {
            // Parameters: int memBank store RESEVER zone 0, EPC District 1, TID District 2,
            // USER District 3;
            // int startAddr starting address (not too large, depending on the size of the
            // data area);
            // int length read data length, in units of word (1word = 2bytes); byte []
            // accessPassword password 4 bytes
            int tidLength = 6; // in word 1 word = 2 byte
            // byte[] tid; // = new byte[tidLength*2];

            if (_uhfManager == null) {
                return null;
            }

            Log.d(TAG, "GetTID");

            try {
                byte[] pw = new byte[4];
                byte[] tid = _uhfManager.readFrom6C(2, 0, tidLength, pw);

                if (tid != null && tid.length > 1) {

                    Log.d(TAG, "GetTID - " + tid);
                    return tid;

                } else {
                    if (tid != null) {
                        // tid has error code

                        // try again with small tid (8 byte)
                        tidLength = 4;
                        tid = _uhfManager.readFrom6C(2, 0, tidLength, pw);
                        Log.d(TAG, "GetTID (8yte) - " + tid);

                        if (tid != null && tid.length > 1) {
                            return tid;
                        } else {

                            // tid has error code
                            Log.d(TAG, "GetTID error - " + Tools.Bytes2HexString(tid, tid.length));
                            // if (tid != null) {
                            // call.reject("Fehler-GetTID tid error code: " + Tools.Bytes2HexString(tid,
                            // tid.length));
                            // } else {
                            // call.reject("Fehler-GetTID tid no error code");
                            // }

                            return null;
                        }
                    }
                    return null;
                }

            } catch (Exception ex) {

                call.reject("Fehler-GetTID: " + ex.getMessage());

            }

            return null;
        }
    } // end inventory thread class
}
