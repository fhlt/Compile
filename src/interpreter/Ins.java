package interpreter;

import nemu.Instructions;

/**
 * 解析指令
 * @author LiangTao
 *
 */
public class Ins {

	private String insCode;
	public String option;
	public String argc1;
	public String argc2;
	public Boolean insOK;
	/**
	 * 构造方法
	 * @param insCode
	 */
	public Ins(String insCode){
		this.insCode = insCode;
		option = null;
		argc1 = null;
		argc2 = null;
		insOK = true;
		analy();
	}
	/**
	 * 分解指令并补充指令参数
	 */
	private void analy(){
		String insArr[] = insCode.split(" ");
		if(insArr.length == 1){
			//0操作数指令
			option = insArr[0];
		}
		else if(insArr.length == 2){
			String argcArr[] = insArr[1].split(",");
			if(argcArr.length == 1){
				//1操作数指令
				option = insArr[0];
				argc1 = argcArr[0];
			}
			else if(argcArr.length == 2){
				//2操作数指令
				option = insArr[0];
				argc1 = argcArr[0];
				argc2 = argcArr[1];
			}
			else{
				insOK = false;
				System.out.println("操作数过多");
			}
		}
		else{
			insOK = false;
			System.out.println("指令偏长");
		}
		if(Instructions.getInsbyOp(option) == null){
			insOK = false;
			System.out.println("没有该条指令"+option);
		}
	}
}
