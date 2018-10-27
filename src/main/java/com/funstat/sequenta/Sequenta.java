package com.funstat.sequenta;

import com.funstat.domain.Ohlc;
import com.funstat.store.MdDao;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


public class Sequenta {

    public static final int SETUP_LENGTH = 9;
    int[] counts = new int[]{13,21};

    List<Ohlc> data = new ArrayList<>();

    List<Setup> pendingSetups = new ArrayList<>();

    Ohlc past(int bars){
        return data.get(data.size() - bars);
    }

    public class Setup{
        public int start;
        public int end;
        public boolean up;
        public List<Integer> countDowns = new ArrayList<>();
        int pendingSignal = 0;
        Setup cancelledRef;
        Setup recycleRef;

        int closesBeyondTdst = 0;

        double min;
        double max;

        void updateEnd(int idx){
            end = idx;
            calcRange();
        }

        public double getTdst(){
            return up ? min : max;
        }

        public LocalDateTime getStart(){
            return data.get(start).dateTime;
        }

        public LocalDateTime getEnd(){
            return data.get(end).dateTime;
        }

        public int setupSize(){
            return end - start;
        }


        void calcRange(){
            this.min = Double.MAX_VALUE;
            this.max = Double.MIN_VALUE;
            for(int i = start; i <= end; i++){
                min = Math.min(data.get(i).low, min);
                max = Math.max(data.get(i).high, max);
            }
        }

        double range(){
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

        public Optional<Double> recycleRatio(){

            if(recycleRef == null){
                return Optional.empty();
            }
            return Optional.of(recycleRef.range()/this.range());
        }

        boolean reached(){
            return (end - start) >= (SETUP_LENGTH - 1);
        }

        boolean invalidated(){
            return closesBeyondTdst > 5;
        }

        boolean isCancelled(){
            return cancelledRef != null;
        }

        boolean isCompleted(){
            return pendingSignal == counts.length;
        }

        boolean isExpired(){
            return data.size() - start > 150;
        }


        public int getCompletedSignal(){
            if(pendingSignal == 0){
                return -1;
            }
            return counts[pendingSignal - 1];
        }

        List<Signal> checkCountDown(){
            int idx = data.size() - 1;
            List<Signal> ret = new ArrayList<>();

            checkClosesBeyondTdst(idx);

            if(isCntdn(idx)){
                countDowns.add(idx);
                ret.add(new Signal(SignalType.Cdn, this));
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
            }
            if(isCompleted()){
                ret.add(new Signal(SignalType.Completed, this));
            }
            return ret;
        }

        private void checkClosesBeyondTdst(int idx) {
            if(up ){
                if(data.get(idx).close < getTdst()){
                    closesBeyondTdst++;
                }
            }else {
                if(data.get(idx).close > getTdst()){
                    closesBeyondTdst++;
                }
            }
        }


        private boolean isCntdn(int idx) {
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
        Ohlc ohlc = past(1);
        return ohlc.close > past(5).close;
    }

    Setup currentSetup;

    public List<Signal> onOhlc(Ohlc ohlc){
        data.add(ohlc);
        if(data.size() < 5){
            return Collections.emptyList();
        }
        if(data.size() == 5){
            currentSetup = new Setup(data.size() - 1, data.size() - 1, getCurrentTrend());
            return Collections.emptyList();
        }
        List<Signal> ret = new ArrayList();
        ret.addAll(runCurrent());

        this.pendingSetups.forEach(ps->{
            ret.addAll(ps.checkCountDown());
        });
        this.pendingSetups = this.pendingSetups.stream().filter(ps->{
            return !ps.isCompleted() && !ps.isExpired() && !ps.invalidated();
        }).collect(Collectors.toList());
        return ret;
    }


    private List<Signal> runCurrent() {
        List<Signal> ret = new ArrayList<>();
        int idx = data.size() - 1;
        currentSetup.updateEnd(idx);
        if( getCurrentTrend() != currentSetup.up){
            if(!currentSetup.reached()){
                ret.add(new Signal(SignalType.SetupUnreach, currentSetup));
            }else {
                ret.add(new Signal(SignalType.Flip, currentSetup));
            }
            currentSetup = new Setup(idx,idx, getCurrentTrend());
            return ret;
        }else {
            ret.add(new Signal(SignalType.SetupCount, currentSetup));
        }
        if(currentSetup.reached() && !pendingSetups.contains(currentSetup)){
            pendingSetups.add(currentSetup);
            ret.add(new Signal(SignalType.SetupReach, currentSetup));
        }

        for(int i = 0; i < pendingSetups.size() - 1; i++){
            Setup tchk = pendingSetups.get(i);
            if(tchk == currentSetup){
                break;
            }
            if(currentSetup.up == tchk.up && currentSetup.range() > tchk.range()){
                if(tchk.recycleRef == null || tchk.recycleRef.range() < currentSetup.range()){
                    tchk.recycleRef = currentSetup;
                    ret.add(new Signal(SignalType.Recycling, tchk, currentSetup));
                }
            }

            if(currentSetup.reached() && currentSetup.up != tchk.up){
                if(tchk.cancelledRef== null || tchk.cancelledRef.range() < currentSetup.range()){
                    tchk.cancelledRef = currentSetup;
                    ret.add(new Signal(SignalType.Cancel, tchk, currentSetup));
                }
            }
        }
        return ret;
    }
}