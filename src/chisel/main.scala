import chisel3._
import chisel3.util._


case class VgaConfig(
    TOTAL_COL  : Int,
    TOTAL_ROW  : Int,
    ACTIVE_COL : Int,
    ACTIVE_ROW : Int,
    FPH        : Int,
    FPV        : Int,
    BPH        : Int,
    BPV        : Int
)

class VGABundle extends Bundle{
    val red   = Output(UInt(3.W))
    val green = Output(UInt(3.W))
    val blue  = Output(UInt(3.W))
    val hsync = Output(Bool())
    val vsync = Output(Bool())
}

class SyncPulse(config: VgaConfig) extends Module{
    val io = IO(new Bundle{
        val hsync       = Output(Bool())
        val vsync       = Output(Bool())
        val hsync_porch = Output(Bool())
        val vsync_porch = Output(Bool())
        val active_zone = Output(Bool())
    })

    val (col_counter, overflow) = Counter(true.B, config.TOTAL_COL)
    val (row_counter, _) = Counter(overflow, config.TOTAL_ROW)
    val hsync       = Wire(Bool())
    val vsync       = Wire(Bool())
    val hsync_porch = Wire(Bool())
    val vsync_porch = Wire(Bool())

    // hsync
    hsync  := col_counter <= config.ACTIVE_COL.U
    // vsync
    vsync  := row_counter <= config.ACTIVE_ROW.U

    // hsync with porch
    hsync_porch := col_counter <= config.ACTIVE_COL.U + config.FPH.U ||
                   col_counter >= config.TOTAL_COL.U  - config.BPH.U

    // vsync with porch
    vsync_porch := row_counter <= config.ACTIVE_ROW.U + config.FPV.U ||
                   row_counter >= config.TOTAL_ROW.U  - config.BPV.U

    //outputs
    io.hsync                := hsync
    io.vsync                := vsync
    io.hsync_porch          := hsync_porch
    io.vsync_porch          := vsync_porch
    io.active_zone          := hsync & vsync
}

class VGA(config: VgaConfig) extends Module{
    val io = IO(new VGABundle)

    val sync_pulse = Module(new SyncPulse(config))

    io.red   := Mux(sync_pulse.io.active_zone, 1.U, 0.U)
    io.green := Mux(sync_pulse.io.active_zone, 7.U, 0.U)
    io.blue  := Mux(sync_pulse.io.active_zone, 1.U, 0.U)
    io.hsync := sync_pulse.io.hsync_porch
    io.vsync := sync_pulse.io.vsync_porch
}

class Top(config: VgaConfig) extends RawModule {
    val clock    = IO(Input(Clock()))
    val vga_io   = IO(new VGABundle)

    withClockAndReset(clock, false.B){
        val vga = Module(new VGA(config))
        vga_io <> vga.io
    }
    
}

object Main extends App{
    val vga_config = VgaConfig(
        TOTAL_COL  = 800,
        TOTAL_ROW  = 525,
        ACTIVE_COL = 640,
        ACTIVE_ROW = 480,
        FPH        = 16,
        FPV        = 10,
        BPH        = 48,
        BPV        = 33
    )
    (new chisel3.stage.ChiselStage).emitVerilog(new Top(vga_config), Array("--target-dir", "build/artifacts/netlist/"))
}