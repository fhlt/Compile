package compile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * 简单目标代码生成器
 * @author liangtao
 *
 */
public class ASM {
	//常数表存放在内存的第254页
	//无符号实常数表起始地址为254*256+255，表的使用向低地址区域延伸，每个无符号实常数占用两个单元
	private int realAddr = 254*256+255;
	//无符号常整数表的起始地址为254*256+0，表的适用向高地址区域延伸，每个无符号常整数占用一个单元
	private int constAddr = 254*256+0;
	//符号表放在内存的第253页
	//符号表的起始地址为253*256+255，表的使用向低地址区域延伸，每个符号占用四个单元
	private int signAddr = 253*256+255;
	//临时变量表的起始地址为252*256+0，表的使用向高地址区域延伸，每个符号占用四个单元
	private int tmpAddr = 252*256+0;
	//数据存储区起始地址为251*255+0，表的使用向高地址区域延伸
	private int dataAddr = 251*255+0;
	
	private ArrayList<String> asmCodeList=new ArrayList<String>();
	private ArrayList<FourElement> fourElemList;
	//基本块---------前驱节点
	private Map<Integer,Block> DAG = new HashMap<Integer,Block>();
	public static Map<String, Words> idMap;
	private ArrayList<String> id;
	private int eip = 0;
	public ASM(ArrayList<FourElement> fourElemList , LexAnalysis lex){
		this.fourElemList=fourElemList;
		this.idMap = lex.idMap;
		//首先需要对符号表中的变量分配内存
		allocation();
		lex.idMap = this.idMap;
		splitDAG();
		sortDAG();
		asmCode(fourElemList);//生成汇编代码
		
		try {
			getAsmFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
/*		
		Iterator iter = SemAnalysis.tmpVarMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Integer val = (Integer) entry.getValue();
			System.out.println(entry.getKey()+"---------"+val);
		}
		*/
	}

	private void asmCode(ArrayList<FourElement> fourElemList2) {
		// TODO Auto-generated method stub
		FourElement element;
		int index = 1;//跳过开头
		boolean flag;
		int c1 = 0,c2 = 0,c3 = 0;//只使用两个字节
		int d1 = 0,d2 = 0,d3 = 0;
		eip = 0;
		while(index<fourElemList2.size()){
			flag = false;
			element = fourElemList2.get(index);
			if(DAG.containsKey(element.id)){
				DAG.get(element.id).addr = eip;
			}
			
			
			if(element.op.charAt(0) == 'j'){
				//跳转语句
				int addr = Integer.parseInt(element.result);
				c3=addr/256;d3=addr%256;
			}
			else{
				if(element.result != null && !element.op.equals("sys")){
					int addr = idMap.get(element.result).getAddress();
					c3=addr/256;d3=addr%256;
				}	
			}
			//计算相对地址
			if(element.arg1 != null && !element.op.equals("sys")){
				int addr = idMap.get(element.arg1).getAddress();
				c1=addr/256;d1=addr%256;
			}
			if(element.arg2 != null && !element.op.equals("sys")){
				int addr = idMap.get(element.arg2).getAddress();
				c2=addr/256;d2=addr%256;
			}
			
			if(!element.op.equals("sys") && !element.op.equals("j")){
				//arg1送R0
				codeListAdd("load R3,"+d1);
				codeListAdd("load R0,"+c1+"[R3]");
				if(c1 == 252 || c1 == 253){
					codeListAdd("load R0,@R0");
				}
			}
			
			
			if (element.op.equals(":=")) {
				flag = true;
				//赋值运算，无需处理arg2
			}
			else if(element.op.equals("r")){
				flag =  true;
				codeListAdd("read R0");
			}
			else if(element.op.equals("w")){
				codeListAdd("write R0");
			}
			else if (element.op.equals("+")) {
				flag = true;
				codeListAdd("load R3," + d2);
				if(c2 == 252 || c2 == 253){
					//arg2是符号表或临时变量表的入口
					codeListAdd("load R3," + c2 +"[R3]");
					codeListAdd("add R0,@R3");
				}
				else{
					codeListAdd("add R0,"+ c2 +"[R3]");
				}
			}
			else if(element.op.equals("-")){
				flag = true;
				codeListAdd("load R3," + d2);
				if(c2 == 252 || c2 == 253){
					//arg2是符号表或临时变量表的入口
					codeListAdd("load R3," + c2 +"[R3]");
					codeListAdd("sub R0,@R3");
				}
				else{
					codeListAdd("sub R0,"+ c2 +"[R3]");
				}
			}
			else if(element.op.equals("*")){
				flag = true;
				codeListAdd("load R3," + d2);
				if(c2 == 252 || c2 == 253){
					//arg2是符号表或临时变量表的入口
					codeListAdd("load R3," + c2 +"[R3]");
					codeListAdd("mul R0,@R3");
				}
				else{
					codeListAdd("mul R0,"+ c2 +"[R3]");
				}
			}
			else if(element.op.equals("/")){
				flag = true;
				codeListAdd("load R3," + d2);
				if(c2 == 252 || c2 == 253){
					//arg2是符号表或临时变量表的入口
					codeListAdd("load R3," + c2 +"[R3]");
					codeListAdd("div R0,@R3");
				}
				else{
					codeListAdd("div R0,"+ c2 +"[R3]");
				}
			}
			else if(element.op.equals("j>")){
				codeListAdd("load R3," + d2);
				if(c2 == 252 || c2 == 253){
					//arg2是符号表或临时变量表的入口
					codeListAdd("load R3," + c2 +"[R3]");
					codeListAdd("cmp R0,@R3");
				}
				else{
					codeListAdd("cmp R0,"+ c2 +"[R3]");
				}
				codeListAdd("jmppos "+(c3*256+d3));
			}
			else if(element.op.equals("j<")){
				codeListAdd("load R3," + d2);
				if(c2 == 252 || c2 == 253){
					//arg2是符号表或临时变量表的入口
					codeListAdd("load R3," + c2 +"[R3]");
					codeListAdd("cmp R0,@R3");
				}
				else{
					codeListAdd("cmp R0,"+ c2 +"[R3]");
				}
				codeListAdd("jmpneg "+(c3*256+d3));
			}
			else if(element.op.equals("j==")){
				codeListAdd("load R3," + d2);
				if(c2 == 252 || c2 == 253){
					//arg2是符号表或临时变量表的入口
					codeListAdd("load R3," + c2 +"[R3]");
					codeListAdd("cmp R0,@R3");
				}
				else{
					codeListAdd("cmp R0,"+ c2 +"[R3]");
				}
				codeListAdd("jmpzero "+(c3*256+d3));
			}
			else if(element.op.equals("j")){
				codeListAdd("jmp "+(c3*256+d3));
			}
			else if(element.op.equals("sys")){
				codeListAdd("halt");//停机指令
			}
			//R0送result
			if(flag){
				codeListAdd("load R3," + d3);
				codeListAdd("load R3," + c3 + "[R3]");
				codeListAdd("store R0,@R3");	
			}	
			index++;
		}
		
		for(int i=0;i<asmCodeList.size();i++){
			if(asmCodeList.get(i).charAt(0) == 'j'){
				String ele = asmCodeList.get(i);
				String[] arr = ele.split(" ");
				String newele = arr[0]+" "+DAG.get(Integer.parseInt(arr[1])).addr;
				asmCodeList.set(i, newele);
			}
		}
		
		Iterator iter = DAG.entrySet().iterator();
		 while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Block value = (Block) entry.getValue();
			System.out.println(entry.getKey()+"--"+value.start+"--"+value.addr);
		 }
	}
	/**
	 * 添加四元式
	 * @param code
	 */
	private void codeListAdd(String code){
		asmCodeList.add(code);
		eip++;
	}

