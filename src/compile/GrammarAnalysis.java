package compile;

import java.awt.geom.GeneralPath;
import java.beans.Expression;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatFlagsException;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;

import javax.swing.text.html.HTMLEditorKit.Parser;
/**
 * 语法分析
 * @author Liangtao
 *
 */
public class GrammarAnalysis {

	private int lastj = 0;
	private int ele_num = 0;
	private int tmp_num = 0;
	private Iterator iter;
	private int tokenCount = 0;
	private LexAnalysis lexAnalysis;//词法分析结果
	public Map<Integer, Error> errorMap= new HashMap<Integer, Error>();//错误信息
	public ArrayList<FourElement>fourElemList=new ArrayList<FourElement>();//四元式列表
	public StringBuilder builder = new StringBuilder();
	private Words token;
	private Stack<Words> stack = new Stack<>();
	
	private int errCount = 0;
	public GrammarAnalysis(LexAnalysis lexAnalysis){
		this.lexAnalysis = lexAnalysis;
		iter = lexAnalysis.wordMap.entrySet().iterator();
		if(parser()){
			System.out.println("语法分析结束");
//			System.out.println(builder.toString());
		}
	}
	/**
	 * 从token表中读取单词，根据定义进行分析
	 * @return
	 */
	private Boolean parser(){
		int level = 0;//层次
		LevelOfBuilder(level, "语法分析开始");
		token = getNextToken();
		LevelOfBuilder(level, "program分析");
		if(token!=null && !token.getWord().equals("program")){
			error("缺少关键字program",token);
		}
		token = getNextToken();
		if(token!=null && !isidentifier(token)){
			error("缺少程序名字",token);
		}
		String proName = token.getWord();
		token = getNextToken();
		if(!token.getWord().equals(";")){
			error("缺少;",token);
		}
		gencode("program", proName, null, null);//程序头部特殊四元式
		token = getNextToken();
		if(token!=null && token.getWord().equals("const")){//下面是常量说明语句
			LevelOfBuilder(level+1, "const常量说明语句");
			token = getNextToken();
			const_st(token,level+1);
			token = getNextToken();
		}
		if(token!=null && token.getWord().equals("var")){//下面是说明语句
			LevelOfBuilder(level+1, "var变量说明语句");
			token = getNextToken();
			token = var_st(token,level+1);
		}
//		System.out.println("说明语句结束");
//		token = getNextToken();		//下面是可执行部分
		if(token!=null && token.getWord().equals("begin")){
			token = getNextToken();
			ST_SORT(level+1);			//分类各种可执行语句
		}
		token = getNextToken();
		if(token!=null && token.getWord().equals(".")){
			//处理程序结束
			LevelOfBuilder(level, "语法分析结束");
			gencode("sys", proName, null, null);//程序结束特殊四元式
			return true;
		}
		return true;
	}
	/**
	 * 分类控制语句
	 */
	private void ST_SORT(int level) {
		// TODO Auto-generated method stub
		while(true){
			if(token != null && token.getWord().equals("if")){
				LevelOfBuilder(level, "if语句分析");
				ifs(level+1);//调用if语句分析模块
//				System.out.println("if语句结束");
			}
			else if(token != null && token.getWord().equals("while")){
				LevelOfBuilder(level, "while语句分析");
				whiles(level+1);//调用while语句分析模块
			}
			else if(token != null && token.getWord().equals("repeat")){
				LevelOfBuilder(level, "repeat语句分析");
				repeats(level+1);//调用repeat语句分析模块
			}
			else if(token != null && token.getWord().equals("for")){
				LevelOfBuilder(level, "for语句分析");
				fors(level+1);//调用for语句分析模块
			}	
			else if(token != null && token.getWord().equals("do")){
				LevelOfBuilder(level, "do-while语句分析");
				dowhiles(level+1);//调用do-while语句分析模块
			}	
			else if(token != null && token.getWord().equals("end")){
				break;
			}
			else if(token != null && token.getWord().equals(";")){
				//空语句
			}
			else if(token != null && token.getWord().equals("write")){
				LevelOfBuilder(level, "write语句分析");
				writes(level);
			}
			else if(token != null && token.getWord().equals("read")){
				LevelOfBuilder(level, "read语句分析");
				reads(level);
			}
			else{
				LevelOfBuilder(level, "赋值语句分析");
				assign(token,level+1);//赋值语句分析
			}
			token = getNextToken();
		}
	}
	
	
	/**
	 * 分类执行语句
	 */
	public void exe_sort(int level){
		if(token != null && token.getWord().equals("if")){
			LevelOfBuilder(level, "if语句分析");
			ifs(level+1);//调用if语句分析模块
		}
		else if(token != null && token.getWord().equals("while")){
			LevelOfBuilder(level, "while语句分析");
			whiles(level+1);//调用while语句分析模块
		}
		else if(token != null && token.getWord().equals("repeat")){
			LevelOfBuilder(level, "repeat语句分析");
			repeats(level+1);//调用repeat语句分析模块
		}
		else if(token != null && token.getWord().equals("for")){
			LevelOfBuilder(level, "for语句分析");
			fors(level+1);//调用for语句分析模块
		}	
		else if(token != null && token.getWord().equals("do")){
			LevelOfBuilder(level, "do-while语句分析");
			dowhiles(level+1);//调用do-while语句分析模块
		}	
		else if(token != null && token.getWord().equals("write")){
			LevelOfBuilder(level, "write语句分析");
			writes(level);
		}
		else if(token != null && token.getWord().equals("read")){
			LevelOfBuilder(level, "read语句分析");
			reads(level);
		}
		else{
			LevelOfBuilder(level, "赋值语句分析");
			assign(token,level+1);//赋值语句分析
		}
	}
	/**
	 * read语句分析
	 * @param level
	 */
	private void reads(int level){
		token = getNextToken();
		token = getNextToken();//参数
		if(token != null && !token.getWord().equals(";")){
			error("read语句缺少参数",token);
		}
	}
	/**
	 * write语句分析
	 * @param level
	 */
	private void writes(int level){
		token = getNextToken();
		token = getNextToken();//参数
		if(token != null && !token.getWord().equals(";")){
			error("write语句缺少参数",token);
		}
	}
	/**
	 * do-while语句分析 
	 * @param i
	 */
	private void dowhiles(int level) {
		// TODO Auto-generated method stub
		token = getNextToken();
		exe_sort(level+1);
		if(token != null && token.getWord().equals("while")){
			bexpr(level+1);
		}
		else{
			error("do while语句缺少 while",token);
		}
	}
	/**
	 * for语句分析模块
	 */
	private void fors(int level) {
		// TODO Auto-generated method stub
		token = getNextToken();
		if(token != null && !token.getAttr().equals(Words.IDENTIFIER)){
			error("for语句表示错误", token);
		}
		token = getNextToken();
		if(token != null && !token.getWord().equals(":=")){
			error("for语句表示错误", token);
		}
		token = getNextToken();
		ar_expr();//算术表达式
		if(token != null && !token.getWord().equals("to")){
			error("for语句此处应该是to", token);
		}
		token = getNextToken();
		ar_expr();
		if(token != null && !token.getWord().equals("do")){
			error("for语句此处应该是to", token);
		}
		token = getNextToken();
		exe_sort(level+1);//执行语句
	}
	/**
	 * repeat语句分析模块
	 */
	private void repeats(int level) {
		// TODO Auto-generated method stub
		token = getNextToken();
		exe_sort(level+1);
		while(true){
			token = getNextToken();
			if(token != null && !token.getWord().equals("until")){
				exe_sort(level+1);
			}
			else
				break;
		}
		bexpr(level+1);
//		token = getNextToken();
		if(token != null && !token.getWord().equals(";")){
			error("repeat没有正常结束", token);
		}
	}
	/**
	 * while语句分析模块
	 */
	private void whiles(int level) {
		// TODO Auto-generated method stub
		bexpr(level+1);//分析布尔表达式（）
//		token = getNextToken();
		if(token != null && !token.getWord().equals("do")){
			error("while语句此处应该是to", token);
		}
		token = getNextToken();
		exe_sort(level+1);//执行语句
	}
	/**
	 * if语句分析模块
	 */
	private void ifs(int level) {
		// TODO Auto-generated method stub
		/*
		bool_expr();//至少要有一个布尔表达式
		while(true){
			token = getNextToken();
			if(token != null && (token.getWord().equals("and") || token.getWord().equals("or"))){
				bool_expr();
			}
			else if(token != null  && token.getWord().equals("then")){
				break;
			}
			else{
				System.out.println("错误的if条件语句");
				error("错误的if条件语句", token);break;
			}
		}
		*/
//		bool_term(level+1);
		bexpr(level+1);
		token = getNextToken();
		exe_sort(level+1);//执行语句
//		token = getNextToken();
		if(token != null && token.getWord().equals("else")){
			token = getNextToken();
			exe_sort(level+1);//执行语句
		}
	}

