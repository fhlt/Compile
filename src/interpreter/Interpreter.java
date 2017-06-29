package interpreter;

import nemu.Instructions;

public class Interpreter {

	private StringBuilder MachIns = null;
	public Boolean interpreterOK = true;
	/**
	 * 构造方法
	 * @param asm
	 */
	public Interpreter(String asm){
		interpreterOK = true;
		MachIns = new StringBuilder();
		interPre(asm);
	}
	/**
	 * 将汇编代码翻译为机器码
	 * @param asm
	 */
	private void interPre(String asm){
		String codeArr[] = asm.split("\n");
		for (String code : codeArr) {
			String bincode = "";
			Ins ins = new Ins(code);
			if(ins.insOK){
				System.out.println(ins.option+" "+ins.argc1+" "+ins.argc2);
				//操作码
				bincode += Instructions.getInsbyOp(ins.option);
				switch(divideOp(ins.option)){
				case -1:interpreterOK = false;break;
				case 0:bincode += "000";break;
				case 1:bincode += singleArgc(ins.option,ins.argc1);break;
				case 2:bincode += doubleArgc(ins.argc1,ins.argc2);break;
				}
				bincode +="\n";
				MachIns.append(bincode);
			}
			//有错误立即跳出
			if(interpreterOK == false){
				break;
			}
		}
	}
	/**
	 * 单操作数指令
	 * @param argc
	 * @return
	 */
	private String singleArgc(String op,String argc){
		String value = "";
		switch(op){
		case "read":
		case "write":
			switch(argc){
			case "R0":value = "000";break;//0000
			case "R1":value = "400";break;//0100
			case "R2":value = "800";break;//1000
			case "R3":value = "c00";break;//1100
				default:System.out.println("未定义的第一操作数"+argc);interpreterOK = false;break;
			}
			break;//第一地址
		case "call":
		case "jmp":
		case "jmpneg":
		case "jmppos":
		case "jmpzero":value = argc2Reflect(argc);break;//第二地址
		}
		return value;
	}
	/**
	 * 双操作数指令
	 * @param argc
	 * @return
	 */
	private String doubleArgc(String argc1,String argc2){
		String value = "";
		if(argc2.charAt(0) == 'M'){
			switch(argc1){
			case "R0":value += "0";break;
			case "R1":value += "4";break;
			case "R2":value += "8";break;
			case "R3":value += "c";break;
				default:System.out.println("doubleArgc-（M）");interpreterOK = false;break;
			}
			String Mr = argc2.substring(1);
			//合法校验
			Mr = Integer.toHexString(Integer.parseInt(Mr));
			if(Mr.length() == 0){
				value += "00";
			}
			else if(Mr.length() == 1){
				value += "0" + Mr;
			}
			else if(Mr.length() == 2){
				value += Mr;
			}
		}else if(argc2.charAt(0) == '@'){
			switch(argc1){
			case "R0":value += "1";break;
			case "R1":value += "5";break;
			case "R2":value += "9";break;
			case "R3":value += "d";break;
				default:System.out.println("doubleArgc-（@）");interpreterOK = false;break;
			}
			value += "1";
			String Ri = argc2.substring(1);
			switch(Ri){
			case "R0":value += "0";break;
			case "R1":value += "1";break;
			case "R2":value += "2";break;
			case "R3":value += "3";break;
				default:System.out.println("doubleArgc间址寻址（未知地址）");interpreterOK = false;break;
			}
		}else if(argc2.charAt(0) == 'R'){
			switch(argc1){
			case "R0":value += "1";break;
			case "R1":value += "5";break;
			case "R2":value += "9";break;
			case "R3":value += "d";break;
				default:System.out.println("doubleArgc-（R）");interpreterOK = false;break;
			}
			value += "0";
			switch(argc2){
			case "R0":value += "0";break;
			case "R1":value += "1";break;
			case "R2":value += "2";break;
			case "R3":value += "3";break;
				default:System.out.println("doubleArgc-（R）");interpreterOK = false;break;
			}
		}else{
			//立即寻址或变址寻址
			Plot plot = isDir(argc2);
			if(plot.isDir && interpreterOK){
				//立即寻址
				switch(argc1){
				case "R0":value += "2";break;
				case "R1":value += "6";break;
				case "R2":value += "a";break;
				case "R3":value += "e";break;
					default:System.out.println("doubleArgc-（D）" + argc1);interpreterOK = false;break;
				}
//				System.out.println(plot.C);
				argc2 = Utils.decToHex(plot.C);
				if(argc2.length() == 1){
					value += 0+argc2;
				}
				else if(argc2.length() == 2){
					value += argc2;
				}
			}else if(interpreterOK){
				//变址寻址
				switch(argc1){
				case "R0":value += "3";break;
				case "R1":value += "7";break;
				case "R2":value += "b";break;
				case "R3":value += "f";break;
					default:System.out.println("doubleArgc-（C）");interpreterOK = false;break;
				}
				String tmp = Integer.toHexString(plot.C);
				if(tmp.length() == 1){
					value += "0" + tmp;
				}
				else if(tmp.length() == 2){
					value += tmp;
				}
			}
		}
		return value;
	}
	/**
	 * 第一参数映射为机器码
	 * @param argc1
	 * @return
	 */
	private String argc1Reflect(String argc){
		String value = "";
		switch(argc){
		case "R0":value += "0";break;
		case "R1":value += "4";break;
		case "R2":value += "8";break;
		case "R3":value += "c";break;
			default:System.out.println("第一参数非法");interpreterOK = false;break;
		}
		return value;
	}
	/**
	 * 第二参数映射为机器码
	 * @param argc2
	 * @return
	 */
	private String argc2Reflect(String argc){
		String value = "";
		if(argc.charAt(0) == 'M'){
			//直接地址寻址
			value += "0";
			String Mr = argc.substring(1);
			//合法校验
			Mr = Integer.toHexString(Integer.parseInt(Mr));
			if(Mr.length() == 0){
				value += "00";
			}
			else if(Mr.length() == 1){
				value += "0" + Mr;
			}
			else if(Mr.length() == 2){
				value += Mr;
			}
		}else if(argc.charAt(0) == '@'){
			//寄存器寻址-间址寻址
			value += "11";
			String Ri = argc.substring(1);
			switch(Ri){
			case "R0":value += "0";break;
			case "R1":value += "1";break;
			case "R2":value += "2";break;
			case "R3":value += "3";break;
				default:System.out.println("间址寻址（未知地址）");interpreterOK = false;break;
			}
		}else if(argc.charAt(0) == 'R'){
			//寄存器寻址-寄存器直接寻址
			value += "10";
			switch(argc){
			case "R0":value += "0";break;
			case "R1":value += "1";break;
			case "R2":value += "2";break;
			case "R3":value += "3";break;
				default:System.out.println("直接寻址（未知地址）");interpreterOK = false;break;
			}
		}else{
			//立即寻址或变址寻址
			Plot plot = isDir(argc);
			if(plot.isDir && interpreterOK){
				//立即寻址
				value += "2";
//				System.out.println(plot.C);
				
				//合法校验
				String Mr = Integer.toHexString(Integer.parseInt(argc));
				if(Mr.length() == 0){
					value += "00";
				}
				else if(Mr.length() == 1){
					value += "0" + Mr;
				}
				else if(Mr.length() == 2){
					value += Mr;
				}
				else{
					System.out.println("doubleArgc-（D）");interpreterOK = false;
				}
			}else if(interpreterOK){
				//变址寻址
				value += "3";
				String tmp = Integer.toHexString(plot.C);
				if(tmp.length() == 1){
					value += "0" + tmp;
				}
				else if(tmp.length() == 2){
					value += tmp;
				}
			}
		}
		return value;
	}
	/**
	 * 区分单操作数指令和双操作数指令
	 * @param op
	 * @return 0/1/2
	 */
	private byte divideOp(String op){
		byte value = -1;
		switch(op){
		case "ret":
		case "halt":value = 0;break;
		case "read":
		case "write":
		case "call":
		case "jmp":
		case "jmpneg":
		case "jmppos":
		case "jmpzero":value = 1;break;
		case "store":
		case "load":
		case "add":
		case "sub":
		case "mul":
		case "div":
		case "cmp":value = 2;break;
		default:System.out.println("没有"+op+"指令");
		}
		return value;
	}
	/**
	 * 区分立即寻址与变址寻址
	 * @return
	 */
	private Plot isDir(String argc){
		Plot plot = new Plot();
		int i = 0;
		int recoderLeft = 0,recoderRight = 0;
		String C = "";
		String register = "";
		for(i = 0;i<argc.length();i++){
			if(argc.charAt(i) == '['){
				recoderLeft++;
			}else if(argc.charAt(i) == ']' && recoderLeft == 1){
				recoderRight++;
			}
			//记录参数C
			else if(recoderLeft == 0 && recoderRight == 0){
				C += argc.charAt(i);
			}
			//记录Ri
			else if(recoderLeft == 1 && recoderRight == 0){
				register += argc.charAt(i);
			}
		}
		int iValue = -1;
		if(!C.equals("") && Utils.isHexNumber(C)){
			iValue = Integer.parseInt(C);
		}
		else if(C.equals("")){
			iValue = 0;
		}
		//页号越界
		if(iValue < 0 || iValue > 0xff){
			System.out.println("isDir"+"页号越界"+iValue);
			interpreterOK = false;
		}
		else{
			plot.C = iValue;
		}
		if(recoderRight == 1 && recoderLeft == 1){
			plot.isDir = false;
		}
		//中括号不匹配异常
		if(!(recoderRight == 1 && recoderLeft == 1)){
			if(!(recoderRight == 0 && recoderLeft == 0)){
				System.out.println("isDir"+"不匹配的中括号");
				interpreterOK = false;
			}
		}
		//变址寄存器异常
		if((recoderRight == 1 && recoderLeft == 1) && !register.equals("R3")){
			System.out.println("isDir"+"非R3("+register+")");
			interpreterOK = false;
		}
		return plot;
	}
	/**
	 * 内部类
	 * @author Liangtao
	 */
	class Plot{
		public int C = 0;
		public Boolean isDir = true;
	}
	/**
	 * 获取机器指令代码
	 * @return
	 */
	public String getMachIns(){
		return MachIns.toString();
	}
	
	public static void main(String[] argc){
		Interpreter interpreter = new Interpreter(";");
		System.out.println(interpreter.isDir("2[R3]"));
	}
	
}
