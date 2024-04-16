#include <systemc.h>
#include "paper_prodcons.h"


int sc_main(int argc, char* argv[])
{
  paperProdcons pc("prodcons");
  
  sc_signal<int> b;
  pc.bus(b);
  
  sc_start(1200,SC_NS);
  return 0;
}
