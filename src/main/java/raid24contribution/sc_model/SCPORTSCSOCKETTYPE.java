package raid24contribution.sc_model;

import java.io.Serializable;
import java.util.EnumSet;

public enum SCPORTSCSOCKETTYPE implements Serializable {
    
    SC_PORT, SC_SIGNAL, SC_IN, SC_OUT, SC_INOUT, SC_FIFO, SC_FIFO_IN, SC_FIFO_OUT, SC_SOCKET;
    
    
    public static final EnumSet<SCPORTSCSOCKETTYPE> ALL =
            EnumSet.of(SC_PORT, SC_IN, SC_OUT, SC_INOUT, SC_FIFO_IN, SC_FIFO_OUT, SC_SOCKET, SC_SIGNAL, SC_FIFO);
    public static final EnumSet<SCPORTSCSOCKETTYPE> SC_SIGNAL_ALL = EnumSet.of(SC_IN, SC_OUT, SC_INOUT, SC_SIGNAL);
    public static final EnumSet<SCPORTSCSOCKETTYPE> SC_FIFO_ALL = EnumSet.of(SC_FIFO_IN, SC_FIFO_OUT, SC_FIFO);
    
}
