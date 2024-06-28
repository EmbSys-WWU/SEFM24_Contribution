#include <systemc.h>
#include <iostream>

#ifndef __WAITTEST_H__
#define __WAITTEST_H__

SC_MODULE(paperTimeAgnostic)
{
  sc_inout<int> bus;
 
  int UNTRUSTED_IN;
  int UNTRUSTED_OUT;
  int TRUSTED_IN;
  int TRUSTED_OUT;
  
  void ecu() {
    while (true) {
      int data = UNTRUSTED_IN;
      bus.write(data);
      wait(SC_ZERO_TIME);
      data = TRUSTED_IN;
      bus.write(data);
      wait(SC_ZERO_TIME);
    }
  }
  
  void diagnosis() {
    wait(SC_ZERO_TIME);
    while (true) {
      int read = bus.read();
      UNTRUSTED_OUT = read;
      wait(SC_ZERO_TIME);
    }
  }
  
  void pump() {
    while (true) {
      wait(SC_ZERO_TIME);
      int read = bus.read();
      TRUSTED_OUT = read;
    }
  }

    
  SC_CTOR(paperTimeAgnostic)
  {
    SC_THREAD(ecu);
    SC_THREAD(diagnosis);
    SC_THREAD(pump);
  }

};

#endif
