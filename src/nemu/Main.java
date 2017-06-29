package nemu;

import interpreter.Interpreter;

/**
 * Main
 * @author LiangTao
 *
 */
//load R0,5			2205
//store R0,Mfe      30fe
//load R0,fe		22fe
//load R1,3			2603
//add R1,@R0		6510
//halt				f000

//load R0,6			2206
//store R0,Mfe      30fe
//load R0,fe		22fe
//call @R0			4110
//add R1,@R0		6510
//halt				f000
//load R1,3			2603
//ret				5000

//load R0,5			2205
//store R0,Mfe      30fe
//load R0,fe		22fe
//jmp @R0			b110
//halt				f000
//load R1,3			2603
//add R1,@R0		6510
//halt				f000

//load R0,5			2205
//store R0,Mfe		30fe
//load R0,fe		22fe
//call @R0			4110
//halt 				f000
//load R3,5			2e05
//load R0,6			2206
//add R3,1			6e01
//cmp R3,0a			ae0a
//jmpneg 6			c207
//ret				5000

//read R0			0000

//load R0,5			2205
//write R0			1000
//halt				f000
public class Main {

	public static void main(String[] args){	
		String code = "2205\n30fe\n22fe\n4110\nf000\n2e00\n0000\n"
				+ "3301\n6e01\nae0a\nc206\n2e00\n0000\n"
				+ "3302\n6e01\nae0a\nc20c\n2a00\n2e00\n"
				+ "2301\n2702\n8101\n6900\n6e01\nae0a\n"
				+ "c213\n1800\n5000";
		String asm = "load R3,1\nload R0,254[R3]\n"
				+"write R0\nhalt";
/*		String asm = "load R3,1\nload R0,254[R3]\nload R3,0\nadd R0,254[R3]\nload R3,0\n"
				+ "load R0,252[R3]\nload R0,@R0\nload R3,251\nload R3,253[R3]\nstore R0,@R3\n"
				+"write R0\nhalt";
				*/
		Interpreter interpreter = new Interpreter(asm);
		if(interpreter.interpreterOK){
			System.out.println(interpreter.getMachIns());
			Exec exec = new Exec(interpreter.getMachIns(),new MyDOS());
		}
		int i =0;
		for(i = 0; i< 5;i++){
//			System.out.println(Memory.memory[i]);
		}
		System.out.println((int)(Cpu.R0 & 0xffff));
	}
}
