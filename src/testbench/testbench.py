# Import
import cocotb
from cocotb.triggers import RisingEdge
from cocotb.clock import Clock
from PIL import Image
import numpy as np

# Constant
TOTAL_COL  = 800
TOTAL_ROW  = 525
ACTIVE_COL = 640
ACTIVE_ROW = 480
FPH        = 16
FPV        = 10
BPH        = 48
BPV        = 33

@cocotb.test()
async def dump_frame(dut):
    # Declare and start the simulation Clock
    clock = Clock(dut.clock, 40, units="ns")
    cocotb.start_soon(clock.start())

    # Variable declaration
    num_cycles = 800*525 # one frame
    rgb_array = []
    line = []
    col = 0
    row = 0

    # For loop every pixel of a frame
    for cycle in range(num_cycles):
        await RisingEdge(dut.clock)
        # Get value from dut
        hsync = int(dut.vga_io_hsync)
        vsync = int(dut.vga_io_vsync)
        red   = int(dut.vga_io_red)
        green = int(dut.vga_io_green)
        blue  = int(dut.vga_io_blue)

        # Pixel in pillow a Uint8 whereas they are Uint4 in my design. 
        # We need a shift to see something
        pixel = [red<<5,green<<5,blue<<5]

        col+=1
        line.append(pixel)
        if col==801:
            rgb_array.append(line)
            line = []
            col=0
            row+=1

    frame     = np.array(rgb_array, dtype=np.uint8)
    new_image = Image.fromarray(frame)
    new_image.save("vga_frame.png") 
