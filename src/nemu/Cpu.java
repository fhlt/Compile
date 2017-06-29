package nemu;
/**
 * cpu
 * @author Liangtao
 *
 */
public class Cpu {

	/**
	 * 4个通用寄存器
	 * 除可用于存放操作数和计算结果外，还可用于变址寻址
	 */
	public static short R0;
	public static short R1;
	public static short R2;
	public static short R3;
	/**
	 * 一个标志寄存器FlagReg
	 * 用于保存CMP指令比较结果
	 */
	public static short FlagReg;
	/**
	 * 一个堆栈寄存器TopReg
	 * 用作系统栈顶指针
	 */
	public static short TopReg;
	
	static{
		TopReg = 0;
	}
	public static void main(String[] args){
		System.out.println((int)((TopReg-1) & 0xffff));
	}
}
