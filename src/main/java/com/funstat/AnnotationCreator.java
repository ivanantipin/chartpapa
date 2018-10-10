package com.funstat;

import com.funstat.domain.Annotations;
import com.funstat.domain.HLine;
import com.funstat.domain.Label;
import com.funstat.domain.Ohlc;
import com.funstat.sequenta.Sequenta;
import com.funstat.sequenta.Signal;
import com.funstat.sequenta.SignalType;
import com.funstat.store.MdDao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AnnotationCreator {

    public static Annotations createAnnotations(String code) {
        Sequenta sequenta = new Sequenta();

        List<Label> labels = new ArrayList<>();
        List<HLine> lines = new ArrayList<>();

        AtomicInteger curLine = new AtomicInteger(0);


        List<Ohlc> ohlcs = new MdDao().queryAll(code);
        ohlcs.forEach(oh->{
            List<Signal> signals = sequenta.onOhlc(oh);
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
                        labels.add(new Label("" + s.reference.countDowns.size(), oh.dateTime, level , color, drawOnTop));
                        break;
                    case SetupReach:
                        lines.add(new HLine(s.reference.getStart(),s.reference.getEnd(), s.reference.getTdst()));
                        while (curLine.get() < lines.size() - 5){
                            lines.set(curLine.get(),lines.get(curLine.get()).copyWithNewEnd(oh.dateTime));
                            curLine.incrementAndGet();
                        }
                        break;
                    case Flip:
                        for(int i = s.reference.start; i < s.reference.end; i++){

                            Ohlc coh = ohlcs.get(i);
                            double clevel = s.reference.up ? coh.high : coh.low;
                            labels.add(new Label("" + (i - s.reference.start + 1), coh.dateTime,
                                    clevel , "white", drawOnTop));
                        }
                        break;


                }
            });
        });
        while (curLine.get() < lines.size()){
            lines.set(curLine.get(),lines.get(curLine.get()).copyWithNewEnd(ohlcs.get(ohlcs.size() - 1).dateTime));
            curLine.incrementAndGet();
        }
        return new Annotations(labels,lines);
    }

}
