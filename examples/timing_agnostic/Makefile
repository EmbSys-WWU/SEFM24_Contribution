# Super Generic Makefile
#
# This Makefile compiles every *.cpp and every *.h file into their *.o files.
# Then, an executable is linked and named after the current directory. Make
# sure to have set the environment variables $SYSTEMC_HOME, SYSCIR_HOME, SC2AST
# and $SC2UPPAAL_HOME correctly (if you're on SystemC 2.2.x, set TLM_HOME, too).
#
# If you need specific changes, write them to ./Makefile.local. This file gets
# include (see below) after variable declarations. Check out
# http://stackoverflow.com/questions/8346118/check-if-a-makefile-exists-before-including-it/8346208#8346208
# for more info on how to (not) override variables.
#
# To (compile and) run the current system c design:
# $> make run
#
# To create the uppaal model:
# $> make uppaal
#
# To create the uppaal model without regenerating the ast:
# $> make UPPAALDEP= uppaal # this empties the uppaal target dependecies
#
# To create a new module (the *.h file):
# $> make MN=ModuleName module
#
# To tarball/zip the current dir (*.cpp, *.h & Makefile):
# $> make targz|zip
#
# To ... 'Use the Source, Luke'

# use current directory name as target name
TARGET = $(shell basename $(CURDIR))
# the name of the ast output file
AST_NAME = sc2ast
AST_FILE = $(AST_NAME).ast.xml # this name is given by sc2ast
AST_FILE_OPT=$(AST_NAME).ast.opt.xml
SC2AST_CMD = java -jar $(SC2AST)
VERIFYTA_CMD = $(SC2UPPAAL_HOME)/../RescueTest/scripts/verifyta.sh
CHKMEMSIZE_CMD = $(SC2UPPAAL_HOME)/scripts/chk_mem_size.pl
GENNULLPTR_CMD = $(SC2UPPAAL_HOME)/scripts/gen_memchk_qry.pl

# the name of the uppaal model
UPPAAL_FILE = $(TARGET).uppaal.xml
UPPAAL_FILE_NEWMEM = $(TARGET).uppaal.newmem.xml
ENOUGH_MEM_QUERY_FILE = ./qry_not_enough_mem.q
MEMCHK_QRY_FILE = ./memchk.q

HEADER_FILES = $(wildcard *.h)
CPP_FILES = $(wildcard *.cpp)
ALL_SRC_FILES= $(HEADER_FILES) $(CPP_FILES)
OBJS = $(CPP_FILES:.cpp=.o)

CXX = g++
# -g debug informations, -E output preprocessing result and stop
CFLAGS = -g -Wall -Wno-unused-variable -Wno-unused-but-set-variable #-Wextra
SYSCFLAGS = -DSYSC -DSC_INCLUDE_DYNAMIC_PROCESSES
ARCH ?= $(shell uname -m)
ifeq ($(ARCH),x86_64)
LIBDIR = $(SYSTEMC_HOME)/lib-linux64
else
LIBDIR = $(SYSTEMC_HOME)/lib-linux
endif
# use '-isystem' instead of '-I' to block warnings from included systemc headers *workaround*
INCDIRS = -isystem $(SYSTEMC_HOME)/include -isystem $(SYSTEMC_HOME)/include/tlm_core -isystem $(TLM_HOME)/include
LIBS = -lsystemc -lm

-include Makefile.local

all: $(OBJS)
	$(CXX) $(CFLAGS) $(INCDIRS) -L $(LIBDIR) -W -o $(TARGET) $(OBJS) $(LIBS)

USERDEFINES=
%.o: %.cpp $(HEADER_FILES)
	$(CXX) $(CFLAGS) $(SYSCFLAGS) $(INCDIRS) $(USERDEFINES) -c $*.cpp

run: all
	./$(TARGET)

# make only preprocessing, 'make all' will comlain, screw that!
# use: $> make pp > myoutput
pp: CFLAGS += -E
pp: all

