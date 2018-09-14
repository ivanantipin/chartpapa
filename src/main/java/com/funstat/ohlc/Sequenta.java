package com.funstat.ohlc;

import com.funstat.domain.Ohlc;
import com.iaa.finam.MdDao;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


enum SignalType{
    SetupCount,SetupReach,SetupUnreach,Cdn,Recycling,Flip,Signal,Cancel,Expired,Deffered,Completed;
}

class Signal {
    SignalType type;
    Sequenta.Setup reference;
    Sequenta.Setup successor;

    public Signal(SignalType type, Sequenta.Setup reference) {
        this.type = type;
        this.reference = reference;
    }

    public Signal(SignalType type, Sequenta.Setup reference, Sequenta.Setup successor) {
        this.type = type;
        this.reference = reference;
        this.successor = successor;
    }

    @Override
    public String toString() {
        return "Signal{" +
                "type=" + type +
                '}';
    }
}



public class Sequenta {

    int[] counts = new int[]{13,21};

    List<Ohlc> data = new ArrayList<>();

    List<Setup> pendingSetups = new ArrayList<>();

    Ohlc past(int bars){
        return data.get(data.size() - bars);
    }

    class Setup{
        int start;
        int end;
        boolean up;
        List<Integer> countDowns = new ArrayList<>();
        int pendingSignal = 0;
        Setup cancelledRef;
        Setup recycleRef;
        double range = 0;

        void updateEnd(int idx){
            end = idx;
            range = calcRange();
        }

        double calcRange(){
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            for(int i = start; i <= end; i++){
                min = Math.min(data.get(i).low, min);
                max = Math.max(data.get(i).high, max);
            }
            return max - min;

        }

        int length(){
            return end - start;
        }

        public Setup(int start, int end, boolean up) {
            this.start = start;
            this.end = end;
            this.up = up;
        }

        double recycleRatio(){
            return this.range/recycleRef.range;
        }

        boolean reached(){
            return (end - start) >= 9;
        }

        boolean isCancelled(){
            return cancelledRef != null;
        }

        boolean isCompleted(){
            return pendingSignal == counts.length;
        }

        boolean isCurrent(){
            return this == pendingSetups.get(pendingSetups.size() - 1);
        }

        List<Signal> checkCountDown(){
            int idx = data.size() - 1;
            List<Signal> ret = new ArrayList<>();
            if(cdn(idx)){
                countDowns.add(idx);
                ret.add(new Signal(SignalType.Cdn, this));
            }
            if(countDowns.size() >= counts[pendingSignal]){
                if(up && data.get(idx).high > data.get(countDowns.get(8)).close
                        || !up && data.get(idx).low < data.get(countDowns.get(8)).close
                        ){
                    pendingSignal++;
                    ret.add(new Signal(SignalType.Signal, this));
                }else {
                    ret.add(new Signal(SignalType.Deffered, this));
                }
            }
            if(pendingSignal == counts.length){
                ret.add(new Signal(SignalType.Completed, this));
            }
            return ret;
        }


        private boolean cdn(int idx) {
            return up && getClose(idx) > data.get(idx - 2).high
                    || !up && getClose(idx) < data.get(idx - 2).low;
        }

        private double getClose(int idx) {
            return data.get(idx).close;
        }
    }

    Ohlc last(int idx){
        return data.get(data.size() - idx);
    }

    boolean getCurrentTrend(){
        Ohlc ohlc = data.get(data.size() - 1);
        return ohlc.close > past(4).close;
    }

    Setup currentSetup;

    public List<Signal> onOhlc(Ohlc ohlc){
        data.add(ohlc);
        if(data.size() < 4){
            return Collections.emptyList();
        }
        if(data.size() == 4){
            currentSetup = new Setup(data.size() - 1, data.size() - 1, getCurrentTrend());
            return Collections.emptyList();
        }
        List<Signal> ret = new ArrayList();
        ret.addAll(runCurrent());

        this.pendingSetups.forEach(ps->{
            ret.addAll(ps.checkCountDown());
        });
        this.pendingSetups = this.pendingSetups.stream().filter(ps->{
            return !ps.isCompleted();
        }).collect(Collectors.toList());
        return ret;
    }


    private List<Signal> runCurrent() {
        List<Signal> ret = new ArrayList<>();
        int idx = data.size() - 1;
        currentSetup.updateEnd(idx);
        currentSetup.end = idx;
        if( getCurrentTrend() != currentSetup.up){
            if(currentSetup.length() < 9){
                ret.add(new Signal(SignalType.SetupUnreach, currentSetup));
            }else {
                ret.add(new Signal(SignalType.Flip, currentSetup));
            }
            currentSetup = new Setup(idx,idx, getCurrentTrend());
            return ret;
        }else {
            ret.add(new Signal(SignalType.SetupCount, currentSetup));
        }
        if(currentSetup.length() == 9){
            pendingSetups.add(currentSetup);
            ret.add(new Signal(SignalType.SetupReach, currentSetup));
        }

        for(int i = 0; i < pendingSetups.size() - 1; i++){
            Setup tchk = pendingSetups.get(i);
            if(tchk == currentSetup){
                break;
            }
            if(currentSetup.up == tchk.up && currentSetup.range > tchk.range){
                if(tchk.recycleRef == null || tchk.recycleRef.range < currentSetup.range){
                    tchk.recycleRef = currentSetup;
                    ret.add(new Signal(SignalType.Recycling, tchk, currentSetup));
                }
            }

            if(currentSetup.reached() && currentSetup.up != tchk.up){
                if(tchk.cancelledRef== null || tchk.cancelledRef.range < currentSetup.range){
                    tchk.cancelledRef = currentSetup;
                    ret.add(new Signal(SignalType.Cancel, tchk, currentSetup));
                }
            }
        }
        return ret;
    }



    public static void testSequenta(){
        Sequenta sequenta = new Sequenta();
        List<Ohlc> testHistory = new ArrayList<>();

        sequenta.onOhlc(new Ohlc(LocalDateTime.now(), 1, 2, 0, 1 ));
        sequenta.onOhlc(new Ohlc(LocalDateTime.now(), 1, 2, 0, 1 ));
        sequenta.onOhlc(new Ohlc(LocalDateTime.now(), 1, 2, 0, 1 ));
        sequenta.onOhlc(new Ohlc(LocalDateTime.now(), 1, 2, 0, 1 ));

        for(int i = 1; i < 10; i++){
            sequenta.onOhlc(new Ohlc(LocalDateTime.now(), i, i - 1, i, i + 1));
        }

        for (int i = 0; i < 12; i++){
            Ohlc last = sequenta.last(2);
            sequenta.onOhlc(last);
            sequenta.onOhlc(new Ohlc(last.dateTime, last.close,last.high + 0.2,last.low - 0.2,last.high + 0.1));
        }

        Ohlc last = sequenta.last(2);
        sequenta.onOhlc(last);
        List<Signal> signals = sequenta.onOhlc(new Ohlc(last.dateTime, last.close,last.high + 0.2,last.low - 0.2,last.high + 0.1));
        System.out.println(signals);
    }

    public static void main(String[] args) {
        MdDao mdDao = new MdDao();
        List<Ohlc> tatn = mdDao.queryAll("TATN");
        Sequenta sequenta = new Sequenta();
        tatn.forEach(oh->{
            System.out.println(sequenta.onOhlc(oh));
        });
    }

}
