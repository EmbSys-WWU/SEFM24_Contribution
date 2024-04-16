#include <systemc.h>
#include "gpio.h"

#ifndef __GPIO_TEST_H__
#define __GPIO_TEST_H__

SC_MODULE(gpioTest)
{
  sc_uint<8> SENSOR_VALUE;
  sc_uint<8> ANTENNA_VALUE;
  
  sc_uint<8> bus_in;
  
  sc_out<bool> gpio_convert_bits;
  sc_out<sc_uint<1> > gpio_bit0_in;
  sc_out<sc_uint<1> > gpio_bit1_in;
  sc_out<sc_uint<1> > gpio_bit2_in;
  sc_out<sc_uint<1> > gpio_bit3_in;
  sc_out<sc_uint<1> > gpio_bit4_in;
  sc_out<sc_uint<1> > gpio_bit5_in;
  sc_out<sc_uint<1> > gpio_bit6_in;
  sc_out<sc_uint<1> > gpio_bit7_in;
  sc_in<sc_uint<8> > gpio_word_out;
  
  sc_out<bool> gpio_convert_word;
  sc_out<sc_uint<8> > gpio_word_in;
  sc_in<sc_uint<1> > gpio_bit0_out;
  sc_in<sc_uint<1> > gpio_bit1_out;
  sc_in<sc_uint<1> > gpio_bit2_out;
  sc_in<sc_uint<1> > gpio_bit3_out;
  sc_in<sc_uint<1> > gpio_bit4_out;
  sc_in<sc_uint<1> > gpio_bit5_out;
  sc_in<sc_uint<1> > gpio_bit6_out;
  sc_in<sc_uint<1> > gpio_bit7_out;
  
  void sensor() {
    while (true) {
      sc_uint<8> value = SENSOR_VALUE;
      bus_in = value;      
      wait(8, SC_US);
    }
  }
  
  void breaks() {
    wait(2, SC_US);
    while (true) {
      sc_uint<8> value = gpio_word_out.read();
      wait(8, SC_US);
    }
  }
  
  void antenna() {
    wait(4, SC_US);
    while (true) {
      sc_uint<8> value = ANTENNA_VALUE;
      bus_in = value;
      wait(8, SC_US);
    }
  }
  
  void radio() {
    wait(6, SC_US);
    while (true) {
      sc_uint<8> value = gpio_word_out.read();
      wait(8, SC_US);
    }
  }

  void bus() {
    while (true) {
      wait(SC_ZERO_TIME);
      
      gpio_word_in.write(bus_in);
      gpio_convert_word.write(true);
      wait(SC_ZERO_TIME);
      gpio_convert_word.write(false);
      
      wait(1, SC_US);  
      
      gpio_bit0_in.write(gpio_bit0_out.read());
      gpio_bit1_in.write(gpio_bit1_out.read());
      gpio_bit2_in.write(gpio_bit2_out.read());
      gpio_bit3_in.write(gpio_bit3_out.read());
      gpio_bit4_in.write(gpio_bit4_out.read());
      gpio_bit5_in.write(gpio_bit5_out.read());
      gpio_bit6_in.write(gpio_bit6_out.read());
      gpio_bit7_in.write(gpio_bit7_out.read());
      
      gpio_convert_bits.write(true);
      wait(SC_ZERO_TIME);
      gpio_convert_bits.write(false);
      
      wait(3, SC_US);
    }
  }
    
  SC_CTOR(gpioTest)
  {
    SC_THREAD(sensor);
    SC_THREAD(breaks);
    SC_THREAD(antenna);
    SC_THREAD(radio);
    SC_THREAD(bus);
  }

};

#endif

