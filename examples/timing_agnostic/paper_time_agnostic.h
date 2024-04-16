#include <systemc.h>
#include <iostream>

#ifndef __WAITTEST_H__
#define __WAITTEST_H__

SC_MODULE(paperTimeAgnostic)
{
  sc_inout<int> bus;
  
 
  int SECRET_IN;
  int SECRET_OUT;
  int PUBLIC_IN;
  int PUBLIC_OUT;
  
  void produce() {
    while (true) {
      int data = SECRET_IN;
      bus.write(data);
      wait(SC_ZERO_TIME);
      data = PUBLIC_IN;
      bus.write(data);
      wait(SC_ZERO_TIME);
    }
  }
  
  void consume_secret() {
    wait(SC_ZERO_TIME);
    while (true) {
      int read = bus.read();
      SECRET_OUT = read;
      wait(SC_ZERO_TIME);
    }
  }
  
  void consume_public() {
    while (true) {
      wait(SC_ZERO_TIME);
      int read = bus.read();
      PUBLIC_OUT = read;
    }
  }

    
  SC_CTOR(paperTimeAgnostic)
  {
    SC_THREAD(produce);
    SC_THREAD(consume_secret);
    SC_THREAD(consume_public);
  }

};

#endif
