#include <systemc.h>
#include <iostream>

#ifndef __WAITTEST_H__
#define __WAITTEST_H__

SC_MODULE(paperProdcons)
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
      data = TRUSTED_IN;
      bus.write(data);
      event.notify(1, SC_NS);
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

    
  SC_CTOR(paperProdcons)
  {
    SC_THREAD(ecu);
    SC_THREAD(diagnosis);
    SC_THREAD(pump);
  }

};

#endif
