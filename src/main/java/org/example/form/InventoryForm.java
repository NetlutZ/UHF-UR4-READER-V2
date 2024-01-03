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
    private int totalCount; //总次数
    private long totalTime;//总时间
    private long startReadTime;//开始时间
    private InventoryTableModel inventoryTableModel = new InventoryTableModel();
    Timer timer = new Timer();

    public InventoryForm() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                btnStartStopActionPerformed(null);
            }
        }, 5000);
    }

    private void btnStartStopActionPerformed(ActionEvent e) {
        //设置盘点回调接口
        Main.ur4.setInventoryCallback(new IUHFInventoryCallback() {
            @Override
            public void callback(UHFTAGInfo uhftagInfo) {
                inventoryTableModel.addData(uhftagInfo);
            }
        });
        boolean result = Main.ur4.startInventoryTag();
    }

    public void startInventory() {
        isRuning = true;
//        btnClear.setEnabled(false);
//        btnStartStop.setText("STOP");
        startReadTime = System.currentTimeMillis();

        new Thread() {
            //更新时间的线程
            public void run() {
//                String inventoryTimeStr = txtTime.getText();
                int inventoryTime = 0;
//                if (inventoryTimeStr != null && !inventoryTimeStr.isEmpty()) {
//                    //盘点时间
//                    inventoryTime = Integer.parseInt(inventoryTimeStr);
//                }
                while (isRuning) {
                    totalTime = (System.currentTimeMillis() - startReadTime) / 1000;
//                    lblTime.setText(totalTime + "");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
//                    if (inventoryTime != 0 && totalTime >= inventoryTime) {
//                        //如果到达盘点时间则停止盘点
//                        stopInventory();
//                    }

                }
            }

            ;
        }.start();
    }

    public void stopInventory() {
        isRuning = false;
//        btnStartStop.setText("AUTO");
        if (Main.ur4 != null)
            Main.ur4.stopInventory();
//        btnClear.setEnabled(true);
    }
}
