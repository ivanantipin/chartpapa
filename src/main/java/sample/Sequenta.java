package sample;

import com.iaa.finam.Ohlc;

import java.time.LocalDateTime;
import java.util.*;


public class Sequenta {

    int[] counts = new int[]{13,21};

    List<Ohlc> data = new ArrayList<>();

    List<Setup> pendingSetups = new ArrayList<>();

    Setup currentSetup;

    Ohlc past(int bars){
        return data.get(data.size() - bars);
    }

    enum SignalType{
        Signal,Expired,Completed
    }

    class Signal{
        Setup reference;
        int idx;
        double recycleRatio;
        boolean cancelled;

        public Signal(Setup reference, int idx, double recycleRatio, boolean cancelled) {
            this.reference = reference;
            this.idx = idx;
            this.recycleRatio = recycleRatio;
            this.cancelled = cancelled;
        }
    }

    class Setup{
        int start;
        int end;
        boolean up;
        List<Integer> countDowns = new ArrayList<>();
        int pendingSignal = 0;


        double size(){
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

        int selfIndex(){
            return pendingSetups.indexOf(this);
        }

        boolean isCancelled(){
            return pendingSetups.subList(selfIndex(),pendingSetups.size())
                    .stream().anyMatch(s->s.up != this.up);
        }

        double recycleRatio(){
            double ret = pendingSetups.subList(selfIndex() + 1, pendingSetups.size())
                    .stream().mapToDouble(s -> s.size()).max().orElse(1.0);
            return ret/this.size();
        }

        Optional<Signal> checkCountDown(int idx){
            if(cdn(idx)){
                countDowns.add(idx);
            }
            if(countDowns.size() >= counts[pendingSignal]){
                if(up && data.get(idx).high > data.get(countDowns.get(8)).close
                        || !up && data.get(idx).low < data.get(countDowns.get(8)).close
                        ){
                    pendingSignal++;
                    return Optional.of(new Signal(this,idx, 0,isCancelled()));
                }
            }
            return Optional.empty();
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

    public List<Signal> onOhlc(Ohlc ohlc){
        data.add(ohlc);
        if(data.size() < 4){
            return Collections.emptyList();
        }


        boolean upTrend = ohlc.close > past(4).close;
        if(currentSetup == null || upTrend != currentSetup.up){
            int ind = pendingSetups.size() - 1;
            currentSetup = new Setup(ind, ind, upTrend);
        }
        List<Signal> ret = new ArrayList();
        if(currentSetup != null && currentSetup.length() == 9){
            pendingSetups.add(currentSetup);
            ret.add(new Signal(currentSetup, data.size() - 1, 0, false));
        }

        pendingSetups.forEach(set->{

            set.checkCountDown(data.size() - 1).ifPresent(signal -> {
                ret.add(signal);
            });
        });
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
            sequenta.onOhlc(new Ohlc(last.time, last.close,last.high + 0.2,last.low - 0.2,last.high + 0.1));
        }

        Ohlc last = sequenta.last(2);
        sequenta.onOhlc(last);
        List<Signal> signals = sequenta.onOhlc(new Ohlc(last.time, last.close,last.high + 0.2,last.low - 0.2,last.high + 0.1));

        System.out.println(signals);





    }

    public static void main(String[] args) {
        testSequenta();
    }

}
