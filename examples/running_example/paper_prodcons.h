#include <systemc.h>
#include <iostream>

#ifndef __WAITTEST_H__
#define __WAITTEST_H__

SC_MODULE(paperProdcons)
{
  sc_inout<int> bus;
  sc_event event;
  
 
  int SECRET_IN;
  int SECRET_OUT;
  int PUBLIC_IN;
  int PUBLIC_OUT;
  
  void produce() {
    while (true) {
      int data = SECRET_IN;
      bus.write(data);
      wait(2, SC_NS);
      data = PUBLIC_IN;
      bus.write(data);
      event.notify(1, SC_NS);
      wait(2, SC_NS);
    }
  }
  
  void consume_secret() {
    wait(1, SC_NS);
    while (true) {
      int read = bus.read();
      SECRET_OUT = read;
      wait(4, SC_NS);
    }
  }
  
  void consume_public() {
    while (true) {
      wait(event);
      int read = bus.read();
      PUBLIC_OUT = read;
    }
  }

    
  SC_CTOR(paperProdcons)
  {
    SC_THREAD(produce);
    SC_THREAD(consume_secret);
    SC_THREAD(consume_public);
  }

};

#endif
