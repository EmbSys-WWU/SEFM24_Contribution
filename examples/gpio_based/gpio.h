#include <systemc.h>

#ifndef __GPIO_H__
#define __GPIO_H__

SC_MODULE(gpio)
{
  
  sc_in<bool> convert_bits;
  sc_in<sc_uint<1> > bit0_in;
  sc_in<sc_uint<1> > bit1_in;
  sc_in<sc_uint<1> > bit2_in;
  sc_in<sc_uint<1> > bit3_in;
  sc_in<sc_uint<1> > bit4_in;
  sc_in<sc_uint<1> > bit5_in;
  sc_in<sc_uint<1> > bit6_in;
  sc_in<sc_uint<1> > bit7_in;
  
  sc_event word_change;
  sc_inout<sc_uint<8> > word_reg;
  sc_out<sc_uint<8> > word_out;
  
  sc_in<bool> convert_word;
  sc_in<sc_uint<8> > word_in;
  
  sc_event bits_change;
  sc_inout<sc_uint<1> > bit0_reg;
  sc_inout<sc_uint<1> > bit1_reg;
  sc_inout<sc_uint<1> > bit2_reg;
  sc_inout<sc_uint<1> > bit3_reg;
  sc_inout<sc_uint<1> > bit4_reg;
  sc_inout<sc_uint<1> > bit5_reg;
  sc_inout<sc_uint<1> > bit6_reg;
  sc_inout<sc_uint<1> > bit7_reg;
  sc_out<sc_uint<1> > bit0_out;
  sc_out<sc_uint<1> > bit1_out;
  sc_out<sc_uint<1> > bit2_out;
  sc_out<sc_uint<1> > bit3_out;
  sc_out<sc_uint<1> > bit4_out;
  sc_out<sc_uint<1> > bit5_out;
  sc_out<sc_uint<1> > bit6_out;
  sc_out<sc_uint<1> > bit7_out;
   
  SC_CTOR(gpio)
  {
    SC_THREAD(bits_to_word);
    sensitive << convert_bits;
    SC_METHOD(output_word);
    sensitive << word_change;
    
    SC_THREAD(word_to_bits);
    sensitive << convert_word;
    SC_METHOD(output_bits);
    sensitive << bits_change;
  }
  
  
  void bits_to_word() {
    while (true) {
      wait();
      if (!convert_bits.read()) {
        continue;
      }
    
      sc_uint<1> bit0 = bit0_in.read();
      sc_uint<1> bit1 = bit1_in.read();
      sc_uint<1> bit2 = bit2_in.read();
      sc_uint<1> bit3 = bit3_in.read();
      sc_uint<1> bit4 = bit4_in.read();
      sc_uint<1> bit5 = bit5_in.read();
      sc_uint<1> bit6 = bit6_in.read();
      sc_uint<1> bit7 = bit7_in.read();
      
      sc_uint<8> result = bit7;
      result = (2 * result) + bit6;
      result = (2 * result) + bit5;
      result = (2 * result) + bit4;
      result = (2 * result) + bit3;
      result = (2 * result) + bit2;
      result = (2 * result) + bit1;
      result = (2 * result) + bit0;
      
      word_reg.write(result);
      
      word_change.notify(2, SC_NS);
      wait(2, SC_NS);
    }
  }
  
  void output_word() {
    word_out.write(word_reg.read());
  }
  
  void word_to_bits() {
    while (true) {
      wait();
      if (!convert_word.read()) {
        continue;
      }
      
      sc_uint<8> word = word_in.read();
    
      sc_uint<1> bit0 = word % 2;
      word = word / 2;
      sc_uint<1> bit1 = word % 2;
      word = word / 2;
      sc_uint<1> bit2 = word % 2;
      word = word / 2;
      sc_uint<1> bit3 = word % 2;
      word = word / 2;
      sc_uint<1> bit4 = word % 2;
      word = word / 2;
      sc_uint<1> bit5 = word % 2;
      word = word / 2;
      sc_uint<1> bit6 = word % 2;
      word = word / 2;
      sc_uint<1> bit7 = word % 2;
      
      bit0_reg.write(bit0);
      bit1_reg.write(bit1);
      bit2_reg.write(bit2);
      bit3_reg.write(bit3);
      bit4_reg.write(bit4);
      bit5_reg.write(bit5);
      bit6_reg.write(bit6);
      bit7_reg.write(bit7);
      
      bits_change.notify(2, SC_NS);
      wait(2, SC_NS);
    }
  }
  
  void output_bits() {
    bit0_out.write(bit0_reg.read());
    bit1_out.write(bit1_reg.read());
    bit2_out.write(bit2_reg.read());
    bit3_out.write(bit3_reg.read());
    bit4_out.write(bit4_reg.read());
    bit5_out.write(bit5_reg.read());
    bit6_out.write(bit6_reg.read());
    bit7_out.write(bit7_reg.read());
  }

};

#endif