	/**
	 * 逻辑表达式->布尔项 or 逻辑表达式 | 布尔项
	 * 布尔项->布尔项 and 布尔因子 | 布尔因子
	 * 布尔因子->!布尔因子 | 布尔量
	 * 布尔量->布尔常数 | 标识符 | （布尔表达式）| 关系表达式
	 * 关系表达式->算术表达式 关系运算符 算术表达式
	 */
	private void bexpr(int level){
		LevelOfBuilder(level, "逻辑表达式分析");
		
		String term1 = bterm();//分析项
		if(token != null && token.getWord().equals("or")){
//			sym = token.getWord();
			bexpr(level);
//			s_ret = get_tmpvar();//生成一个新的临时变量
//			gencode(sym,term1,expr1,s_ret);
//			term1 = s_ret;
		}
		//布尔表达式分析结束
	}
	/**
	 * 把以P1，P2为链首的四个四元式合为一个链，返回合并后的链首
	 * @param p1
	 * @param p2
	 * @return
	 */
	private int merg(int p1,int p2){
		if(p2 == 0)    return p1;
		else{
			int p = p2;
			while(Integer.parseInt(fourElemList.get(p).result.toString())!=0){
				p = Integer.parseInt(fourElemList.get(p).result.toString());
			}
			fourElemList.get(p).result = p1+"";
			return p2;
		}
	}
	/**
	 * 回填出口
	 * 把p为链首的四元式单向链中的每个四元式中的第四项置为t
	 * @param p
	 * @param t
	 */
	private void backpatch(int p,int t) {
		// TODO Auto-generated method stub
		int q = p;
		while(q!=0){
			int m = Integer.parseInt(fourElemList.get(q).result.toString());
			fourElemList.get(q).result = t+"";
			q = m;
		}
	}
	/**
	 * 布尔项
	 */
	private String bterm(){
		String s_ret = null;
		String term = null;
		String sym = null;
		String fac1 = bfactor();
//		token = getNextToken();
		if(token != null && token.getWord().equals("and")){
//			sym = token.getWord();
			term = bterm();
//			s_ret = get_tmpvar();//生成一个新的临时变量
//			gencode(sym,fac1,term,s_ret);
//			fac1 = s_ret;
		}
		return fac1;
	}
	/**
	 * 布尔因子
	 */
	private String bfactor(){
		String fac = "";
		token = getNextToken();
		if(token != null && token.getWord().equals("!")){
			fac = bfactor();
		}
		else if(token != null && token.getWord().equals("(")){
			fac = bfactor();
			token = getNextToken();
		}
		else{
			fac = bel();//布尔量
		}
		gencode("j", null, null, "0");//假出口
		return fac;
	}
	/**
	 * 布尔量
	 */
	private String bel(){
		String bel = null;
		if(token != null && token.getWord().equals("true") || token.getWord().equals("false")){
			bel = token.getWord();
			gencode("jnz",token.getWord(),null,ele_num+2+"");//真出口
		}
		else{
			bel = rexpr();
		}
		return bel;
	}
	/**
	 * 关系表达式
	 */
	private String rexpr(){
		String arexpr1 = ar_expr();
		String arexpr2 = null;
		String sym = null;
		String s_ret = null;
		if(token != null && token.getWord().equals(">") ||
							token.getWord().equals("<") ||
							token.getWord().equals("<>")||
							token.getWord().equals("<=")||
							token.getWord().equals(">=")||
							token.getWord().equals("==")){
			sym = "j"+token.getWord();
			token = getNextToken();
			arexpr2 = ar_expr();
//			s_ret = get_tmpvar();
			gencode(sym, arexpr1, arexpr2, "0");
//			token = getNextToken();
		}
		else{
			gencode("jnz", arexpr1, arexpr2,ele_num+2+"");//不确定
		}
		return s_ret;
	}
	/**
	 * 赋值语句
	 */
	private void assign(Words temToken,int level) {
		// TODO Auto-generated method stub	
		String sym = "";
		String result = "";
		if(temToken == null)	
			token = getNextToken();
		else
			token = temToken;
		if(token !=null && !token.getAttr().equals(Words.IDENTIFIER)){
			error("赋值语句错误（非标识符）", token);
		}
		else{
			result = token.getWord();
		}
		token = getNextToken();
		if(token !=null && !token.getWord().equals(":=")){
			error("赋值语句错误（非:=）", token);
		}
		else{
			sym = token.getWord();
		}
		token = getNextToken();
		String fac1 = ar_expr();
		gencode(sym, fac1, null, result);
	}
	/**
	 * 算术表达式
	 * 表达式-><项> {<加法运算符><项>}
	 * 项-><因子> {<乘法运算符><因子>}
	 * 因子-><标识符> | <无符号整数> | ‘（’<表达式> ‘）’
	 */
	private String ar_expr() {
		// TODO Auto-generated method stub
//		if(token != null && (token.getWord().equals("-") || token.getWord().equals("+"))){
//			token = getNextToken();
//		}
		String s_ret = "";
		//项
		String term1 = nape();//处理乘除，取负和括号部分
		while(true){
			if(token != null && token.getWord().equals("+") || 
					token.getWord().equals("-")){
				String sym = token.getWord();
				token = getNextToken();
				String term2 = nape();
				s_ret = get_tmpvar();
				gencode(sym,term1,term2,s_ret);
				term1 = s_ret;//将该临时变量放入term1中
			}
			else{
				s_ret = term1;
				break;
			}
		}
		return s_ret;
	}
	/**
	 * 项-><因子> {<乘法运算符><因子>}
	 */
	private String nape() {
		// TODO Auto-generated method stub
		String term ="";
		String fac1 = gene();
		token = getNextToken();
		while(true){
			if(token != null && token.getWord().equals("*") || token.getWord().equals("/")){
				//遇到乘除号
				String sym = token.getWord();
				token = getNextToken();
				String fac2 = gene();
				term = get_tmpvar();
				gencode(sym, fac1, fac2, term);
				fac1 = term;
				token = getNextToken();
			}
			else{
				term = fac1;
				break;
			}
		}
		return term;
	}
	/**
	 * 因子-><标识符> | <无符号整数> | ‘（’<表达式> ‘）’
	 */
	private String gene() {
		// TODO Auto-generated method stub
//		Words token = getNextToken();
		String fac = "";
		if(token != null && token.getAttr().equals(Words.IDENTIFIER) ||
							token.getAttr().equals(Words.INT_CONST) ||
							token.getAttr().equals(Words.DOUBLE_CONST)){//单个变量常量
			if(token.getAttr().equals(Words.IDENTIFIER) && 
					lexAnalysis.sym_entry(token.getWord())!=-1){
				//不一定在符号表中
			}
			fac = token.getWord();
		}
		else if(token != null && token.getWord().equals("(")){
			token = getNextToken();
			fac = ar_expr();
			if(token != null && !token.getWord().equals(")")){
				error("此处缺少右括号", token);
			}
		}
		//处理单目取负
		else if(token != null && token.getWord().equals("-")){
			token = getNextToken();
			fac = gene();
			String expr = get_tmpvar();
			gencode("-", fac, null, expr);
			fac = expr;
		}
		else{
//			System.exit(0);
		}
		return fac;
	}
	/**
	 * 布尔项
	 * 递归调用不好吧
	 */
	private void bool_term(int level){
		LevelOfBuilder(level, "布尔表达式分析");
		bool_expr();
//		bfactor();
		while(true){
			token = getNextToken();
			if(token != null && (token.getWord().equals("and") || token.getWord().equals("or"))){
				bool_expr();
			}
			else
				break;
		}
//		builder.append("----------布尔表达式分析结束\n");
	}
	/**
	 * 处理布尔表达式
	 */
	private void bool_expr() {
		// TODO Auto-generated method stub
//		builder.append("----------布尔项分析开始\n");
		Words token = getNextToken();
		if(token != null && !token.getWord().equals("(")){
			error("布尔表达式缺少左括号", token);
		}
		token = getNextToken();
		
		while(true){
			if(token!=null && token.getWord().equals("!")){
				token = getNextToken();
			}
			if(token != null && !(token.getAttr().equals(Words.IDENTIFIER) ||
				 	 token.getAttr().equals(Words.INT_CONST) ||
					 token.getAttr().equals(Words.DOUBLE_CONST))){
					error("布尔表达式中错误的标识符或常量", token);
				}
				token = getNextToken();
				if(token != null && token.getWord().equals(">") || 
						token.getWord().equals("<") || 
						token.getWord().equals(">=") ||
						token.getWord().equals("<=") ||
						token.getWord().equals("==") ||
						token.getWord().equals(")")){
					break;//布尔表达式左边完毕
				}
				else if(token != null && token.getAttr().equals(Words.OPERATOR)){
					token = getNextToken();
				}
				else{
					error("错误的布尔项", token);break;
				}
		}
		if(token != null && token.getWord().equals(")")){
//			builder.append("----------布尔项分析结束\n");
			return ;
		}
		//跳出循环后应该是左边已经完毕
		token = getNextToken();
		while(true){
			if(token != null && !(token.getAttr().equals(Words.IDENTIFIER) ||
				 	 token.getAttr().equals(Words.INT_CONST) ||
					 token.getAttr().equals(Words.DOUBLE_CONST))){
					error("布尔表达式中错误的标识符或常量", token);
				}
				token = getNextToken();
				if(token != null && token.getWord().equals(")")){
//					System.out.println("正常退出");
					break;//布尔表达式左边完毕
				}
				else if(token != null && token.getAttr().equals(Words.OPERATOR)){
					token = getNextToken();
				}
				else{
					error("错误的布尔表达式", token);break;
				}
		}
//		builder.append("----------布尔项分析结束\n");
		//右边完毕
	}
	/**
	 * 说明语句
	 * @param token
	 */
	private Words var_st(Words token,int level) {
		// TODO Auto-generated method stub
		stack.clear();//清空栈
		while(token!=null && !token.getWord().equals(":")){
			if(token!=null && !token.getAttr().equals(Words.IDENTIFIER)){
				error("非标识符的定义",token);
			}
			else{
				stack.push(token);
			}
			token = getNextToken();
			if(token!=null && token.getWord().equals(",")){
				token = getNextToken();
			}
			else if(token!=null && token.getWord().equals(":")){
				break;
			}
		}
		token = getNextToken();
		if(token!=null && !(token.getWord().equals("integer") || token.getWord().equals("char")
				|| token.getWord().equals("double") || token.getWord().equals("float"))){
			error("错误的变量定义",token);
		}
		else{
			stack.push(token);
		}
		token = getNextToken();
		if(token!=null && !token.getWord().equals(";")){
			error("定义变量后缺少';'",token);
		}
		//查填符号表
		String tempType = stack.pop().getWord();
		while(!stack.isEmpty()){
			Words tempToken = stack.pop();
			lexAnalysis.checkAndFillType(tempToken, tempType);
		}
		token = getNextToken();
		if(token!=null && !token.getWord().equals("begin")){
			token = var_st(token,level);
		}
		return token;
	}
	/**
	 * 常量说明语句
	 * @param token
	 */
	private void const_st(Words token,int level) {
		// TODO Auto-generated method stub
		stack.clear();
		if(token != null && !token.getAttr().equals(Words.IDENTIFIER)){
			error("常量说明错误",token);
		}
		else{
			stack.push(token);
		}
		token = getNextToken();
		if(token != null && !token.getWord().equals("=")){
			error("常量说明缺少等号",token);
		}
		//常量检查
		token = getNextToken();
		if(token != null && !(token.getAttr().equals(Words.INT_CONST) || 
				token.getAttr().equals(Words.DOUBLE_CONST))){
			error("不是常量", token);
		}
		else{
			lexAnalysis.checkAndFillVal(stack.pop(), token.getWord());
		}
		token = getNextToken();
		if(token != null && token.getWord().equals(",")){
			token = getNextToken();
			const_st(token,level);
		}
		else if(token != null && token.getWord().equals(";")){
			return ;
		}
		else{
			error("常量定义缺少';'", token);
			return ;
		}
	}
	/**
	 * 标识符
	 * @param word
	 * @return
	 */
	private boolean isidentifier(Words token) {
		// TODO Auto-generated method stub
		if(token.getAttr().equals(Words.IDENTIFIER))
			return true;
		else
			return false;
	}
	/**
	 * LevelOfBuilder
	 * @param level
	 * @param str
	 */
	private void LevelOfBuilder(int level,String str){
		int i = 0;
		for(i = 0;i<level;i++){
			builder.append("------");//6个字符
		}
		builder.append(str+"\n");
	}

