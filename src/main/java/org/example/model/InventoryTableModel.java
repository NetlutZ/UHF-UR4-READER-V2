package org.example.model;

import com.rscja.deviceapi.entity.UHFTAGInfo;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.table.AbstractTableModel;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class InventoryTableModel extends AbstractTableModel {

    private List<UHFTAGInfo> uhftagInfoList = new ArrayList<>();
    private int total = 0;
    public String[] columnNames = {"INDEX", "EPC", "TID", "USER", "RSSI", "Count", "Ant"};
    String fileName = "";
    String URL;
    Properties properties = new Properties();
    private HashMap<String, LocalDateTime> EPCScan = new HashMap<String, LocalDateTime>();
    public InventoryTableModel() {
    }

    public void addData(UHFTAGInfo info) {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            fileName = "./date.txt";
        } else {
            fileName = "/home/pi/date.txt";
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String currentDateTime = dtf.format(now);

        try (InputStream input = new FileInputStream("src/main/resources/config.properties")) {
            properties.load(input);
            URL = properties.getProperty("url");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(info.getEPC() + " " + currentDateTime + "\n");
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }

        System.out.println(info.getEPC());
        total++;
        boolean[] exists = new boolean[1];
        int index = getInsertIndex(uhftagInfoList, info, exists);


        if (exists[0]) {
            Duration duration = Duration.between(EPCScan.get(info.getEPC()), now);
            long seconds = duration.getSeconds();
            if(seconds > 60){
                System.out.println(EPCScan.get(info.getEPC()) + " Edited");
                EPCScan.put(info.getEPC(), now);
                updateDevice(info.getEPC(), currentDateTime);
            }
            System.out.println(EPCScan.get(info.getEPC()));

            /*
            UHFTAGInfo temp = uhftagInfoList.get(index);
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(URL + "/device"+"?rfid="+temp.getEPC())
                    .get()
                    .build();
            try (Response response = client.newCall(request).execute()) {
                String jsonResponse = response.body().string().replaceAll("\\},\\{", "},\n{");
                // System.out.println("Response: \n" + jsonResponse);

                if(jsonResponse.equals("[]")){

                }else{
                    JSONArray jsonArray = new JSONArray(jsonResponse);

                    for(int i=0;i<jsonArray.length();i++){      // for duplicate rfid tag
                        JSONObject obj = jsonArray.getJSONObject(i);

                        String statusString = obj.getString("rfidStatus");
                        int deviceId = obj.getInt("id");

                        if(!statusString.equals("Borrowed")){
                            OkHttpClient client2 = new OkHttpClient();
                            RequestBody formbody = new FormBody.Builder()
                                    .add("rfidStatus", "InStorage")
                                    .add("lastScan", currentDateTime)
                                    .build();
                            Request request2 = new Request.Builder()
                                    .url(URL + "/device/" + deviceId)
                                    .put(formbody)
                                    .build();
                            try (Response response2 = client2.newCall(request2).execute()) {
                                // System.out.println("Response Code: " + response2.code());
                                // System.out.println("Response: " + response2.body().string());

                            } catch (Exception e) {
                                System.out.println(e);
                            }

                        }
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
             */


        } else {
            uhftagInfoList.add(index, info);
            EPCScan.put(info.getEPC(), now);
            updateDevice(info.getEPC(), currentDateTime);
        }
    }

    private void updateDevice(String rfid, String currentDateTime) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(URL + "/device"+"?rfid="+rfid)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            String jsonResponse = response.body().string().replaceAll("\\},\\{", "},\n{");
            // System.out.println("Response: \n" + jsonResponse);

            if(jsonResponse.equals("[]")){

            }else{
                JSONArray jsonArray = new JSONArray(jsonResponse);

                for(int i=0;i<jsonArray.length();i++){      // for duplicate rfid tag
                    JSONObject obj = jsonArray.getJSONObject(i);

                    String statusString = obj.getString("rfidStatus");
                    int deviceId = obj.getInt("id");

                    if(!statusString.equals("Borrowed")){
                        OkHttpClient client2 = new OkHttpClient();
                        RequestBody formbody = new FormBody.Builder()
                                .add("rfidStatus", "InStorage")
                                .add("lastScan", currentDateTime)
                                .build();
                        Request request2 = new Request.Builder()
                                .url(URL + "/device/" + deviceId)
                                .put(formbody)
                                .build();
                        try (Response response2 = client2.newCall(request2).execute()) {
                            // System.out.println("Response Code: " + response2.code());
                            // System.out.println("Response: " + response2.body().string());

                        } catch (Exception e) {
                            System.out.println(e);
                        }

                    }
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getTagCount() {
        return uhftagInfoList.size();
    }

    public int getTotal() {
        return total;
    }

    @Override
    public int getRowCount() {
        // TODO Auto-generated method stub
        if (uhftagInfoList != null) {
            return uhftagInfoList.size();
        }
        return 0;
    }


    @Override
    public int getColumnCount() {
        // TODO Auto-generated method stub
        return columnNames.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        // TODO Auto-generated method stub
        return columnNames[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (uhftagInfoList != null) {
            UHFTAGInfo uhftagInfo = uhftagInfoList.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return String.valueOf(rowIndex + 1);
                case 1:
                    return uhftagInfo.getEPC();
                case 2:
                    return uhftagInfo.getTid();
                case 3:
                    return uhftagInfo.getUser();
                case 4:
                    return uhftagInfo.getRssi();
                case 5:
                    return uhftagInfo.getCount();
                case 6:
                    return uhftagInfo.getAnt();
                default:
                    break;
            }
        }
        return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // TODO Auto-generated method stub
        if (columnIndex == 1) {
            return true;
        }
        return super.isCellEditable(rowIndex, columnIndex);
    }


    public void clear() {
        uhftagInfoList.clear();
        total = 0;
    }


    public static int getInsertIndex(List<UHFTAGInfo> listData, UHFTAGInfo newInfo, boolean[] exists) {
        int startIndex = 0;
        int endIndex = listData.size();
        int judgeIndex;
        int ret;
        if (endIndex == 0) {
            exists[0] = false;
            return 0;
        }
        endIndex--;
        while (true) {
            judgeIndex = (startIndex + endIndex) / 2;    //start search from the middle index of the list
            ret = compareBytes(newInfo.getEpcBytes(), listData.get(judgeIndex).getEpcBytes());
            // binary search to find the insert index
            if (ret > 0) {
                if (judgeIndex == endIndex) {
                    exists[0] = false;
                    return judgeIndex + 1;    // search to last index if not found then insert at last
                }
                startIndex = judgeIndex + 1;    //if the newInfo is bigger than the middle index, then search the right half of the list
            } else if (ret < 0) {
                if (judgeIndex == startIndex) {
                    exists[0] = false;
                    return judgeIndex;    // search to first if not found then insert at index 0
                }
                endIndex = judgeIndex - 1; //if the newInfo is smaller than the middle index, then search the left half of the list
            } else {
                exists[0] = true;
                return judgeIndex;
            }
        }
    }

    //return 1,2 b1>b2
    //return -1,-2 b1<b2
    //retrurn 0;b1 == b2
    private static int compareBytes(byte[] b1, byte[] b2) {
        int len = b1.length < b2.length ? b1.length : b2.length;
        int value1;
        int value2;
        for (int i = 0; i < len; i++) {
            value1 = b1[i] & 0xFF;
            value2 = b2[i] & 0xFF;
            if (value1 > value2) {
                return 1;
            } else if (value1 < value2) {
                return -1;
            }
        }
        if (b1.length > b2.length) {
            return 2;
        } else if (b1.length < b2.length) {
            return -2;
        } else {
            return 0;
        }
    }
}
