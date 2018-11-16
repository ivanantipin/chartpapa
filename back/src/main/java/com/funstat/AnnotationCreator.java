package com.funstat;

import com.funstat.domain.Annotations;
import com.funstat.domain.HLine;
import com.funstat.domain.Label;
import com.funstat.domain.Ohlc;
import com.funstat.sequenta.Sequenta;
import com.funstat.sequenta.Signal;
import org.apache.commons.collections.map.HashedMap;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AnnotationCreator {

    static List<Integer> displayedCounts = Arrays.asList(11, 12, 20);

    Map<String, String> lighAnn = new HashedMap() {
        {
            put("shape", "connector");
            put("align", "right");
            put("justify", "false");
            put("crop", "true");
        }

    };

/*
    labelOptions: {
        shape: 'connector',
                align: 'right',
                justify: false,
                crop: true,
                style: {
            fontSize: '0.8em',
                    textOutline: '1px white'
        }
    },
*/

    static String formatDbl(double dbl) {
        return new DecimalFormat("#.##").format(dbl);
    }


    public static Annotations createAnnotations(List<Ohlc> ohlcs) {
        Sequenta sequenta = new Sequenta();

        List<Label> labels = new ArrayList<>();
        List<HLine> lines = new ArrayList<>();
        List<HLine> lines0 = new ArrayList<>();

        AtomicInteger curLine = new AtomicInteger(0);

        for (int ci = 0; ci < ohlcs.size(); ci++) {
            Ohlc oh = ohlcs.get(ci);
            List<Signal> signals = sequenta.onOhlc(oh);
            int finalCi = ci;
            signals.forEach(s -> {

                HashMap<String, String> base = new HashMap<>();

                base.put("drawOnTop", "" + s.reference.up);
                base.put("backgroundColor", s.reference.up ? "red" : "green");
                base.put("verticalAlign", s.reference.up ? "bottom" : "top");
                base.put("distance", s.reference.up ? "10" : "-30");

                double level = s.reference.up ? oh.high : oh.low;

                Label baseLabel = new Label(oh.dateTime, level, base);

                switch (s.type) {
                    case Cdn:
                        int count = s.reference.countDowns.size();
                        if (count == 8 ||
                                (finalCi > ohlcs.size() - 10) && displayedCounts.contains(count)) {
                            labels.add(baseLabel.withAttribute("text", "" + count)
                                    .withAttribute("shape", "connector")
                            );
                        }
                        break;
                    case Deffered:
                        if (s.reference.getCompletedSignal() < 13) {
                            labels.add(baseLabel.withAttribute("text", "+")
                                    .withAttribute("shape", "connector")
                            );
                        }
                        break;
                    case Signal:

                        String recycle = s.reference.recycleRatio().map(ratio -> "/R=" + formatDbl(ratio)).orElse("");

                        labels.add(baseLabel.withAttribute("text", "" + s.reference.getCompletedSignal() + recycle));
                        Ohlc endOh = ohlcs.get(Math.min(finalCi + 3, ohlcs.size() - 1));
                        HLine hline = new HLine(ohlcs.get(finalCi - 3).dateTime, endOh.dateTime, calcStopLine(ohlcs, finalCi, s));


                        hline = hline.withAttribute("color", s.reference.up ? "red" : "green");
                        hline = hline.withAttribute("dashStyle", "Solid");
                        lines0.add(hline);

                        break;
                    case SetupReach:
                        HLine hhline = new HLine(s.reference.getStart(), s.reference.getEnd(), s.reference.getTdst())
                                .withAttribute("dashStyle", "ShortDash")
                                .withAttribute("color", s.reference.up ? "green" : "red");
                        ;
                        lines.add(hhline);
                        while (curLine.get() < lines.size() - 5) {
                            lines.set(curLine.get(), lines.get(curLine.get()).copyWithNewEnd(oh.dateTime));
                            curLine.incrementAndGet();
                        }
                        break;
                    case Flip:
                        double clevel = s.reference.up ? oh.high : oh.low;
                        labels.add(baseLabel
                                .withAttribute("text", "" + (finalCi - s.reference.start + 1))
                                .withAttribute("backgroundColor", "white")
                                .withAttribute("shape", "circle")
                        );
                        break;
                }
            });
        }
        while (curLine.get() < lines.size()) {
            lines.set(curLine.get(), lines.get(curLine.get()).copyWithNewEnd(ohlcs.get(ohlcs.size() - 1).dateTime));
            curLine.incrementAndGet();
        }
        lines.addAll(lines0);
        return new Annotations(labels, lines);
    }

    private static double calcStopLine(List<Ohlc> ohlcs, int ci, Signal s) {
        double curLevel;
        if (s.reference.up) {
            curLevel = Double.MIN_VALUE;
            for (int i = s.reference.start; i <= ci; i++) {
                Ohlc ohh = ohlcs.get(i);
                curLevel = Math.max(curLevel, ohh.high + ohh.getRange());
            }
        } else {
            curLevel = Double.MAX_VALUE;
            for (int i = s.reference.start; i < ci; i++) {
                Ohlc ohh = ohlcs.get(i);
                curLevel = Math.min(curLevel, ohh.low - ohh.getRange());
            }
        }
        return curLevel;
    }

    public static void main(String[] args) {
        System.out.println(formatDbl(1.55));
    }

}
