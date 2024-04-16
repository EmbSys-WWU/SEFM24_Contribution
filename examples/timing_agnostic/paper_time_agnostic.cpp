#include <systemc.h>
#include "paper_time_agnostic.h"


int sc_main(int argc, char* argv[])
{
  paperTimeAgnostic ta("time agnostic");
  
  sc_signal<int> b;
  ta.bus(b);
  
  sc_start(1200,SC_NS);
  return 0;
}