	/**
	 * 为符号表中的变量分配内存
	 */
	private void allocation(){
		//实常数表的起始地址为254*256+255
		//整常数表的起始地址为254*256
		Map<String, Words> tmpMap = new HashMap<String, Words>();
		Iterator iter = idMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Words val = (Words) entry.getValue();
			switch(val.getAttr()){
			case Words.INT_CONST:
				val.setAddress(constAddr);constAddr += 1;break;
			case Words.CHAR_CONST:
				break;
			case Words.DOUBLE_CONST:
				realAddr -= 2;val.setAddress(realAddr);break;
			case Words.IDENTIFIER:
				//为标识符分配地址
				signAddr -= 4;val.setAddress(signAddr);
				//为标识符分配存储地址
				Words tmpWord = new Words(-1, val.getWord()+"S", 0, 6);
				tmpWord.setAddress(dataAddr);
				val.setValue(dataAddr+"");
				dataAddr += 1;
				tmpMap.put(tmpWord.getWord(),tmpWord);
				break;
			}
			tmpMap.put((String)entry.getKey(), (Words)entry.getValue());
		}
		idMap = tmpMap;
		//处理临时变量表
		iter = SemAnalysis.tmpVarMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			entry.setValue(tmpAddr);tmpAddr += 4;
			Words tmpWord = new Words(-1, (String)entry.getKey(), 0, 6);
			tmpWord.setAddress((int)entry.getValue());
			