ast: all
	$(SC2AST_CMD) -f $(ALL_SRC_FILES) -o $(AST_NAME)
	@# produce an optimized version of the ast xml for better debugging
	@(cat $(AST_FILE) | tr '\n' ' ' | sed -e 's/>\s*</></g' -e 's/<comment[^>]*>//g' -e 's/\s\s*/ /g'; echo '') > $(AST_FILE_OPT)

# ex:
# $> make ARGS="-a '-h'" syscir
SYSCIRDEP=ast
ARGS=
syscir: $(SYSCIRDEP)
	$(SYSCIR_HOME)/syscir.sh -b -i $(AST_FILE) -o $(TARGET).syscir $(ARGS)

# ex:
# $> make ARGS="-a '-h'" uppaal
UPPAALDEP=ast
ARGS=
uppaal: $(UPPAALDEP)
	$(SC2UPPAAL_HOME)/sc2uppaal.sh -b -i $(AST_FILE_OPT) -o $(UPPAAL_FILE) $(ARGS)

chkmemsize:
	$(CHKMEMSIZE_CMD) -f $(UPPAAL_FILE)

QUERY_FILE=
VERIFYTADEP=clean uppaal
verifyta: $(VERIFYTADEP)
	$(VERIFYTA_CMD) $(UPPAAL_FILE) $(QUERY_FILE)

VERIFYTANEWMEMDEP=clean uppaal chkmemsize
verifytanewmem: $(VERIFYTANEWMEMDEP)
	$(VERIFYTA_CMD) $(UPPAAL_FILE_NEWMEM) $(QUERY_FILE)


clean:
	rm -rf *.o $(TARGET) $(AST_NAME) $(AST_FILE) $(AST_FILE_OPT) *.xml *syscir $(TARGET).tar.gz $(TARGET).zip AnalysisResult.txt ./logs ./log log.* $(MEMCHK_QRY_FILE) 2> /dev/null

dist:
	rm -r $(TARGET) 2> /dev/null; mkdir $(TARGET)
	@# per default cp derefences the symlinks
	cp $(ALL_SRC_FILES) Makefile qry_* $(TARGET)

targz: dist
	tar -czf $(TARGET).tar.gz $(TARGET)
	rm -r $(TARGET)

zip: dist
	zip -r $(TARGET).zip $(TARGET)
	rm -r $(TARGET)

MODULE_FILE = $(MN).h
MAIN_FILE = main.cpp
module:
	@if [ -f $(MODULE_FILE) ]; then \
		echo "$(MODULE_FILE) already exists!"; \
	else \
		$(eval UPPER = $(shell echo $(MN) | tr '[:lower:]' '[:upper:]')) \
		echo '#include <systemc.h>\n#include <iostream>\n\n#include "util.h"\n\n#ifndef __$(UPPER)_H__\n#define __$(UPPER)_H__\n\nSC_MODULE($(MN))\n{\n\n\tvoid main_method(void)\n\t{\n\n\t}\n\n\tSC_CTOR($(MN))\n\t{\n\t\tSC_THREAD(main_method);\n\t}\n};\n\n#endif' > $(MODULE_FILE); \
		(echo '#include "$(MODULE_FILE)"'; cat $(MAIN_FILE) | sed -e 's/^{$$/{\n\t$(MN) my_$(MN)("$(MN)_inst"); \/\/ auto-generated/') > $(MAIN_FILE).new; mv $(MAIN_FILE).new $(MAIN_FILE); \
		echo 'Module $(MODULE_FILE) created. Please check $(MAIN_FILE)!'; \
	fi

valgrind: all
	valgrind ./$(TARGET)

nullptrqry: uppaal
	$(GENNULLPTR_CMD) -f $(UPPAAL_FILE) > $(MEMCHK_QRY_FILE)
	@echo "Query generated! See $(MEMCHK_QRY_FILE)."
