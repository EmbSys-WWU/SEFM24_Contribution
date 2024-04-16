#include <systemc.h>
#include "paper_change_dependent.h"


int sc_main(int argc, char* argv[])
{
  paperChangeDependent cd("change dependent");
  
  sc_signal<int> b;
  cd.bus(b);
  
  sc_start(1200,SC_NS);
  return 0;
}
