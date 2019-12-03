package firelib.common.core;

import firelib.domain.Ohlc;
import com.funstat.domain.InstrId;
import firelib.common.interval.Interval;
import kotlin.sequences.Sequence;

import java.time.LocalDateTime;
import java.util.List;

public interface Source{
    List<InstrId> symbols();
    Sequence<Ohlc> load(InstrId instrId);
    Sequence<Ohlc> load(InstrId instrId, LocalDateTime dateTime);
    String getName();
    Interval getDefaultInterval();
}
