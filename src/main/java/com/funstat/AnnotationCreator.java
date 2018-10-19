package com.funstat;

import com.funstat.domain.Annotations;
import com.funstat.domain.HLine;
import com.funstat.domain.Label;
import com.funstat.domain.Ohlc;
import com.funstat.sequenta.Sequenta;
import com.funstat.sequenta.Signal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AnnotationCreator {

    public static Annotations createAnnotations(String code, MdUpdater updater) {
        Sequenta sequenta = new Sequenta();

        List<Label> labels = new ArrayList<>();
        List<HLine> lines = new ArrayList<>();
        List<HLine> lines0 = new ArrayList<>();

        AtomicInteger curLine = new AtomicInteger(0);

        List<Ohlc> ohlcs = updater.get(code);
        for(int ci = 0; ci < ohlcs.size(); ci++){
            Ohlc oh = ohlcs.get(ci);
            List<Signal> signals = sequenta.onOhlc(oh);
            int finalCi = ci;
            signals.forEach(s->{

                boolean drawOnTop = s.reference.up;
                double level = s.reference.up ? oh.high : oh.low;
                String color = s.reference.up ? "red" : "green";

                switch (s.type){
                    case Cdn:
                        if(s.reference.countDowns.size() == 8){

                            labels.add(new Label("" + s.reference.countDowns.size(),
                                    oh.dateTime,
                                    level , color, drawOnTop));
                        }
                        break;
                    case Signal:
                        labels.add(new Label("" + s.reference.getCompletedSignal(), oh.dateTime, level , color, drawOnTop));
                        Ohlc endOh = ohlcs.get(Math.min(finalCi + 3, ohlcs.size() - 1));
                        HLine hline = new HLine(ohlcs.get(finalCi - 3).dateTime, endOh.dateTime, calcStopLine(ohlcs, finalCi, s));
                        hline = hline.withAttribute("color", s.reference.up ? "red" : "green");
                        hline = hline.withAttribute("dashStyle", "Solid");
                        lines0.add(hline);

                        break;
                    case SetupReach:
                        lines.add(new HLine(s.reference.getStart(),s.reference.getEnd(), s.reference.getTdst()).withAttribute("dashStyle", "ShortDash"));
                        while (curLine.get() < lines.size() - 5){
                            lines.set(curLine.get(),lines.get(curLine.get()).copyWithNewEnd(oh.dateTime));
                            curLine.incrementAndGet();
                        }
                        break;
                    case Flip:
                        double clevel = s.reference.up ? oh.high : oh.low;
                        labels.add(new Label("" + (finalCi - s.reference.start + 1), oh.dateTime,
                                clevel , "white", drawOnTop));
                        break;
                }
            });
        }
        while (curLine.get() < lines.size()){
            lines.set(curLine.get(),lines.get(curLine.get()).copyWithNewEnd(ohlcs.get(ohlcs.size() - 1).dateTime));
            curLine.incrementAndGet();
        }
        lines.addAll(lines0);
        return new Annotations(labels,lines);
    }

    private static double calcStopLine(List<Ohlc> ohlcs, int ci, Signal s) {
        double curLevel;
        if(s.reference.up){
            curLevel = Double.MIN_VALUE;
            for(int i = s.reference.start; i <= ci; i++){
                Ohlc ohh = ohlcs.get(i);
                curLevel = Math.max(curLevel, ohh.high + ohh.getRange());
            }
        }else {
            curLevel = Double.MAX_VALUE;
            for(int i = s.reference.start; i < ci; i++){
                Ohlc ohh = ohlcs.get(i);
                curLevel = Math.min(curLevel, ohh.low - ohh.getRange());
            }
        }
        return curLevel;
    }

}