	/**
	 * error()
	 * @param string
	 */
	private void error(String string,Words token){
		Error error = new Error(errCount, string ,token.getLine(),token);
		errorMap.put(errCount++, error);
	}
	/**
	 * 得到上一个token
	 * @return
	 */
	private Words getLastToken(){ 
		Words tempToken = lexAnalysis.wordMap.get(token.getId() - 1);
		tokenCount = tempToken.getId()+1;
		return tempToken;
	}
	/**
	 * 得到下一个token
	 * @return
	 */
	private Words getNextToken(){
		Words token = null;
		token = lexAnalysis.wordMap.get(tokenCount++);
		/*
		if (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			token = (Words) entry.getValue();
		}*/
		if(token  == null ){
			token = new Words(-1, "end", -1, 0);
		}
		
		return token;
	}
	/**
	 * 根据参数产生四元式并将它填入四元式表
	 * @param op
	 * @param arg1
	 * @param arg2
	 * @param result
	 */
	private void gencode(String op,String arg1,String arg2,String result){
		FourElement element = new FourElement(ele_num++,op,arg1,arg2,result);
		fourElemList.add(element);
	}
	/**
	 * 调用一次产生一个新的临时变量T0 T1
	 * @return
	 */
	private String get_tmpvar(){
		return "T"+(tmp_num++);
	}
	/**
	 * 打印四元式表
	 * @return
	 * @throws IOException
	 */
	public String outputFourElem() throws IOException{
		
		File file=new File("./result/");
		if(!file.exists()){
			file.mkdirs();
			file.createNewFile();//如果这个文件不存在就创建它
		}
		String path=file.getAbsolutePath();
		FileOutputStream fos=new FileOutputStream(path+"/FourElement.txt");  
		BufferedOutputStream bos=new BufferedOutputStream(fos); 
		OutputStreamWriter osw1=new OutputStreamWriter(bos,"gb2312");
		PrintWriter pw1=new PrintWriter(osw1);
		pw1.println("生成的四元式如下");
		pw1.println("序号（OP,ARG1,ARG2,RESULT）");
		FourElement temp;
		for(int i=0;i<fourElemList.size();i++){
			temp=fourElemList.get(i);
			pw1.println(temp.id+"("+temp.op+","+temp.arg1+","+temp.arg2+","+temp.result+")");
		}
		pw1.close();
		return path+"/FourElement.txt";
	}
	
	/**
	 * 错误信息
	 * @return
	 */
	public String getError(){
		String string = "错误编号\t"+"错误行\t"+"内容\t"+"错误信息\n";
		StringBuilder builder = new StringBuilder();
		builder.append(string);
		Iterator iter = errorMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Object key = entry.getKey();
			Error val = (Error) entry.getValue();
			string  = key+"\t"+val.getLine()+"\t"+val.getWord().getWord()+"\t"+val.getInfo()+"\n";
			builder.append(string);
		}
		return builder.toString();
	}
	
}
