#include <systemc.h>
#include "gpio.h"
#include "gpio_test.h"


int sc_main(int argc, char* argv[])
{
  gpio gpio("gpio");
  gpioTest gpioTest("gpioTest");
  
  sc_signal<bool> convert_bits;
  sc_signal<bool> convert_word;
  sc_signal<sc_uint<8> > word_in;
  sc_signal<sc_uint<1> > bit0_in;
  sc_signal<sc_uint<1> > bit1_in;
  sc_signal<sc_uint<1> > bit2_in;
  sc_signal<sc_uint<1> > bit3_in;
  sc_signal<sc_uint<1> > bit4_in;
  sc_signal<sc_uint<1> > bit5_in;
  sc_signal<sc_uint<1> > bit6_in;
  sc_signal<sc_uint<1> > bit7_in;
  sc_signal<sc_uint<8> > word_out;
  sc_signal<sc_uint<1> > bit0_out;
  sc_signal<sc_uint<1> > bit1_out;
  sc_signal<sc_uint<1> > bit2_out;
  sc_signal<sc_uint<1> > bit3_out;
  sc_signal<sc_uint<1> > bit4_out;
  sc_signal<sc_uint<1> > bit5_out;
  sc_signal<sc_uint<1> > bit6_out;
  sc_signal<sc_uint<1> > bit7_out;
  sc_signal<sc_uint<8> > word_reg;
  sc_signal<sc_uint<1> > bit0_reg;
  sc_signal<sc_uint<1> > bit1_reg;
  sc_signal<sc_uint<1> > bit2_reg;
  sc_signal<sc_uint<1> > bit3_reg;
  sc_signal<sc_uint<1> > bit4_reg;
  sc_signal<sc_uint<1> > bit5_reg;
  sc_signal<sc_uint<1> > bit6_reg;
  sc_signal<sc_uint<1> > bit7_reg;
  
  gpio.convert_bits(convert_bits);
  gpio.convert_word(convert_word);
  
  gpio.bit0_in(bit0_in);
  gpio.bit1_in(bit1_in);
  gpio.bit2_in(bit2_in);
  gpio.bit3_in(bit3_in);
  gpio.bit4_in(bit4_in);
  gpio.bit5_in(bit5_in);
  gpio.bit6_in(bit6_in);
  gpio.bit7_in(bit7_in);
  gpio.word_out(word_out);
  
  gpio.word_in(word_in);
  gpio.bit0_out(bit0_out);
  gpio.bit1_out(bit1_out);
  gpio.bit2_out(bit2_out);
  gpio.bit3_out(bit3_out);
  gpio.bit4_out(bit4_out);
  gpio.bit5_out(bit5_out);
  gpio.bit6_out(bit6_out);
  gpio.bit7_out(bit7_out);
  
  gpio.word_reg(word_reg);
  gpio.bit0_reg(bit0_reg);
  gpio.bit1_reg(bit1_reg);
  gpio.bit2_reg(bit2_reg);
  gpio.bit3_reg(bit3_reg);
  gpio.bit4_reg(bit4_reg);
  gpio.bit5_reg(bit5_reg);
  gpio.bit6_reg(bit6_reg);
  gpio.bit7_reg(bit7_reg);
  
  gpioTest.gpio_convert_bits(convert_bits);
  gpioTest.gpio_bit0_in(bit0_in);
  gpioTest.gpio_bit1_in(bit1_in);
  gpioTest.gpio_bit2_in(bit2_in);
  gpioTest.gpio_bit3_in(bit3_in);
  gpioTest.gpio_bit4_in(bit4_in);
  gpioTest.gpio_bit5_in(bit5_in);
  gpioTest.gpio_bit6_in(bit6_in);
  gpioTest.gpio_bit7_in(bit7_in);
  gpioTest.gpio_word_out(word_out);
  
  gpioTest.gpio_convert_word(convert_word);
  gpioTest.gpio_word_in(word_in);
  gpioTest.gpio_bit0_out(bit0_out);
  gpioTest.gpio_bit1_out(bit1_out);
  gpioTest.gpio_bit2_out(bit2_out);
  gpioTest.gpio_bit3_out(bit3_out);
  gpioTest.gpio_bit4_out(bit4_out);
  gpioTest.gpio_bit5_out(bit5_out);
  gpioTest.gpio_bit6_out(bit6_out);
  gpioTest.gpio_bit7_out(bit7_out);
  
  sc_start(1200,SC_US);
  return 0;
}
