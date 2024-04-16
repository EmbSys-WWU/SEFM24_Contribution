package raid24contribution.sc_model;

import java.io.Serializable;

/**
 * Represents the type of channel. A struct can either be a primitive channel (e.g., sc_signal or
 * sc_fifo), a hierarchical channel (sc_clock or most user-defined channels) or no channel.
 * 
 * 
 */
public enum SCCHANNELTYPE implements Serializable {
    NO_CHANNEL, PRIMITIVE_CHANNEL, HIERARCHICAL_CHANNEL;
}
