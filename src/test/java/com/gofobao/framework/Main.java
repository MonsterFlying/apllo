package com.gofobao.framework;

import com.gofobao.framework.helper.ReleaseHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService laodaA = Executors.newFixedThreadPool(7);
        //提交作业给老大，作业内容封装在Callable中，约定好了输出的类型是String。
        String outputs = laodaA.submit(
                new Callable<String>() {
                    public String call() throws Exception {
                        return "I am a task, which submited by the so called laoda, and run by those anonymous workers";
                    }
                    //提交后就等着结果吧，到底是手下7个作业中谁领到任务了，老大是不关心的。
                }).get();

        System.out.println(outputs);


    }


    /**
     * 重新触发派发红包方法
     *
     * @return
     */
    private static boolean publishRedpack() {
        String url = "https://api.gofobao.com/pub/publishActivity/red";
        Map<String, String> data = new HashMap<>();
        data.put("beginTime", "2017-10-21 17:44:00");
        ReleaseHelper.sendMsg(url, data);
        return true;
    }


    /**
     * 离线拨正充值记录
     *
     * @return
     */
    private static boolean statementRechargeOffline() {
        String url = "https://api.gofobao.com/pub/rechargeStatement/offline";
        Map<String, String> data = new HashMap<>();
        data.put("date", "2017-09-30 11:37:50");
        data.put("id", "5930");
        ReleaseHelper.sendMsg(url, data);
        return true;
    }


    private static boolean changeRecord() {
        String url = "https://api.gofobao.com/pub/rechargeStatement/offline";
        Map<String, String> data = new HashMap<>();
        data.put("date", "2017-09-30 11:37:50");
        data.put("id", "5930");
        ReleaseHelper.sendMsg(url, data);
        return true;
    }


}
