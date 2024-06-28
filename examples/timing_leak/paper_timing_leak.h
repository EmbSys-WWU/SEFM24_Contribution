#include <systemc.h>
#include <iostream>

#ifndef __WAITTEST_H__
#define __WAITTEST_H__

SC_MODULE(paperTimingLeak)
{
  sc_inout<int> bus;
  sc_event event;
 
  int UNTRUSTED_IN;
  int UNTRUSTED_OUT;
  int TRUSTED_IN;
  int TRUSTED_OUT;
  
  void ecu() {
    while (true) {
      int data = UNTRUSTED_IN;
      bus.write(data);
      wait(2, SC_NS);
      if (data < 0) {
	      data = TRUSTED_IN;
	      bus.write(data);
	      event.notify(750, SC_PS);
      } else {
	      data = TRUSTED_IN;
	      bus.write(data);
	      event.notify(1250, SC_PS);
      }
      wait(2, SC_NS);
    }
  }
  
  void diagnosis() {
    wait(1, SC_NS);
    while (true) {
      int read = bus.read();
      UNTRUSTED_OUT = read;
      wait(4, SC_NS);
    }
  }
  
  void pump() {
    while (true) {
      wait(event);
      int read = bus.read();
      TRUSTED_OUT = read;
    }
  }

    
  SC_CTOR(paperTimingLeak)
  {
    SC_THREAD(ecu);
    SC_THREAD(diagnosis);
    SC_THREAD(pump);
  }

};

#endif
