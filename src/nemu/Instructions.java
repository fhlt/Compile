package nemu;

import java.util.HashMap;
import java.util.Map;

public class Instructions {

	//指令集
	private static Map<String, String> instructions = new HashMap<String, String>();
	private static Map<String, String> toInstructions = new HashMap<String, String>();
	static {
		instructions.put("0", "read"); 
		instructions.put("1", "write");
		instructions.put("2", "load");
		instructions.put("3", "store");
		instructions.put("4", "call");
		instructions.put("5", "ret");
		instructions.put("6", "add");
		instructions.put("7", "sub");
		instructions.put("8", "mul");
		instructions.put("9", "div");
		instructions.put("10", "cmp"); //a
		instructions.put("11", "jmp"); //b
		instructions.put("12", "jmpneg"); //c
		instructions.put("13", "jmppos"); //d
		instructions.put("14", "jmpzero"); //e
		instructions.put("15", "halt"); //f
	}
	static{
		toInstructions.put("read", "0");
		toInstructions.put("write", "1");
		toInstructions.put("load", "2");
		toInstructions.put("store", "3");
		toInstructions.put("call", "4");
		toInstructions.put("ret", "5");
		toInstructions.put("add", "6");
		toInstructions.put("sub", "7");
		toInstructions.put("mul", "8");
		toInstructions.put("div", "9");
		toInstructions.put("cmp", "a"); //10
		toInstructions.put("jmp", "b"); //11
		toInstructions.put("jmpneg", "c");	//12
		toInstructions.put("jmppos", "d"); //13
		toInstructions.put("jmpzero", "e"); //14
		toInstructions.put("halt", "f"); //15
	}
	
	public static String getOpByIns(String op){
		return instructions.get(op);
	}
	public static String getInsbyOp(String ins){
		return toInstructions.get(ins);
	}
}
