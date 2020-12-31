package me

//import com.intelligt.modbus.jlibmodbus.msg.ModbusRequestBuilder
//import com.intelligt.modbus.jlibmodbus.msg.base.ModbusRequest
//import com.intelligt.modbus.jlibmodbus.msg.response.ReadHoldingRegistersResponse
//import com.intelligt.modbus.jlibmodbus.utils.DataUtils

import java.net.InetAddress;

//import com.intelligt.modbus.jlibmodbus.Modbus;
//import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
//import com.intelligt.modbus.jlibmodbus.exception.ModbusNumberException;
//import com.intelligt.modbus.jlibmodbus.exception.ModbusProtocolException;
//import com.intelligt.modbus.jlibmodbus.master.ModbusMaster;
//import com.intelligt.modbus.jlibmodbus.master.ModbusMasterFactory;
//import com.intelligt.modbus.jlibmodbus.tcp.TcpParameters;
public class Test {


    public static float parseInt2Float(int x1, int x2) {
        //int f, fRest, exponent, exponentRest;
        //float value, weishu;
        def f = x1 / 32768;
        def fRest = x1 % 32768;
        def exponent = fRest / 128;
        def exponentRest = fRest % 128;
        def weishu = (float)(exponentRest * 65536 + x2) / 8388608;
        def value = (float)Math.Pow(-1, f) * (float)Math.Pow(2, exponent - 127) * (weishu + 1);
        return value;
    }
//
//    public static void main(String[] args) throws Exception {
//
//
////        short high = 32206 // the high 16 bits
////        short low = 47932 // the low 16 bits
////        int combined = (high << 16) | low;
////        float num = Float.intBitsToFloat(combined);
//
//
//        try {
//            // 设置主机TCP参数
//            TcpParameters tcpParameters = new TcpParameters();
//
//            // 设置TCP的ip地址
//            InetAddress adress = InetAddress.getByName("124.165.254.98");
//
//            // TCP参数设置ip地址
//            // tcpParameters.setHost(InetAddress.getLocalHost());
//            tcpParameters.setHost(adress);
//
//            // TCP设置长连接
//            tcpParameters.setKeepAlive(true);
//            // TCP设置端口，这里设置是默认端口502
//            tcpParameters.setPort(20115);
//
//            // 创建一个主机
//            ModbusMaster master = ModbusMasterFactory.createModbusMasterTCP(tcpParameters);
//            Modbus.setAutoIncrementTransactionId(true);
//
//            int slaveId = 1;//从机地址
//            int offset = 0x000;//寄存器读取开始地址
//            int quantity = 2;//读取的寄存器数量
//
//
//            try {
//                //println master.isConnected()
//                if (!master.isConnected()) {
//                    master.connect();// 开启连接
//                }
//
//                // 读取对应从机的数据，readInputRegisters读取的写寄存器，功能码04
//                //int[] registerValues = master.readInputRegisters(slaveId, offset, quantity);
//                int[] registerValues = master.readHoldingRegisters(slaveId, offset, quantity)
//                println registerValues
//                short high = registerValues[0] // the high 16 bits
//                short low = registerValues[1] // the low 16 bits
//                int combined = (high << 16) | low;
//                println Float.intBitsToFloat(combined);
//
//                //println parseInt2Float(low,high)
//                // 控制台输出
////                for (value in registerValues) {
////                    println value
////
////                    //System.out.println("Address: " + offset + ", Value: " + value.getFloat64At(0));
////                }
//
//            } catch (ModbusProtocolException e) {
//                e.printStackTrace();
//            } catch (ModbusNumberException e) {
//                e.printStackTrace();
//            } catch (ModbusIOException e) {
//                e.printStackTrace();
//            } finally {
//                try {
//                    master.disconnect();
//                } catch (ModbusIOException e) {
//                    e.printStackTrace();
//                }
//            }
//        } catch (RuntimeException e) {
//            throw e;
//        } catch (Exception e) {
//            e.printStackTrace();
//
//        }
//
//
//    }
}