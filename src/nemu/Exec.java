package nemu;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.text.html.HTMLDocument.Iterator;

import compile.ASM;
import compile.Words;
import mainUi.CompileUi;

/**
 * 执行一段代码
 * @author LiangTao
 *
 */
public class Exec extends Thread{

	private short eip;//指令范围0~65535
	private MyDOS dos;
	/* 构造方法 */
	public Exec(String code,MyDOS dos){
		//将源程序装入内存
		load(code);
		this.dos = dos;
//		dos = new MyDOS();//创建Mydos
		dos.addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowIconified(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeiconified(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeactivated(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowClosing(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowClosed(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowActivated(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	@Override
	public void run(){
		long startTime = System.currentTimeMillis();    //获取开始时间
		exe();
		long endTime = System.currentTimeMillis();    //获取结束时间
		dos.refreshDos("<"+"本次程序运行时间：" + (endTime - startTime) + "ms\n");    //输出程序运行时间
		dos.refreshDos("<"+"End of Execute!"+"\n");
		dos.refreshDos("<"+"Press any key to return!"+"\n");
		//dos.repaint();
		dos.revalidate();//重绘控件
		dos.getReadBuffer();
		dos.dispose();
	}
	/**
	 * 执行程序
	 */
	private void exe() {
		// TODO Auto-generated method stub
		eip = 0;
		Boolean stilRun = true;
		//取指令
		while(stilRun){
			short ins = Memory.memory[eip];
			byte op = (byte) ((ins>>12) & 0x000f); 
			byte addr1 = (byte) ((ins >> 10) & 0x0003);
			byte addr2 = (byte) ((ins >> 8) & 0x0003);
			int dir = (int) (ins & 0x00ff);
			String option = Instructions.getOpByIns(op+"");
			System.out.println(option);
			//分析指令
			//执行指令
			switch(option){
			case "read":match_read(addr1,addr2,dir);break;
			case "write":match_write(addr1,addr2,dir);break;
			case "load":match_load(addr1,addr2,dir);break;
			case "store":match_store(addr1, addr2, dir);break;
			case "call":match_call(addr1, addr2, dir);break;
			case "ret":match_ret(addr1, addr2, dir);break;
			case "add":match_add(addr1, addr2, dir);break;
			case "sub":match_sub(addr1, addr2, dir);break;
			case "mul":match_mul(addr1, addr2, dir);break;
			case "div":match_div(addr1, addr2, dir);break;
			case "cmp":match_cmp(addr1, addr2, dir);break;
			case "jmp":match_jmp(addr1, addr2, dir);break;
			case "jmpneg":match_jmpneg(addr1, addr2, dir);break;
			case "jmppos":match_jmppos(addr1, addr2, dir);break;
			case "jmpzero":match_jmpzero(addr1, addr2, dir);break;
			case "halt":stilRun = false;break;
			default:System.out.println("无法识别的命令");break;
			}
			eip++;
		}
	}
	/**
	 * 从键盘读一个字到第一地址
	 * 等待用户从键盘输入一个十进制数据，数据范围是-32768~+32767
	 * @param addr1
	 * @param addr2
	 * @param dir
	 */
	private void match_read(byte addr1 , byte addr2 , int dir){
		String readBuffer = null;
		Boolean readOK = false;
		while(dos != null && !readOK){
			readBuffer = dos.getReadBuffer();
			if(!assert_num(readBuffer)){
				//字符串中包含不合法的类型
				readBuffer += "\n输入不合法，请重新输入";
			}
			else{
				//符合要求的字符串(如果数据越界直接截取)
				short inputNum = (short) Integer.parseInt(readBuffer);
				switch(addr1){
				case 0:Cpu.R0 = inputNum;break;
				case 1:Cpu.R1 = inputNum;break;
				case 2:Cpu.R2 = inputNum;break;
				case 3:Cpu.R3 = inputNum;break;
					default:System.out.println("match_read");break;
				}
				readOK = true;
			}
			dos.refreshDos(readBuffer+"\n");
		}
	}
	/**
	 * 从第一地址写一个字到屏幕
	 * @param addr1
	 * @param addr2
	 * @param dir
	 */
	private void match_write(byte addr1 , byte addr2 , int dir){
		short value1 = getAddr1Value(addr1, addr2, dir);
		String write = value1 + "";
		if(dos != null){
			dos.refreshDos("<"+write+"\n");
		}
	}
	/**
	 * load:从addr2将字装入addr1
	 * @param addr1
	 * @param addr2
	 */
	private void match_load(byte addr1 , byte addr2 , int dir){
		short value2 = getAddr2Value(addr1, addr2, dir);
		switch(addr1){
		case 0:Cpu.R0 = value2;break;
		case 1:Cpu.R1 = value2;break;
		case 2:Cpu.R2 = value2;break;
		case 3:Cpu.R3 = value2;break;
			default:System.out.println("match_load"+addr1);break;
		}
		
	}
	/**
	 * 将addr1中的字存放到addr2中去
	 * @param addr1
	 * @param addr2
	 * @param dir
	 */
	private void match_store(byte addr1 , byte addr2 , int dir){
		short value1 = getAddr1Value(addr1, addr2, dir);
		if(addr2 == 0){
			//直接地址寻址
			Memory.memory[dir] = value1;
		}
		else if(addr2 == 1){
			//寄存器寻址
			byte Ri = (byte) (((byte) dir) & 0x000f);
			byte mode = (byte) ((((byte) dir) >> 4) & 0x000f);
			if(mode == 0){
				//寄存器直接寻址
				switch(Ri){
				case 0:Cpu.R0 = value1;break;
				case 1:Cpu.R1 = value1;break;
				case 2:Cpu.R2 = value1;break;
				case 3:Cpu.R3 = value1;break;
				default:System.out.println("match_store-addr2(1)-mode(0)");
				}
			}
			else if(mode == 1){
				//寄存器间址寻址
				switch(Ri){
				case 0:Memory.memory[(int)(Cpu.R0 & 0x0000ffff)] = value1;break;
				case 1:Memory.memory[(int)(Cpu.R1 & 0x0000ffff)] = value1;break;
				case 2:Memory.memory[(int)(Cpu.R2 & 0x0000ffff)] = value1;break;
				case 3:Memory.memory[(int)(Cpu.R3 & 0x0000ffff)] = value1;break;
				default:System.out.println("match_store-addr2(1)-mode(1)");
				}
			}
			else{
				System.out.println("match_store-addr2(1)");
			}
		}
		else if(addr2 == 2){
			//不应该存在
			System.out.println("match_store-addr2(2)");
		}
		else if(addr2 == 3){
			//变址寻址
			Memory.memory[dir*256+(Cpu.R3&0xffff)] = value1;
			System.out.println(dir*256+(Cpu.R3&0xffff) + "---" + value1);
		}
	}
	/**
	 * 转移到第二地址指定的内存单元
	 * 执行子程序，断点保留在系统堆栈中
	 * @param addr1
	 * @param addr2
	 * @param dir
	 */
	private void match_call(byte addr1 , byte addr2 , int dir){
		Cpu.TopReg = (short) (Cpu.TopReg - 1);
		System.out.println("堆栈"+(int)(Cpu.TopReg & 0xffff));
		//将断点存入TopReg所指向的内存单元
		Memory.memory[(int)(Cpu.TopReg & 0xffff)] = eip;
		//新的入口
		short neweip = getAddr2Value(addr1, addr2, dir);
		eip = (short) (neweip - 1);
	}
	/**
	 * 由系统堆栈获得断点，返回
	 * @param addr1
	 * @param addr2
	 * @param dir
	 */
	private void match_ret(byte addr1 , byte addr2 , int dir){
		eip = (short) Memory.memory[(int)(Cpu.TopReg & 0xffff)];
		Cpu.TopReg = (short) (Cpu.TopReg + 1);
		System.out.println("退栈"+eip+"当前栈顶指针"+(int)(Cpu.TopReg & 0xffff));
	}
	/**
	 * 将addr1中的字加上addr2中的字，结果保留在第一地址中
	 * @param addr1
	 * @param addr2
	 * @param dir
	 */
	private void match_add(byte addr1 , byte addr2 , int dir){
		short value = getAddr2Value(addr1, addr2, dir);
		
		switch(addr1){
		case 0:Cpu.R0 = (short) (Cpu.R0 + value);break;
		case 1:Cpu.R1 = (short) (Cpu.R1 + value);break;
		case 2:Cpu.R2 = (short) (Cpu.R2 + value);break;
		case 3:Cpu.R3 = (short) (Cpu.R3 + value);break;
		default:System.out.println("add(+)");break;
		}
	}
	/**
	 * 将第一地址中的字减去第二地址中的字，结果保留在第一地址中
	 * @param addr1
	 * @param addr2
	 * @param dir
	 */
	private void match_sub(byte addr1 , byte addr2 , int dir){
		short value = getAddr2Value(addr1, addr2, dir);
		
		switch(addr1){
		case 0:Cpu.R0 = (short) (Cpu.R0 - value);break;
		case 1:Cpu.R1 = (short) (Cpu.R1 - value);break;
		case 2:Cpu.R2 = (short) (Cpu.R2 - value);break;
		case 3:Cpu.R3 = (short) (Cpu.R3 - value);break;
		default:System.out.println("sub(-)");break;
		}
	}
	/**
	 * 将第一地址中的字乘以第二地址中的字，结果保留在第一地址中
	 * @param addr1
	 * @param addr2
	 * @param dir
	 */
	private void match_mul(byte addr1 , byte addr2 , int dir){
		short value = getAddr2Value(addr1, addr2, dir);
		
		switch(addr1){
		case 0:Cpu.R0 = (short) (Cpu.R0 * value);break;
		case 1:Cpu.R1 = (short) (Cpu.R1 * value);break;
		case 2:Cpu.R2 = (short) (Cpu.R2 * value);break;
		case 3:Cpu.R3 = (short) (Cpu.R3 * value);break;
		default:System.out.println("mul(*)");break;
		}
	}
	/**
	 * 将第一地址中的字除以第二地址中的字，结果保留在第一地址中
	 * @param addr1
	 * @param addr2
	 * @param dir
	 */
	private void match_div(byte addr1 , byte addr2 , int dir){
		short value = getAddr2Value(addr1, addr2, dir);
		
		switch(addr1){
		case 0:Cpu.R0 = (short) (Cpu.R0 / value);break;
		case 1:Cpu.R1 = (short) (Cpu.R1 / value);break;
		case 2:Cpu.R2 = (short) (Cpu.R2 / value);break;
		case 3:Cpu.R3 = (short) (Cpu.R3 / value);break;
		default:System.out.println("div(/)");break;
		}
	}
	/**
	 * 将第一地址中的字和第二地址中的字比较，由系统置位标志寄存器FlagReg
	 * 标志寄存器FlagReg=-1，表示第一地址中的字小于第二地址中的字
	 * 					1 ，					大于
	 * 					0					等于
	 * @param addr1
	 * @param addr2
	 * @param dir
	 */
	private void match_cmp(byte addr1 , byte addr2 , int dir){
		short value1 = getAddr1Value(addr1, addr2, dir);
		short value2 = getAddr2Value(addr1, addr2, dir);
		if(value1 < value2){
			Cpu.FlagReg = -1;
		}
		else if(value1 > value2){
			Cpu.FlagReg = 1;
		}
		else if(value1 == value2){
			Cpu.FlagReg = 0;
		}
	}
	/**
	 * 无条件转移到第二地址指定的内存单元
	 * @param addr1
	 * @param addr2
	 * @param dir
	 */
	private void match_jmp(byte addr1 , byte addr2 , int dir){
		short neweip = getAddr2Value(addr1, addr2, dir);
		eip = (short) (neweip - 1);
	}
	/**
	 * 若标志寄存器FlagReg中的值为-1，转移到第二地址指定的内存单元
	 * @param addr1
	 * @param addr2
	 * @param dir
	 */
	private void match_jmpneg(byte addr1 , byte addr2 , int dir){
		if(Cpu.FlagReg == -1){
			eip = (short) (getAddr2Value(addr1, addr2, dir) - 1);
		}
	}
	/**
	 * 若标志寄存器FlagReg中的值为1，转移到第二地址指定的内存单元
	 * @param addr1
	 * @param addr2
	 * @param dir
	 */
	private void match_jmppos(byte addr1 , byte addr2 , int dir){
		if(Cpu.FlagReg == 1){
			eip = (short) (getAddr2Value(addr1, addr2, dir) - 1);
		}
	}
	/**
	 * 若标志寄存器FlagReg中的值为0，转移到第二地址指定的内存单元
	 * @param addr1
	 * @param addr2
	 * @param dir
	 */
	private void match_jmpzero(byte addr1 , byte addr2 , int dir){
		if(Cpu.FlagReg == 0){
			eip = (short) (getAddr2Value(addr1, addr2, dir) - 1);
		}
	}
	/**
	 * 将机器语言程序装入内存
	 * @param code
	 * @return
	 */
	private Boolean load(String code){
		int i,j;
		String []instructs = code.split("\n");
		for(i = 0;i < instructs.length ;i++){
			byte[] bs = null;
			try {
				bs = instructs[i].getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(j = 0;j<bs.length;j++){
				if(bs[j]>='0' && bs[j] <= '9'){
					bs[j] = (byte) (bs[j] - '0');
				}	
				else if(bs[j] >= 'a' && bs[j] <= 'f'){
					bs[j] = (byte) (bs[j] - 'a' + 10);
				}
//				System.out.println((short) bs[j]);
			}
			//将字符串指令转换为指令数据
			short ins = (short)(((bs[0] & 0x0000000F) << 12) |
								((bs[1] & 0x0000000F) << 8) |
								((bs[2] & 0x0000000F) << 4) |
								(bs[3] & 0x0000000F));
//			System.out.println((short)ins);
			Memory.memory[i] = ins;
		}
		//加载数据
		java.util.Iterator<Entry<String, Words>> iter = ASM.idMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Words word = (Words) entry.getValue();
			
			int value = 0;
			if(word.getValue() != null){
				value = Integer.parseInt(word.getValue());
			}
			Memory.memory[word.getAddress()] = (short) (value & 0xffff);
			System.out.println(Memory.memory[word.getAddress()]);
		}
		
		return true;
	}
	/**
	 * 获取第一地址的值
	 * @param addr1
	 * @param addr2
	 * @param dir
	 * @return
	 */
	private short getAddr1Value(byte addr1 , byte addr2 , int dir){
		short value = 0;
		switch(addr1){
		case 0:value = Cpu.R0;break;
		case 1:value = Cpu.R1;break;
		case 2:value = Cpu.R2;break;
		case 3:value = Cpu.R3;break;
		default:System.out.println("getAddr1Value"+addr1);
		}
		return value;
	}
	/**
	 * 获取第二地址的值
	 * @param addr1
	 * @param addr2
	 * @param dir
	 * @return
	 */
	private short getAddr2Value(byte addr1 , byte addr2 , int dir){
		short value = 0;
		if(addr2 == 0){
			//直接地址寻址
			value = (short) Memory.memory[dir];
		}
		else if(addr2 == 1){
			//寄存器寻址
			byte Ri = (byte) (((byte) dir) & 0x000f);
			byte mode = (byte) ((((byte) dir) >> 4) & 0x000f);
			if(mode == 0){
				//寄存器直接寻址
				switch(Ri){
				case 0:value = Cpu.R0;break;
				case 1:value = Cpu.R1;break;
				case 2:value = Cpu.R2;break;
				case 3:value = Cpu.R3;break;
				default:System.out.println("getAddr2Value-addr2(1)-mode(0)");
				}
			}
			else if(mode ==1){
				//寄存器间址寻址
				switch(Ri){
				case 0:value = Memory.memory[(int)(Cpu.R0 & 0x0000ffff)];break;
				case 1:value = Memory.memory[(int)(Cpu.R1 & 0x0000ffff)];break;
				case 2:value = Memory.memory[(int)(Cpu.R2 & 0x0000ffff)];break;
				case 3:value = Memory.memory[(int)(Cpu.R3 & 0x0000ffff)];break;
				default:System.out.println("getAddr2Value-addr2(1)-mode(1)");
				}
			}
			else{
				System.out.println("getAddr2Value-addr2(1)");
			}
		}
		else if(addr2 == 2){
			//立即寻址
			value = (short) dir;
		}
		else if(addr2 == 3){
			//变址寻址
			value = (short) (Memory.memory[dir*256+(Cpu.R3&0x00ff)]);
		}
		return value;
	}
	/**
	 * 判断输入字符串str是否合法
	 * @param str
	 * @return
	 */
	private Boolean assert_num(String str){
		int i = 0;
		if(str.equals("")){
			return false;
		}	
		if(!(str.charAt(0) == '-' || str.charAt(0) == '+') && !(str.charAt(i) >= '0' && str.charAt(i) <= '9')){
			return false;
		}
		for(i = 1;i < str.length(); i++){
			if(!(str.charAt(i) >= '0' && str.charAt(i) <= '9')){
				return false;
			}
		}
		return true;
	}
	
	public static void main(String[] argc){
		Exec exec = new Exec("2205\nf000",new MyDOS());
	}
	
}
