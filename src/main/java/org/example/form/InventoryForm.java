package org.example.form;

import com.rscja.deviceapi.entity.UHFTAGInfo;
import com.rscja.deviceapi.interfaces.IUHFInventoryCallback;
import org.example.Main;
import org.example.model.InventoryTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Timer;
import java.util.TimerTask;

public class InventoryForm {
    boolean isRuning = false;
    private InventoryTableModel inventoryTableModel = new InventoryTableModel();
    Timer timer = new Timer();

    public InventoryForm() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                btnStartStopActionPerformed(null);
            }
        }, 3000);
    }

    private void btnStartStopActionPerformed(ActionEvent e) {
        Main.ur4.setInventoryCallback(new IUHFInventoryCallback() {
            @Override
            public void callback(UHFTAGInfo uhftagInfo) {
                inventoryTableModel.addData(uhftagInfo);
            }
        });
        boolean result = Main.ur4.startInventoryTag();
        if (!result) {
            System.out.println("Failed to open counting!");
            return;
        }
    }

    public void startInventory() {
        isRuning = true;

        new Thread() {
            public void run() {
                int inventoryTime = 0;
                while (isRuning) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }

                }
            }

            ;
        }.start();
    }

    public void stopInventory() {
        isRuning = false;
        if (Main.ur4 != null)
            Main.ur4.stopInventory();
    }
}
