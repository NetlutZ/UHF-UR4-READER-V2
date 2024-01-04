package org.example;

import com.rscja.deviceapi.RFIDWithUHFNetworkUR4;
import com.rscja.deviceapi.RFIDWithUHFSerialPortUR4;
import com.rscja.deviceapi.interfaces.IUR4;
import org.example.form.InventoryForm;
import org.example.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class Main {

    InventoryForm inventoryForm = new InventoryForm();
    public static IUR4 ur4 = null;
    RFIDWithUHFNetworkUR4 ur4Network = null;
    RFIDWithUHFSerialPortUR4 ur4SerialPort = null;
    String ip = "192.168.99.202";
    String port = "8888";
    Properties properties = new Properties();
    String URL;
    ArrayList<Integer> idList = new ArrayList<Integer>();
    String idString = "";
    int newLossActivityID = 0;
    int lastLossActivityID = 0;

    public static void main(String[] args) {
        System.out.println("Hello world!");
        Main main = new Main();
    }

    public Main() {
        initUR4();
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                System.out.println("Shutdown hook ran!");
                if (ur4 != null) {
                    inventoryForm.stopInventory();
                    ur4.free();
                }
            }
        });

        try (InputStream input = new FileInputStream("src/main/resources/config.properties")) {
            properties.load(input);
            URL = properties.getProperty("url");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        int delay = 0; // delay for x minutes
        int period = 3000; // repeat every x seconds
        java.util.Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();
                String currentDateTime = dtf.format(now);
                System.out.println("Task performed on " + new java.util.Date());

                // Check Device that Loss more than 20 seconds(change second in server)
                try {
                    java.net.URL url = new URL(URL + "/device/check/loss");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    int responseCode = connection.getResponseCode();
                    // System.out.println("Response Code: " + responseCode);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    StringBuffer response = new StringBuffer();

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    //  String jsonResponse = response.toString().replaceAll("\\},\\{", "},\n{");
                    //  System.out.println("Response: \n" + jsonResponse);

                    // create array to keep id of device that loss
                    JSONArray jsonArray = new JSONArray(response.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        int id = obj.getInt("id");
                        System.out.println("ID: " + id);
                        idList.add(id);
                    }

                    for (int i = 0; i < idList.size(); i++) {
                        if (i == idList.size() - 1) {
                            idString += idList.get(i);
                        } else {
                            idString += idList.get(i) + ",";
                        }
                    }

                    // System.out.println("idString: " + idString);
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }


                // Get Latest LossActivityID
                try {
                    URL url2 = new URL(URL + "/activity/lastest/loss");
                    HttpURLConnection connection2 = (HttpURLConnection) url2.openConnection();
                    connection2.setRequestMethod("GET");

                    int responseCode2 = connection2.getResponseCode();
                    // System.out.println("Response Code: " + responseCode2);

                    BufferedReader reader2 = new BufferedReader(new InputStreamReader(connection2.getInputStream()));
                    String line2;
                    StringBuffer response2 = new StringBuffer();

                    while ((line2 = reader2.readLine()) != null) {
                        response2.append(line2);
                    }
                    reader2.close();

                    // System.out.println("Response: " + response.toString());
                    String jsonResponse2 = response2.toString().replaceAll("\\},\\{", "},\n{");

                    if(jsonResponse2.equals("null")){
                        lastLossActivityID = 0;
                    }else{
                        // Get ActivityCode and Cut it to Remain Only Number
                        JSONObject obj = new JSONObject(jsonResponse2);
                        String activityCode = obj.getString("activityCode");
                        String[] splitActivityCode = activityCode.split("L");       //TODO: Can Change
                        lastLossActivityID = Integer.parseInt(splitActivityCode[1]);
                        // System.out.println("lastLossActivityID: " + lastLossActivityID);
                        lastLossActivityID++;
                    }

                    connection2.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Post Request to Activity
                if (!idString.equals("")) {
                    try {
                        URL url2 = new URL(URL + "/activity");
                        HttpURLConnection connection2 = (HttpURLConnection) url2.openConnection();
                        connection2.setRequestMethod("POST");
                        connection2.setRequestProperty("Content-Type", "application/json");

                        //TODO: Can Change
                        String payload = "{\"activityCode\":\"L" + lastLossActivityID + "\",\"activityDate\":\"" + currentDateTime + "\",\"activityTime\":\"" + currentDateTime.substring(11, 19) + "\",\"device\":\"" + idString + "\"}";
                        connection2.setDoOutput(true);
                        DataOutputStream outputStream = new DataOutputStream(connection2.getOutputStream());
                        outputStream.writeBytes(payload);
                        outputStream.flush();
                        outputStream.close();

                        int responseCode2 = connection2.getResponseCode();
                        // System.out.println("Response Code: " + responseCode2);

                        BufferedReader reader2 = new BufferedReader(new InputStreamReader(connection2.getInputStream()));
                        String line2;
                        StringBuffer response2 = new StringBuffer();

                        while ((line2 = reader2.readLine()) != null) {
                            response2.append(line2);
                        }
                        reader2.close();

                        // System.out.println("Response: " + response.toString());
                        String jsonResponse2 = response2.toString().replaceAll("\\},\\{", "},\n{");
                        // System.out.println("Response: \n" + jsonResponse2);

                        //get id of activity that just created
                        JSONObject obj = new JSONObject(jsonResponse2);
                        newLossActivityID = obj.getInt("id");
                        idString = "";
                        connection2.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


                // Update ActivityId and Status for Device that Loss
                for (int i = 0; i < idList.size(); i++) {
                    try {
                        URL url2 = new URL(URL + "/device/" + idList.get(i));
                        HttpURLConnection connection2 = (HttpURLConnection) url2.openConnection();
                        connection2.setRequestMethod("PUT");
                        connection2.setRequestProperty("Content-Type", "application/json");

                        String payload = "{\"rfidStatus\":\"Loss\",\"activityId\":\"" + newLossActivityID + "\"}";    //TODO: Can Change
                        connection2.setDoOutput(true);
                        connection2.setDoInput(true); // Set to receive a response

                        // Open output stream and write payload
                        try (OutputStream os = connection2.getOutputStream()) {
                            byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                            os.write(input, 0, input.length);
                        }

                        int responseCode2 = connection2.getResponseCode();
                        // System.out.println("Response Code: " + responseCode2);

                        // Read response
                        BufferedReader reader2 = new BufferedReader(new InputStreamReader(connection2.getInputStream()));
                        String line2;
                        StringBuffer response2 = new StringBuffer();

                        while ((line2 = reader2.readLine()) != null) {
                            response2.append(line2);
                        }
                        reader2.close();
                        // System.out.println("Response: " + response2.toString());
                        connection2.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                idList.clear();
            }
        }, delay, period);

    }

    private void initUR4() {
        if (ur4Network == null) {
            ur4Network = new RFIDWithUHFNetworkUR4();
            ur4 = ur4Network;
        }

//        if (ur4SerialPort == null) {
//            ur4SerialPort = new RFIDWithUHFSerialPortUR4();
//            ur4 = ur4SerialPort;
//        }


        if (ur4 instanceof RFIDWithUHFSerialPortUR4) {
            String com = (String) "COM" + 4;
            boolean rsult = ur4SerialPort.init(com);
            if (!rsult) {
                System.out.println("Failed to open the serial port!");
                return;
            }
        } else if (ur4 instanceof RFIDWithUHFNetworkUR4) {
            if (!StringUtils.isIPAddress(ip)) {
                System.out.println("Illegal IP address!");
                return;
            }
            if (StringUtils.isEmpty(port)) {
                System.out.println("The port cannot be empty!");
                return;
            }
            boolean rsult = ur4Network.init(ip, Integer.parseInt(port));
            System.out.println("Connection result: " + rsult);
            if (!rsult) {
                System.out.println("Connection failed!");
                return;
            }
        }

    }


}