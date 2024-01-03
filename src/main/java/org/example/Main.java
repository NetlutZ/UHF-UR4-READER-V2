package org.example;

import com.rscja.deviceapi.RFIDWithUHFNetworkUR4;
import com.rscja.deviceapi.RFIDWithUHFSerialPortUR4;
import com.rscja.deviceapi.interfaces.IUR4;
import org.example.form.InventoryForm;
import org.example.utils.StringUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        Main main = new Main();
    }

    public Main() {
        initUR4();
       Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
           @Override
           public void run() {
               System.out.println("Shutdown hook ran!");
               if (ur4 != null) {
                   inventoryForm.stopInventory();
                   ur4.free();
               }
           }
       }));

    }

    InventoryForm inventoryForm = new InventoryForm();
    public static IUR4 ur4 = null;
    RFIDWithUHFNetworkUR4 ur4Network = null;
    RFIDWithUHFSerialPortUR4 ur4SerialPort = null;
    String ip = "192.168.99.202";
    String port = "8888";

    private void initUR4() {
//        if (ur4Network == null) {
//            ur4Network = new RFIDWithUHFNetworkUR4();
//            ur4 = ur4Network;
//        }

        if (ur4SerialPort == null) {
            ur4SerialPort = new RFIDWithUHFSerialPortUR4();
            ur4 = ur4SerialPort;
        }


        if (ur4 instanceof RFIDWithUHFSerialPortUR4) {
            String com = (String) "COM" + 4;
//            System.out.println(ur4SerialPort);
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