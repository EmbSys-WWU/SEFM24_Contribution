#include <systemc.h>
#include "paper_timing_leak.h"


int sc_main(int argc, char* argv[])
{
  paperTimingLeak tl("timingLeak");
  
  sc_signal<int> b;
  tl.bus(b);
  
  sc_start(1200,SC_NS);
  return 0;
}
