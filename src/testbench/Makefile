# Parameters
TOPLEVEL_LANG ?= verilog
VERILOG_SOURCES = $(shell pwd)/../../build/artifacts/netlist/Top.v
TOPLEVEL := Top
MODULE := testbench
SIM := verilator

# Simulator specific arguments
EXTRA_ARGS += --trace --trace-structs
SIM_BUILD=$(PWD)/../../build/artifacts/sim_build
COCOTB_RESULTS_FILE=$(PWD)/../../build/artifacts/sim_build/result.xml

# Include cocotb's Makefile
include $(shell cocotb-config --makefiles)/Makefile.sim