			Words tmpS = new Words(-1, (String)entry.getKey()+"S", 0, 6);
			tmpS.setAddress(dataAddr);
			tmpWord.setValue(dataAddr+"");
			dataAddr += 1;
			
			idMap.put(tmpS.getWord(),tmpS);
			
			idMap.put((String) entry.getKey(),tmpWord);
		}
	}
	/**
	 * 对DAG进行拓扑排序
	 */
	private void sortDAG(){
		 ArrayList<Integer> tmpDAG = new ArrayList<Integer>();
		 Queue<Integer> queue = new LinkedList<Integer>();
		 int i = 0;
		 Iterator iter = DAG.entrySet().iterator();
		 while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Block value = (Block) entry.getValue();
			if(value.pioneer == null){
				queue.add(value.start);				
			}

		 }
		 if(queue.isEmpty()){
			 System.out.println("无法翻译");
			 return;
		 }
		 while(!queue.isEmpty()){
			 int j = queue.poll();
			 tmpDAG.add(j);
			 iter = DAG.entrySet().iterator();
			 while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Block value = (Block) entry.getValue();
				if(value.pioneer != null && value.pioneer == j){
					queue.add(value.start);				
				}

			 }
		 }
		 for(i = 0;i<tmpDAG.size();i++){
			System.out.println(tmpDAG.get(i));
		} 
		 
	}
	/**
	 * 划分基本块
	 * 三原则
	 */
	private void splitDAG(){
		FourElement element;
		Map<Integer,Block> DAG = new HashMap<Integer,Block>();
		int index = 1;//跳过开头
		Block block = null;
		while(index<fourElemList.size()){
			element = fourElemList.get(index);
			if(index == 1){
				block = new Block();
				block.start = element.id;
				block.pioneer = null;
				if(element.op.charAt(0) == 'j'){
					block.pioneer = Integer.parseInt(element.result);
					DAG.put(element.id, block);
					
					element = fourElemList.get(block.pioneer);
					block = new Block();
					block.start = element.id;
					block.pioneer = null;
					if(element.op.charAt(0) == 'j'){
						block.pioneer = Integer.parseInt(element.result);
					}
					DAG.put(element.id, block);
				}
				else{
					DAG.put(element.id, block);
				}
			}
			else if(element.op.charAt(0) == 'j'){
				block = new Block();
				block.start = element.id;
				block.pioneer = Integer.parseInt(element.result);
				DAG.put(element.id, block);
				
				element = fourElemList.get(block.pioneer);
				block = new Block();
				block.start = element.id;
				block.pioneer = null;
				if(element.op.charAt(0) == 'j'){
					block.pioneer = Integer.parseInt(element.result);
				}
				DAG.put(element.id, block);
				
				if(index+1<fourElemList.size()){
					element = fourElemList.get(index+1);
					block = new Block();
					block.start = element.id;
					block.pioneer = null;
					if(element.op.charAt(0) == 'j'){
						block.pioneer = Integer.parseInt(element.result);
					}
					DAG.put(element.id, block);
				}
			}
			index++;
		}
		this.DAG = DAG;
		Iterator iter = this.DAG.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Block value = (Block) entry.getValue();
			System.out.println(value.start+"---------"+value.pioneer);
		}
	}
	/**
	 * 获取asm文件地址
	 * @return
	 * @throws IOException
	 */
	public String getAsmFile() throws IOException {
		
		File file = new File("./result/");
		if (!file.exists()) {
			file.mkdirs();
			file.createNewFile();// 如果这个文件不存在就创建它
		}
		String path = file.getAbsolutePath();
		FileOutputStream fos = new FileOutputStream(path + "/to_asm.asm");
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		OutputStreamWriter osw1 = new OutputStreamWriter(bos, "utf-8");
		PrintWriter pw1 = new PrintWriter(osw1);
		
		for(int i=0;i<asmCodeList.size();i++)
			pw1.println(asmCodeList.get(i));
		
		pw1.close();
		return path + "/to_asm.asm";
	}
	/**
	 * 基本块
	 * @author lenovo
	 *
	 */
	class Block{
		int start;//基本块起始位置
		Integer pioneer;//基本块的前驱
		int addr;
	}
}
