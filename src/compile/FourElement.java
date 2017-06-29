package compile;

/**
 * 四元式
 * @author liangtao
 *
 */
public class FourElement {
	int id;//四元式的序号
	String op;//操作符
	String arg1;//第一个操作数
	String arg2;//第二个操作数
	String result;//结果
	public FourElement(){
		
	}
	/**
	 * 构造方法
	 * @param id 编号
	 * @param op 操作符
	 * @param arg1 第一个参数
	 * @param arg2 第二个参数
	 * @param result 结果
	 */
	public FourElement(int id,String op,String arg1,String arg2,String result){
		this.id=id;
		this.op=op;
		this.arg1=arg1;
		this.arg2=arg2;
		this.result=result;	
	}
}
